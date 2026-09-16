from __future__ import annotations

import argparse
import hashlib
import json
import mimetypes
import os
import shutil
import subprocess
import sys
from pathlib import Path
from urllib.request import Request, urlopen

from .pipeline import (
    build_manifest,
    build_metadata_review_report,
    build_publishable_manifest,
    build_remote_catalog,
    build_upload_plan,
    detect_duplicates,
    scan_directory,
    prepare_streaming_records,
    stage_valid_files,
    validate_manifest,
    write_csv,
)


def _json_dump(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def _scan(args: argparse.Namespace) -> int:
    source = Path(args.source).expanduser().resolve()
    output = Path(args.output).expanduser().resolve()
    if output == source or source in output.parents:
        raise ValueError("output directory must be outside the read-only source directory")
    rights_status = str(args.rights_status or "UNKNOWN").upper()
    if rights_status not in {"UNKNOWN", "OWNER_CREATED", "LICENSED", "PUBLIC_DOMAIN_RECORDING", "NOT_APPROVED"}:
        raise ValueError(f"unsupported rights status: {rights_status}")
    records = prepare_streaming_records(scan_directory(source, rights_status=rights_status, rights_evidence=args.rights_evidence))
    if args.stage:
        stage_valid_files(records, output / "staged", ffmpeg=shutil.which("ffmpeg"))
    manifest = build_manifest(records, args.base_url, args.catalog_version)
    publishable = build_publishable_manifest(records, args.base_url, args.catalog_version)
    duplicates = detect_duplicates(records)
    output.mkdir(parents=True, exist_ok=True)
    write_csv(output / "AUDIO_INVENTORY.csv", records)
    write_csv(output / "AUDIO_DUPLICATES.csv", [
        {"duplicateType": kind, "files": " | ".join(paths)}
        for kind, groups in duplicates.items() for paths in groups
    ])
    _json_dump(output / "PRODUCTION_CONTENT_DRAFT.json", manifest)
    _json_dump(output / "PRODUCTION_CONTENT_PUBLISHABLE.json", publishable)
    _json_dump(output / "catalog" / "latest.json", build_remote_catalog(publishable))
    write_csv(output / "R2_UPLOAD_PLAN.csv", build_upload_plan(manifest, output / "staged"))
    quality = _quality_report(records, duplicates, rights_status, args.rights_evidence)
    (output / "AUDIO_QUALITY_REPORT.md").write_text(quality, encoding="utf-8")
    (output / "METADATA_REVIEW_REPORT.md").write_text(build_metadata_review_report(records), encoding="utf-8")
    (output / "INGESTION_SUMMARY.md").write_text(
        _ingestion_summary(records, duplicates, manifest, publishable, args.existing_usage_bytes, output / "staged"), encoding="utf-8"
    )
    print(f"Scanned {len(records)} supported audio file(s) from {source}")
    print(f"Reports written under {output}")
    if not args.stage:
        print("No staged media was created; pass --stage to copy valid files into the output directory.")
    return 0


def _ingestion_summary(records: list[dict], duplicates: dict[str, list[list[str]]], manifest: dict, publishable: dict, existing_usage_bytes: int | None, staging_dir: Path) -> str:
    valid = [record for record in records if record.get("valid") and not record.get("duplicateOf")]
    invalid = [record for record in records if not record.get("valid")]
    staged_files = [path for path in staging_dir.rglob("*") if path.is_file()] if staging_dir.is_dir() else []
    staged_bytes = sum(path.stat().st_size for path in staged_files)
    original_bytes = sum(int(record.get("fileSizeBytes") or 0) for record in records)
    projected = (existing_usage_bytes or 0) + staged_bytes if existing_usage_bytes is not None else None
    by_ext: dict[str, int] = {}
    for record in records:
        ext = str(record.get("extension", "")).lower().lstrip(".").upper() or "UNKNOWN"
        by_ext[ext] = by_ext.get(ext, 0) + 1
    lines = [
        "# Licensed audio ingestion summary", "",
        f"Supported files discovered: {len(records)}",
        f"Valid canonical files staged: {len(valid)}",
        f"Invalid or exact-duplicate files excluded from upload: {len(invalid)}",
        f"Metadata-review records: {sum(1 for record in records if record.get('needsReview'))}",
        f"Publishable records after validation: {len(publishable.get('tracks', []))}",
        f"Original bytes inspected: {original_bytes}",
        f"Streaming bytes staged: {staged_bytes}",
        f"Projected bucket bytes including supplied existing usage: {projected if projected is not None else 'unknown'}",
        "",
        "## Format breakdown", "",
    ]
    lines.extend(f"- {extension}: {count}" for extension, count in sorted(by_ext.items()))
    lines.extend([
        "", "## Processing", "",
        f"- Direct copies: {sum(1 for record in valid if record.get('streamingAction') != 'TRANSCODE_AAC_M4A')}",
        f"- Transcoded derivatives: {sum(1 for record in valid if record.get('streamingAction') == 'TRANSCODE_AAC_M4A')}",
        "- Loudness/mastering changes: none",
        "- Exact duplicate groups: " + str(len(duplicates["exactSha256"])),
        "- Probable title duplicate groups: " + str(len(duplicates["potentialTitle"])),
        "", "Rights are `LICENSED` only because the owner supplied that assertion; documentary proof was not independently reviewed.",
    ])
    return "\n".join(lines) + "\n"


def _quality_report(records: list[dict], duplicates: dict[str, list[list[str]]], rights_status: str, rights_evidence: str | None) -> str:
    invalid = [record for record in records if not record.get("valid")]
    review = [record for record in records if record.get("needsReview")]
    lines = [
        "# Audio Quality Report", "", f"Files inspected: {len(records)}",
        f"Valid: {len(records) - len(invalid)}", f"Invalid: {len(invalid)}",
        f"Needs metadata review: {len(review)}", "",
        f"Rights status: `{rights_status}`; evidence: `{rights_evidence or 'not supplied'}`.",
        "The tool does not normalize loudness or modify source masters.", "",
    ]
    if invalid:
        lines.extend(["## Invalid files", ""])
        lines.extend(f"- `{item['relativePath']}`: {'; '.join(item['errors'])}" for item in invalid)
        lines.append("")
    lines.extend(["## Duplicate summary", "", f"Exact SHA-256 groups: {len(duplicates['exactSha256'])}", f"Potential title groups: {len(duplicates['potentialTitle'])}", ""])
    if rights_status == "LICENSED" and rights_evidence == "USER_CONFIRMED":
        lines.extend(["The owner supplied this rights assertion; documentary license contracts were not independently reviewed.", ""])
    return "\n".join(lines)


def _validate(args: argparse.Namespace) -> int:
    manifest_path = Path(args.manifest).expanduser().resolve()
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    result = validate_manifest(manifest)
    for warning in result["warnings"]:
        print(f"WARNING: {warning}")
    for error in result["errors"]:
        print(f"ERROR: {error}")
    print(f"Validation: {len(result['errors'])} error(s), {len(result['warnings'])} warning(s)")
    return 1 if result["errors"] else 0


def _upload(args: argparse.Namespace) -> int:
    manifest = json.loads(Path(args.manifest).expanduser().resolve().read_text(encoding="utf-8"))
    plan = build_upload_plan(manifest, args.staging_dir)
    for item in plan:
        print(f"{item['localStagedFile']} -> {item['objectKey']} [{item['status']}]" + (f" ({item['reason']})" if item["reason"] else ""))
    catalog_item = None
    if args.catalog_file:
        catalog_path = Path(args.catalog_file).expanduser().resolve()
        catalog_key = str(args.catalog_key).strip().lstrip("/")
        if not catalog_path.is_file():
            print(f"Catalog file does not exist: {catalog_path}", file=sys.stderr)
            return 1
        if not catalog_key.startswith("catalog/") or "\\" in catalog_key or ".." in catalog_key or any(part == "" for part in catalog_key.split("/")):
            print(f"Catalog object key is unsafe: {catalog_key}", file=sys.stderr)
            return 1
        catalog_item = {"localStagedFile": str(catalog_path), "objectKey": catalog_key}
        print(f"{catalog_path} -> {catalog_key} [CATALOG]")
    if not args.upload:
        print("Dry run only. Pass --upload after rights and staging checks to perform an upload.")
        return 0
    ready = [item for item in plan if item["status"] == "READY"]
    if not ready:
        print("No rights-approved staged files are ready for upload.", file=sys.stderr)
        return 1
    bucket = args.bucket or os.environ.get("R2_BUCKET")
    if not bucket:
        print("R2_BUCKET is required for an upload.", file=sys.stderr)
        return 1
    if not args.base_url:
        print("--base-url is required for safe upload verification; no overwrite can be assumed.", file=sys.stderr)
        return 1
    uploader = _uploader_command()
    if uploader is None:
        print("No authenticated wrangler or AWS CLI was found; upload was not attempted.", file=sys.stderr)
        return 1
    for item in ready:
        existing = _existing_object(args.base_url, item["objectKey"], item["localStagedFile"])
        if existing == "SAME":
            print(f"Skipped existing matching object {item['objectKey']}")
            continue
        if existing == "CONFLICT" and not args.allow_overwrite:
            print(f"Upload blocked because object already exists with different metadata: {item['objectKey']}", file=sys.stderr)
            return 1
        command = uploader(bucket, item["objectKey"], item["localStagedFile"])
        result = subprocess.run(command, capture_output=True, text=True, check=False)
        if result.returncode != 0:
            print(f"Upload failed for {item['objectKey']}: {result.stderr.strip()}", file=sys.stderr)
            return 1
        print(f"Uploaded {item['objectKey']}; verifying HTTP response")
        _verify_url(args.base_url.rstrip("/") + "/" + item["objectKey"], item["localStagedFile"])
    if catalog_item:
        existing = _existing_object(args.base_url, catalog_item["objectKey"], catalog_item["localStagedFile"])
        if existing == "SAME":
            print(f"Skipped existing matching catalog {catalog_item['objectKey']}")
            return 0
        if existing == "CONFLICT" and not args.allow_overwrite:
            print(f"Catalog upload blocked because the object already exists with different metadata: {catalog_item['objectKey']}", file=sys.stderr)
            return 1
        command = uploader(bucket, catalog_item["objectKey"], catalog_item["localStagedFile"])
        result = subprocess.run(command, capture_output=True, text=True, check=False)
        if result.returncode != 0:
            print(f"Catalog upload failed for {catalog_item['objectKey']}: {result.stderr.strip()}", file=sys.stderr)
            return 1
        print(f"Uploaded {catalog_item['objectKey']}; verifying HTTP response")
        _verify_url(args.base_url.rstrip("/") + "/" + catalog_item["objectKey"], catalog_item["localStagedFile"])
    return 0


def _existing_object(base_url: str, object_key: str, local_path: str) -> str:
    """Return SAME, CONFLICT, or MISSING without downloading an existing object."""
    url = base_url.rstrip("/") + "/" + object_key
    try:
        response = urlopen(Request(url, method="HEAD"), timeout=20)
    except Exception as error:
        status = getattr(error, "code", None)
        if status == 404:
            return "MISSING"
        raise RuntimeError(f"could not verify existing R2 object {object_key}: {error}") from error
    remote_length = response.headers.get("Content-Length")
    local_size = os.path.getsize(local_path)
    etag = (response.headers.get("ETag") or "").strip().strip('"')
    if remote_length and int(remote_length) == local_size and etag:
        digestor = hashlib.md5()  # noqa: S324 - compare non-secret HTTP ETag only
        with Path(local_path).open("rb") as stream:
            while chunk := stream.read(1024 * 1024):
                digestor.update(chunk)
        digest = digestor.hexdigest()
        return "SAME" if etag.lower() == digest.lower() else "CONFLICT"
    return "CONFLICT"


def _uploader_command():
    if wrangler := shutil.which("wrangler"):
        return lambda bucket, key, path: [wrangler, "r2", "object", "put", f"{bucket}/{key}", "--file", path, "--content-type", mimetypes.guess_type(path)[0] or "application/octet-stream"]
    if aws := shutil.which("aws"):
        endpoint = os.environ.get("R2_ENDPOINT", "").strip()
        account_id = os.environ.get("R2_ACCOUNT_ID", "").strip()
        endpoint = endpoint or (f"https://{account_id}.r2.cloudflarestorage.com" if account_id else "")
        if not endpoint:
            return None
        return lambda bucket, key, path: [
            aws, "s3", "cp", path, f"s3://{bucket}/{key}",
            "--endpoint-url", endpoint,
            "--content-type", mimetypes.guess_type(path)[0] or "application/octet-stream",
        ]
    return None


def _verify_url(url: str, expected_path: str | None = None) -> None:
    expected_size = os.path.getsize(expected_path) if expected_path else None
    expected_type = mimetypes.guess_type(expected_path)[0] if expected_path else None
    try:
        response = urlopen(Request(url, method="HEAD"), timeout=20)
        content_type = (response.headers.get("Content-Type") or "").split(";", 1)[0].strip().lower()
        content_length = response.headers.get("Content-Length")
        if not 200 <= response.status < 300:
            raise RuntimeError(f"HTTP HEAD returned {response.status}")
        if expected_size is not None and content_length != str(expected_size):
            raise RuntimeError(f"HTTP Content-Length {content_length!r} does not match local size {expected_size}")
        if expected_type and content_type != expected_type:
            raise RuntimeError(f"HTTP Content-Type {content_type!r} does not match expected {expected_type!r}")
        if (response.headers.get("Accept-Ranges") or "").lower() != "bytes":
            raise RuntimeError("HTTP response does not advertise byte ranges")
        print(f"  HTTP {response.status}; Content-Type={response.headers.get('Content-Type')}; Content-Length={content_length}; Accept-Ranges={response.headers.get('Accept-Ranges')}")
        range_response = urlopen(Request(url, headers={"Range": "bytes=0-1"}), timeout=20)
        content_range = range_response.headers.get("Content-Range") or ""
        if range_response.status != 206 or not content_range.startswith("bytes 0-1/"):
            raise RuntimeError(f"range probe did not return bytes 0-1 (HTTP {range_response.status}, Content-Range={content_range!r})")
        if range_response.headers.get("Content-Length") != "2":
            raise RuntimeError("range probe did not return exactly two bytes")
        print(f"  Range probe HTTP {range_response.status}; Content-Range={content_range}")
    except RuntimeError:
        raise
    except Exception as error:  # pragma: no cover - network is intentionally optional
        raise RuntimeError(f"HTTP verification failed: {error}") from error


def _parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Read-only DivyaDhun audio inventory and R2 upload planner")
    subparsers = parser.add_subparsers(dest="command", required=True)
    scan = subparsers.add_parser("scan", help="inspect an explicit source directory")
    scan.add_argument("--source", required=True, help="explicit audio source directory")
    scan.add_argument("--output", default="build/audio-ingest", help="separate generated output directory")
    scan.add_argument("--base-url", help="single HTTPS R2/CDN base URL")
    scan.add_argument("--catalog-version", default="draft")
    scan.add_argument("--rights-status", default="UNKNOWN", help="explicit rights status for every scanned file")
    scan.add_argument("--rights-evidence", help="evidence label, for example USER_CONFIRMED")
    scan.add_argument("--existing-usage-bytes", type=int, help="known existing R2 bytes for a conservative projection")
    scan.add_argument("--stage", action="store_true", help="copy valid inputs to output/staged using normalized names")
    scan.set_defaults(function=_scan)
    validate = subparsers.add_parser("validate", help="validate a production manifest")
    validate.add_argument("--manifest", required=True)
    validate.set_defaults(function=_validate)
    upload = subparsers.add_parser("upload", help="dry-run or perform rights-gated upload")
    upload.add_argument("--manifest", required=True)
    upload.add_argument("--staging-dir", required=True)
    upload.add_argument("--base-url")
    upload.add_argument("--bucket")
    upload.add_argument("--upload", action="store_true", help="perform upload; omitted means dry-run")
    upload.add_argument("--allow-overwrite", action="store_true", help="allow replacing a conflicting existing object (use sparingly)")
    upload.add_argument("--catalog-file", help="optional generated catalog JSON to upload after audio objects")
    upload.add_argument("--catalog-key", default="catalog/latest.json", help="catalog object key (must remain under catalog/)")
    upload.set_defaults(function=_upload)
    return parser


def main(argv: list[str] | None = None) -> int:
    args = list(argv if argv is not None else sys.argv[1:])
    if args and args[0].startswith("-"):
        args.insert(0, "scan")
    try:
        parsed = _parser().parse_args(args)
        return parsed.function(parsed)
    except (OSError, ValueError, RuntimeError, json.JSONDecodeError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
