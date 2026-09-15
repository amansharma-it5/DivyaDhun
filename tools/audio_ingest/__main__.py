from __future__ import annotations

import argparse
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
    build_upload_plan,
    detect_duplicates,
    scan_directory,
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
    records = scan_directory(source)
    if args.stage:
        stage_valid_files(records, output / "staged")
    manifest = build_manifest(records, args.base_url, args.catalog_version)
    duplicates = detect_duplicates(records)
    output.mkdir(parents=True, exist_ok=True)
    write_csv(output / "AUDIO_INVENTORY.csv", records)
    write_csv(output / "AUDIO_DUPLICATES.csv", [
        {"duplicateType": kind, "files": " | ".join(paths)}
        for kind, groups in duplicates.items() for paths in groups
    ])
    _json_dump(output / "PRODUCTION_CONTENT_DRAFT.json", manifest)
    write_csv(output / "R2_UPLOAD_PLAN.csv", build_upload_plan(manifest, output / "staged"))
    quality = _quality_report(records, duplicates)
    (output / "AUDIO_QUALITY_REPORT.md").write_text(quality, encoding="utf-8")
    print(f"Scanned {len(records)} supported audio file(s) from {source}")
    print(f"Reports written under {output}")
    if not args.stage:
        print("No staged media was created; pass --stage to copy valid files into the output directory.")
    return 0


def _quality_report(records: list[dict], duplicates: dict[str, list[list[str]]]) -> str:
    invalid = [record for record in records if not record.get("valid")]
    review = [record for record in records if record.get("needsReview")]
    lines = [
        "# Audio Quality Report", "", f"Files inspected: {len(records)}",
        f"Valid: {len(records) - len(invalid)}", f"Invalid: {len(invalid)}",
        f"Needs metadata review: {len(review)}", "",
        "All rights statuses remain `UNKNOWN` unless an owner supplies explicit evidence.",
        "The tool does not normalize loudness or modify source masters.", "",
    ]
    if invalid:
        lines.extend(["## Invalid files", ""])
        lines.extend(f"- `{item['relativePath']}`: {'; '.join(item['errors'])}" for item in invalid)
        lines.append("")
    lines.extend(["## Duplicate summary", "", f"Exact SHA-256 groups: {len(duplicates['exactSha256'])}", f"Potential title groups: {len(duplicates['potentialTitle'])}", ""])
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
    uploader = _uploader_command()
    if uploader is None:
        print("No authenticated wrangler or AWS CLI was found; upload was not attempted.", file=sys.stderr)
        return 1
    for item in ready:
        command = uploader(bucket, item["objectKey"], item["localStagedFile"])
        result = subprocess.run(command, capture_output=True, text=True, check=False)
        if result.returncode != 0:
            print(f"Upload failed for {item['objectKey']}: {result.stderr.strip()}", file=sys.stderr)
            return 1
        print(f"Uploaded {item['objectKey']}; verifying HTTP response")
        if args.base_url:
            _verify_url(args.base_url.rstrip("/") + "/" + item["objectKey"])
    return 0


def _uploader_command():
    if wrangler := shutil.which("wrangler"):
        return lambda bucket, key, path: [wrangler, "r2", "object", "put", f"{bucket}/{key}", "--file", path]
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


def _verify_url(url: str) -> None:
    try:
        response = urlopen(Request(url, method="HEAD"), timeout=20)
        print(f"  HTTP {response.status}; Content-Type={response.headers.get('Content-Type')}; Content-Length={response.headers.get('Content-Length')}; Accept-Ranges={response.headers.get('Accept-Ranges')}")
        range_response = urlopen(Request(url, headers={"Range": "bytes=0-1"}), timeout=20)
        print(f"  Range probe HTTP {range_response.status}; Content-Range={range_response.headers.get('Content-Range')}")
    except Exception as error:  # pragma: no cover - network is intentionally optional
        print(f"  verification failed: {error}")


def _parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Read-only DivyaDhun audio inventory and R2 upload planner")
    subparsers = parser.add_subparsers(dest="command", required=True)
    scan = subparsers.add_parser("scan", help="inspect an explicit source directory")
    scan.add_argument("--source", required=True, help="explicit audio source directory")
    scan.add_argument("--output", default="build/audio-ingest", help="separate generated output directory")
    scan.add_argument("--base-url", help="single HTTPS R2/CDN base URL")
    scan.add_argument("--catalog-version", default="draft")
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
    upload.set_defaults(function=_upload)
    return parser


def main(argv: list[str] | None = None) -> int:
    args = list(argv if argv is not None else sys.argv[1:])
    if args and args[0].startswith("-"):
        args.insert(0, "scan")
    try:
        parsed = _parser().parse_args(args)
        return parsed.function(parsed)
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
