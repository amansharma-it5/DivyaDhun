from __future__ import annotations

import csv
import hashlib
import json
import os
import re
import shutil
import subprocess
import unicodedata
import wave
from collections import defaultdict
from pathlib import Path
from typing import Any, Iterable
from urllib.parse import urljoin, urlparse

SUPPORTED_EXTENSIONS = {".mp3", ".m4a", ".aac", ".wav", ".flac"}
SUPPORTED_CATEGORIES = {"AARTI", "BHAJAN", "MANTRA", "CHALISA", "STOTRA", "MEDITATION"}
APPROVED_RIGHTS = {"OWNER_CREATED", "LICENSED", "PUBLIC_DOMAIN_RECORDING"}
RIGHTS_STATUSES = APPROVED_RIGHTS | {"UNKNOWN", "NOT_APPROVED"}
KNOWN_DEITIES = {
    "hanuman", "shiv", "krishna", "ram", "ganesh", "durga", "lakshmi", "sai",
    "saraswati", "vishnu", "kali", "shani",
}
_DEITY_ALIASES = {
    "hanuman": ("hanuman", "bajrang", "bajrangbali", "pawanputra", "हनुमान"),
    "shiv": ("shiv", "shiva", "mahadev", "bholenath", "शिव", "महादेव"),
    "krishna": ("krishna", "kanha", "kanhaiya", "gopal", "कृष्ण"),
    "ram": ("ram", "rama", "shriram", "shri-ram", "राम"),
    "ganesh": ("ganesh", "ganapati", "विनायक", "गणेश"),
    "durga": ("durga", "ambe", "jagdambe", "दुर्गा"),
    "lakshmi": ("lakshmi", "mahalakshmi", "लक्ष्मी"),
    "sai": ("sai", "saibaba", "sai-baba", "साईं"),
    "saraswati": ("saraswati", "सरस्वती"),
    "vishnu": ("vishnu", "विष्णु"),
    "kali": ("kali", "काली"),
    "shani": ("shani", "शनिदेव", "शनि"),
}
_CATEGORY_ALIASES = {
    "AARTI": ("aarti", "आरती"),
    "BHAJAN": ("bhajan", "भजन"),
    "MANTRA": ("mantra", "मंत्र", "dhun"),
    "CHALISA": ("chalisa", "चालीसा"),
    "STOTRA": ("stotra", "स्तोत्र", "stotram"),
    "MEDITATION": ("meditation", "ध्यान"),
}


def slugify(value: str) -> str:
    """Create a deterministic lowercase ASCII slug suitable for an object key."""
    normalized = unicodedata.normalize("NFKD", value).encode("ascii", "ignore").decode("ascii")
    slug = re.sub(r"[^a-zA-Z0-9]+", "-", normalized.lower()).strip("-")
    return slug or "untitled"


def normalize_base_url(value: str | None) -> str | None:
    if value is None or not value.strip():
        return None
    candidate = value.strip()
    parsed = urlparse(candidate)
    if parsed.scheme.lower() != "https" or not parsed.netloc:
        raise ValueError("remote base URL must use HTTPS")
    return candidate.rstrip("/") + "/"


def build_audio_url(base_url: str | None, object_key: str) -> str | None:
    base = normalize_base_url(base_url)
    key = object_key.strip().lstrip("/")
    if not base or not key:
        return None
    return urljoin(base, key)


def sha256_file(path: Path, chunk_size: int = 1024 * 1024) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        while chunk := stream.read(chunk_size):
            digest.update(chunk)
    return digest.hexdigest()


def _find_ffprobe() -> str | None:
    return shutil.which("ffprobe")


def _probe_with_ffprobe(path: Path, executable: str) -> tuple[dict[str, Any], list[str]]:
    command = [
        executable, "-v", "error", "-print_format", "json",
        "-show_format", "-show_streams", str(path),
    ]
    try:
        result = subprocess.run(command, capture_output=True, text=True, timeout=30, check=False)
    except (OSError, subprocess.TimeoutExpired) as error:
        return {}, [f"ffprobe unavailable: {error}"]
    if result.returncode != 0:
        detail = result.stderr.strip() or "ffprobe rejected the media container"
        return {}, [detail]
    try:
        return json.loads(result.stdout), []
    except json.JSONDecodeError:
        return {}, ["ffprobe returned invalid JSON"]


def _probe_wav(path: Path) -> tuple[dict[str, Any], list[str]]:
    try:
        with wave.open(str(path), "rb") as stream:
            frames = stream.getnframes()
            rate = stream.getframerate()
            duration = frames / rate if rate else 0
            return {
                "duration": duration,
                "codec_name": "pcm",
                "sample_rate": str(rate),
                "channels": stream.getnchannels(),
                "bit_rate": stream.getsampwidth() * 8 * rate * stream.getnchannels(),
            }, []
    except (OSError, EOFError, wave.Error) as error:
        return {}, [f"unreadable WAV: {error}"]


def _metadata_for_probe(probe: dict[str, Any], path: Path) -> dict[str, Any]:
    streams = [item for item in probe.get("streams", []) if item.get("codec_type") == "audio"]
    stream = streams[0] if streams else {}
    fmt = probe.get("format", {})
    tags = {str(key).lower(): str(value).strip() for key, value in fmt.get("tags", {}).items()}
    duration = stream.get("duration", fmt.get("duration"))
    try:
        duration_ms = round(float(duration) * 1000) if duration is not None else None
    except (TypeError, ValueError):
        duration_ms = None
    return {
        "durationMs": duration_ms,
        "codec": stream.get("codec_name"),
        "bitrate": _int_or_none(stream.get("bit_rate", fmt.get("bit_rate"))),
        "sampleRate": _int_or_none(stream.get("sample_rate")),
        "channels": _int_or_none(stream.get("channels")),
        "title": tags.get("title") or path.stem,
        "artist": tags.get("artist") or tags.get("album_artist"),
        "album": tags.get("album"),
    }


def _int_or_none(value: Any) -> int | None:
    try:
        return int(float(value)) if value is not None else None
    except (TypeError, ValueError):
        return None


def infer_metadata(relative_path: str, title: str) -> dict[str, Any]:
    searchable = f"{relative_path} {title}".lower()
    deity_id = next((deity for deity, aliases in _DEITY_ALIASES.items() if any(alias.lower() in searchable for alias in aliases)), None)
    category = next((category for category, aliases in _CATEGORY_ALIASES.items() if any(alias.lower() in searchable for alias in aliases)), None)
    return {
        "deityId": deity_id,
        "category": category,
        "needsReview": deity_id is None or category is None,
    }


def _safe_relative(path: Path, source: Path) -> str:
    return path.relative_to(source).as_posix()


def _object_key(deity_id: str | None, title: str, extension: str) -> str:
    deity = slugify(deity_id or "unknown")
    return f"audio/{deity}/{slugify(title)}{extension.lower()}"


def scan_directory(source: str | Path, ffprobe: str | None = None) -> list[dict[str, Any]]:
    """Read an explicit source directory without writing to it or scanning elsewhere."""
    root = Path(source).expanduser().resolve()
    if not root.is_dir():
        raise ValueError(f"source directory does not exist: {root}")
    executable = ffprobe or _find_ffprobe()
    records: list[dict[str, Any]] = []
    for path in sorted((item for item in root.rglob("*") if item.is_file()), key=lambda item: str(item).lower()):
        extension = path.suffix.lower()
        if extension not in SUPPORTED_EXTENSIONS:
            continue
        relative = _safe_relative(path, root)
        size = path.stat().st_size
        errors: list[str] = []
        warnings: list[str] = []
        if size == 0:
            errors.append("zero-byte file")
        if executable and not errors:
            probe, probe_errors = _probe_with_ffprobe(path, executable)
            errors.extend(probe_errors)
            media = _metadata_for_probe(probe, path) if probe else {}
        elif extension == ".wav" and not errors:
            media, probe_errors = _probe_wav(path)
            errors.extend(probe_errors)
        else:
            media = {}
            if not executable:
                warnings.append("ffprobe not found; codec metadata is unavailable")
        media.setdefault("title", path.stem)
        if media.get("durationMs") is not None and media["durationMs"] <= 0:
            errors.append("duration is zero or negative")
        inferred = infer_metadata(relative, media["title"])
        object_key = _object_key(inferred["deityId"], media["title"], extension)
        records.append({
            "originalFilename": path.name,
            "relativePath": relative,
            "sourcePath": str(path),
            "extension": extension,
            "fileSizeBytes": size,
            "sha256": sha256_file(path),
            "durationMs": media.get("durationMs"),
            "codec": media.get("codec"),
            "bitrate": media.get("bitrate"),
            "sampleRate": media.get("sampleRate"),
            "channels": media.get("channels"),
            "metadataTitle": media.get("title"),
            "metadataArtist": media.get("artist"),
            "metadataAlbum": media.get("album"),
            "deityId": inferred["deityId"],
            "category": inferred["category"],
            "objectKey": object_key,
            "valid": not errors,
            "errors": errors,
            "warnings": warnings,
            "needsReview": inferred["needsReview"] or not media.get("artist"),
        })
    return records


def detect_duplicates(records: Iterable[dict[str, Any]]) -> dict[str, list[list[str]]]:
    exact: dict[str, list[str]] = defaultdict(list)
    names: dict[str, list[str]] = defaultdict(list)
    for record in records:
        identifier = record.get("relativePath") or record.get("originalFilename") or record.get("id", "")
        digest = record.get("sha256")
        if digest:
            exact[digest].append(identifier)
        title = (record.get("metadataTitle") or record.get("titleEnglish") or "").strip().lower()
        if title:
            names[title].append(identifier)
    return {
        "exactSha256": [paths for paths in exact.values() if len(paths) > 1],
        "potentialTitle": [paths for paths in names.values() if len(paths) > 1],
    }


def build_manifest(records: Iterable[dict[str, Any]], base_url: str | None = None, catalog_version: str = "draft") -> dict[str, Any]:
    normalized_base = normalize_base_url(base_url)
    tracks: list[dict[str, Any]] = []
    for record in records:
        title = str(record.get("metadataTitle") or Path(str(record.get("originalFilename", "untitled"))).stem).strip()
        deity_id = record.get("deityId")
        category = record.get("category")
        track_id = f"{deity_id or 'unknown'}_{slugify(title).replace('-', '_')}"
        object_key = record.get("objectKey") or _object_key(deity_id, title, record.get("extension", ".m4a"))
        tracks.append({
            "id": track_id,
            "titleEnglish": title,
            "titleHindi": "",
            "deityId": deity_id or "",
            "category": category or "",
            "artist": record.get("metadataArtist") or "",
            "source": "",
            "language": "HINDI",
            "durationMs": record.get("durationMs"),
            "audioObjectKey": object_key,
            "audioUrl": build_audio_url(normalized_base, object_key),
            "artworkUrl": None,
            "lyricsId": None,
            "rightsStatus": record.get("rightsStatus", "UNKNOWN"),
            "licenseReference": record.get("licenseReference"),
            "attribution": record.get("attribution") or record.get("metadataArtist"),
            "sha256": record.get("sha256"),
            "availability": "STAGED" if record.get("valid", True) else "UNAVAILABLE",
            "needsReview": bool(record.get("needsReview", True)),
            "originalFilename": record.get("originalFilename"),
        })
    return {"schemaVersion": 1, "catalogVersion": catalog_version, "tracks": tracks}


def validate_manifest(manifest: dict[str, Any], known_deities: set[str] | None = None) -> dict[str, list[str]]:
    errors: list[str] = []
    warnings: list[str] = []
    known = known_deities or KNOWN_DEITIES
    tracks = manifest.get("tracks")
    if not isinstance(tracks, list):
        return {"errors": ["tracks must be an array"], "warnings": []}
    ids: set[str] = set()
    keys: set[str] = set()
    required = ("id", "titleEnglish", "deityId", "category", "rightsStatus", "audioObjectKey")
    for index, track in enumerate(tracks):
        prefix = f"tracks[{index}]"
        if not isinstance(track, dict):
            errors.append(f"{prefix} must be an object")
            continue
        for field in required:
            if not str(track.get(field, "")).strip():
                errors.append(f"{prefix}.{field} is required")
        track_id = str(track.get("id", ""))
        if track_id in ids:
            errors.append(f"duplicate track id: {track_id}")
        ids.add(track_id)
        key = str(track.get("audioObjectKey", ""))
        if key in keys and key:
            errors.append(f"duplicate object key: {key}")
        keys.add(key)
        if key and (not key.startswith("audio/") or "\\" in key or ".." in key or any(part == "" for part in key.split("/"))):
            errors.append(f"unsafe audio object key: {key}")
        deity = str(track.get("deityId", ""))
        if deity and deity not in known:
            errors.append(f"unknown deity id: {deity}")
        category = str(track.get("category", "")).upper()
        if category and category not in SUPPORTED_CATEGORIES:
            errors.append(f"unsupported category: {category}")
        rights = str(track.get("rightsStatus", "")).upper()
        if rights not in RIGHTS_STATUSES:
            errors.append(f"invalid or missing rights status: {rights or '<blank>'}")
        duration = track.get("durationMs")
        if duration is not None:
            try:
                if int(duration) <= 0:
                    errors.append(f"{prefix}.durationMs must be positive")
            except (TypeError, ValueError):
                errors.append(f"{prefix}.durationMs must be an integer")
        url = track.get("audioUrl")
        if url:
            parsed = urlparse(str(url))
            if parsed.scheme.lower() != "https" or not parsed.netloc:
                errors.append(f"{prefix}.audioUrl must be an HTTPS URL")
        elif rights in APPROVED_RIGHTS:
            errors.append(f"{prefix}.audioUrl is required for approved rights")
        if rights == "UNKNOWN":
            warnings.append(f"{prefix} remains RIGHTS_UNKNOWN and cannot be published")
        if track.get("needsReview"):
            warnings.append(f"{prefix} needs metadata review")
    return {"errors": errors, "warnings": warnings}


def approved_tracks(manifest: dict[str, Any]) -> list[dict[str, Any]]:
    return [track for track in manifest.get("tracks", []) if str(track.get("rightsStatus", "")).upper() in APPROVED_RIGHTS]


def build_upload_plan(manifest: dict[str, Any], staging_dir: str | Path) -> list[dict[str, Any]]:
    root = Path(staging_dir).expanduser().resolve()
    plan: list[dict[str, Any]] = []
    for track in manifest.get("tracks", []):
        key = str(track.get("audioObjectKey", ""))
        staged = (root / key).resolve() if key else root
        rights = str(track.get("rightsStatus", "UNKNOWN")).upper()
        status = "READY"
        reason = ""
        if rights not in APPROVED_RIGHTS:
            status, reason = "BLOCKED_RIGHTS", f"rightsStatus={rights}"
        elif not staged.is_file():
            status, reason = "MISSING_STAGED_FILE", "staged file does not exist"
        plan.append({"id": track.get("id"), "localStagedFile": str(staged), "objectKey": key, "status": status, "reason": reason})
    return plan


def write_csv(path: Path, rows: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    fields = sorted({key for row in rows for key in row}) if rows else ["status"]
    with path.open("w", newline="", encoding="utf-8") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def stage_valid_files(records: Iterable[dict[str, Any]], output_dir: str | Path) -> list[dict[str, Any]]:
    destination = Path(output_dir).expanduser().resolve()
    staged: list[dict[str, Any]] = []
    for record in records:
        if not record.get("valid"):
            continue
        source = Path(str(record["sourcePath"])).resolve()
        target = (destination / str(record["objectKey"])).resolve()
        target.parent.mkdir(parents=True, exist_ok=True)
        if source == target or source in target.parents:
            raise ValueError("staging output must not be inside the source file")
        shutil.copy2(source, target)
        staged.append({"sourcePath": str(source), "stagedPath": str(target), "objectKey": record["objectKey"]})
    return staged
