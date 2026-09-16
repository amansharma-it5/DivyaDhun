"""Build an auditable metadata-recovery report for blocked local recordings.

This tool never changes source audio, stages media, assigns unresolved metadata,
or publishes anything. It records deterministic local evidence and leaves human
and web-research decisions explicitly unresolved until supported by evidence.
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path
from typing import Any

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from audio_ingest.pipeline import _CATEGORY_ALIASES, _DEITY_ALIASES


PLACEHOLDER_ARTISTS = {"", "licensed recording", "unknown artist", "various artists"}


def _json(value: Any) -> str:
    return json.dumps(value, ensure_ascii=False, separators=(",", ":"))


def _sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        while chunk := stream.read(1024 * 1024):
            digest.update(chunk)
    return digest.hexdigest()


def _ffprobe(path: Path, executable: str | None) -> dict[str, Any]:
    if not executable or not path.is_file() or path.stat().st_size == 0:
        return {"status": "ZERO_BYTE_SOURCE" if path.is_file() and path.stat().st_size == 0 else "SOURCE_MISSING"}
    command = [executable, "-v", "error", "-print_format", "json", "-show_format", "-show_streams", str(path)]
    try:
        result = subprocess.run(command, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=30, check=False)
    except (OSError, subprocess.TimeoutExpired) as error:
        return {"status": "FFPROBE_ERROR", "error": str(error)}
    if result.returncode != 0:
        return {"status": "FFPROBE_ERROR", "error": result.stderr.strip() or "ffprobe rejected the file"}
    try:
        payload = json.loads(result.stdout)
    except json.JSONDecodeError:
        return {"status": "FFPROBE_ERROR", "error": "ffprobe returned invalid JSON"}
    streams = [item for item in payload.get("streams", []) if item.get("codec_type") == "audio"]
    stream = streams[0] if streams else {}
    fmt = payload.get("format", {})
    tags = {str(k).lower(): str(v).strip() for k, v in fmt.get("tags", {}).items()}
    duration = stream.get("duration", fmt.get("duration"))
    try:
        duration_ms = round(float(duration) * 1000) if duration is not None else None
    except (TypeError, ValueError):
        duration_ms = None
    return {
        "status": "FFPROBE_OK" if streams else "NO_AUDIO_STREAM",
        "durationMs": duration_ms,
        "codec": stream.get("codec_name"),
        "bitrate": stream.get("bit_rate") or fmt.get("bit_rate"),
        "sampleRate": stream.get("sample_rate"),
        "channels": stream.get("channels"),
        "title": tags.get("title"),
        "artist": tags.get("artist") or tags.get("album_artist"),
        "album": tags.get("album"),
        "format": fmt.get("format_name"),
        "formatDuration": fmt.get("duration"),
    }


def _signals(text: str, aliases: dict[str, tuple[str, ...]]) -> list[str]:
    searchable = text.lower()
    return [key for key, values in aliases.items() if any(value.lower() in searchable for value in values)]


def _category_candidates(text: str) -> list[str]:
    candidates = _signals(text, _CATEGORY_ALIASES)
    if not candidates and re.search(r"\b(amritwani|amritvani|vandana|bhajan)\b", text, re.I):
        candidates = ["BHAJAN"]
    if not candidates and re.search(r"\bdhyan\b", text, re.I):
        candidates = ["MEDITATION"]
    return sorted(set(candidates))


def _missing_fields(row: dict[str, str]) -> list[str]:
    values = {
        "deityId": row.get("deityId", "").strip(),
        "category": row.get("category", "").strip(),
        "durationMs": row.get("durationMs", "").strip(),
        "artist": row.get("metadataArtist", "").strip(),
    }
    fields = [key for key, value in values.items() if not value or (key == "artist" and value.lower() in PLACEHOLDER_ARTISTS)]
    if row.get("valid", "").lower() != "true":
        fields.append("media")
    return fields


def _research_query(row: dict[str, str]) -> str:
    title = row.get("metadataTitle") or Path(row.get("sourcePath", "")).stem
    artist = row.get("metadataArtist", "").strip()
    if artist and artist.lower() not in PLACEHOLDER_ARTISTS:
        return f'"{title}" "{artist}" devotional song'
    return f'"{title}" devotional song'


def _action(field: str, row: dict[str, str], candidates: list[str]) -> str:
    if field == "deityId":
        return f"Review exact recording evidence; candidate signal(s): {', '.join(candidates) if candidates else 'none'}. Do not assign until confirmed."
    if field == "category":
        return f"Review supported category; deterministic title signal(s): {', '.join(candidates) if candidates else 'none'}. Do not assign until confirmed."
    if field == "artist":
        return "Confirm artist/licensor from source tags, license, or owner evidence; placeholder is not a publishable artist."
    if field == "durationMs":
        return "Re-probe a repaired non-empty source file; no duration is inferred from filename or bitrate."
    return "Repair or document the source-media problem before any staging decision."


def build_report(inventory_path: Path, output_dir: Path, ffprobe: str | None, evidence_path: Path | None = None) -> list[dict[str, Any]]:
    with inventory_path.open(newline="", encoding="utf-8-sig") as stream:
        rows = [dict(row) for row in csv.DictReader(stream)]
    blocked = [row for row in rows if row.get("needsReview", "").lower() == "true" or row.get("valid", "").lower() != "true" or row.get("duplicateOf")]
    output_dir.mkdir(parents=True, exist_ok=True)
    report: list[dict[str, Any]] = []
    for row in blocked:
        source = Path(row.get("sourcePath", ""))
        title_text = " ".join(filter(None, [row.get("metadataTitle", ""), row.get("metadataAlbum", ""), row.get("relativePath", "")]))
        deity_candidates = _signals(title_text, _DEITY_ALIASES)
        category_candidates = _category_candidates(title_text)
        probe = _ffprobe(source, ffprobe)
        missing = _missing_fields(row)
        source_exists = source.is_file()
        observed_size = source.stat().st_size if source_exists else None
        record = {
            "trackId": row.get("trackId", ""),
            "sourceFilename": row.get("originalFilename", ""),
            "relativePath": row.get("relativePath", ""),
            "sourcePath": str(source),
            "sourceExists": source_exists,
            "sha256": _sha256(source) if source_exists else "",
            "fileSizeBytes": observed_size,
            "inventorySha256": row.get("sha256", ""),
            "codec": row.get("codec", ""),
            "bitrate": row.get("bitrate", ""),
            "sampleRate": row.get("sampleRate", ""),
            "channels": row.get("channels", ""),
            "durationMs": row.get("durationMs", ""),
            "embeddedArtwork": row.get("embeddedArtwork", ""),
            "metadataTitle": row.get("metadataTitle", ""),
            "metadataArtist": row.get("metadataArtist", ""),
            "metadataAlbum": row.get("metadataAlbum", ""),
            "inferredDeityId": row.get("deityId", ""),
            "inferredCategory": row.get("category", ""),
            "missingFields": missing,
            "currentBlockers": [item for item in [row.get("errors", ""), row.get("warnings", ""), "metadata needs review" if row.get("needsReview", "").lower() == "true" else "", "exact duplicate" if row.get("duplicateOf") else ""] if item],
            "duplicateOf": row.get("duplicateOf", ""),
            "audioValidation": probe,
            "audioListeningStatus": "AUDIO_LISTENING_NOT_AVAILABLE",
            "deterministicDeityCandidates": deity_candidates,
            "deterministicCategoryCandidates": category_candidates,
            "deterministicActions": {field: _action(field, row, deity_candidates if field == "deityId" else category_candidates) for field in missing},
            "researchStatus": "PENDING_WEB_REVIEW",
            "researchQuery": _research_query(row),
            "researchSources": [],
            "researchFinding": "",
            "researchCandidateDeityId": "",
            "researchCandidateCategory": "",
            "researchCandidateArtist": "",
            "researchAlbum": "",
            "researchDurationMs": "",
            "researchConfidence": "UNRESOLVED",
            "researchNotes": "No metadata assignment made. Exact recording identity, artist, deity, and category require evidence; source audio is not uploaded to any recognition service.",
            "rightsStatus": "LICENSED",
            "rightsEvidence": "USER_CONFIRMED",
            "publicationStatus": "BLOCKED_REVIEW",
        }
        report.append(record)

    evidence: dict[str, dict[str, Any]] = {}
    if evidence_path and evidence_path.is_file():
        evidence = json.loads(evidence_path.read_text(encoding="utf-8"))
    for item in report:
        item.update(evidence.get("__default", {}))
        item.update(evidence.get(item["trackId"], {}))
    report.sort(key=lambda item: item["trackId"])
    (output_dir / "IDENTIFICATION_REPORT.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    fields = list(report[0]) if report else []
    with (output_dir / "IDENTIFICATION_REPORT.csv").open("w", newline="", encoding="utf-8") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields)
        writer.writeheader()
        for item in report:
            writer.writerow({key: _json(value) if isinstance(value, (list, dict)) else value for key, value in item.items()})
    _write_markdown(report, output_dir / "IDENTIFICATION_REPORT.md")
    return report


def _write_markdown(report: list[dict[str, Any]], path: Path) -> None:
    counts: dict[str, int] = {}
    for item in report:
        for field in item["missingFields"]:
            counts[field] = counts.get(field, 0) + 1
    lines = [
        "# Phase 19 blocked-track identification report",
        "",
        f"Generated records: **{len(report)}**",
        "",
        "Every row remains `BLOCKED_REVIEW`. Rights are recorded as `LICENSED — USER_CONFIRMED`; documentary agreements were not independently reviewed. Local technical inspection uses ffprobe and SHA-256 only. `AUDIO_LISTENING_NOT_AVAILABLE` means no human listening decision was made, and no audio was sent to a recognition service. Web matches are suggestions for human review, not automatic catalog assignments.",
        "",
        "## Missing-field counts",
        "",
    ]
    lines.extend(f"- `{field}`: {count}" for field, count in sorted(counts.items()))
    lines.extend(["", "## Per-track evidence", "", "| # | Track ID | Source | Missing fields | Local audio | Deterministic suggestions | Research |", "| ---: | --- | --- | --- | --- | --- | --- |"])
    for index, item in enumerate(report, 1):
        probe = item["audioValidation"]
        local = f"{probe.get('status')}; {item.get('durationMs') or 'duration unknown'} ms; {item.get('codec') or 'codec unknown'}"
        suggestions = []
        if item["deterministicDeityCandidates"]:
            suggestions.append("deity=" + ",".join(item["deterministicDeityCandidates"]))
        if item["deterministicCategoryCandidates"]:
            suggestions.append("category=" + ",".join(item["deterministicCategoryCandidates"]))
        if not suggestions:
            suggestions.append("none")
        sources = ", ".join(f"[{i + 1}]({url})" for i, url in enumerate(item["researchSources"])) or "none recorded"
        finding = item.get("researchFinding") or "no conclusive web match"
        lines.append(f"| {index} | `{item['trackId']}` | `{item['sourceFilename']}` | `{', '.join(item['missingFields']) or 'none identified'}` | `{local}` | `{'; '.join(suggestions)}` | `{item['researchStatus']}: {finding}` ({sources}) |")
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--inventory", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--ffprobe", default=shutil.which("ffprobe"))
    parser.add_argument("--evidence", type=Path)
    args = parser.parse_args()
    report = build_report(args.inventory.resolve(), args.output_dir.resolve(), args.ffprobe, args.evidence.resolve() if args.evidence else None)
    print(f"Wrote {len(report)} blocked-track records to {args.output_dir.resolve()}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
