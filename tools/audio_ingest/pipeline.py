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
    "hanuman": ("hanuman", "bajrang", "bajrangbali", "pawanputra", "salasar", "हनुमान"),
    "shiv": ("shiv", "shiva", "mahadev", "bholenath", "bhole", "shankar", "kanwar", "कांवड़", "शिव", "महादेव"),
    "krishna": ("krishna", "kanha", "kanhaiya", "gopal", "govind", "madhav", "bihari", "sanware", "कृष्ण"),
    "ram": ("ram", "rama", "shriram", "shri-ram", "ramayan", "raghunath", "राम"),
    "ganesh": ("ganesh", "ganapati", "ganesha", "vinayak", "विनायक", "गणेश"),
    "durga": ("durga", "ambe", "ambey", "jagdambe", "sherawali", "navratri", "जगदम्बे", "दुर्गा"),
    "lakshmi": ("lakshmi", "laxmi", "mahalakshmi", "लक्ष्मी"),
    "sai": ("sai", "saibaba", "sai-baba", "साईं"),
    "saraswati": ("saraswati", "सरस्वती"),
    "vishnu": ("vishnu", "विष्णु"),
    "kali": ("kali", "काली"),
    "shani": ("shani", "शनिदेव", "शनि"),
}
_CATEGORY_ALIASES = {
    "AARTI": ("aarti", "आरती"),
    "BHAJAN": ("bhajan", "भजन"),
    "MANTRA": ("mantra", "मंत्र", "dhun", "jaap", "jap", "chant", "chanting"),
    "CHALISA": ("chalisa", "चालीसा"),
    "STOTRA": ("stotra", "स्तोत्र", "stotram"),
    "MEDITATION": ("meditation", "ध्यान"),
}

_SITE_SUFFIX = re.compile(r"(?:\s*[-–—]\s*|\s*\()?(?:riskyjatt|koshalworld|djpunjab|pagalworld|gaana|\.com)[^)]*\)?\s*$", re.IGNORECASE)
_NUMERIC_PREFIX = re.compile(r"^\s*\d+[\s._-]*")


def clean_display_text(value: str | None, fallback: str = "") -> str:
    """Remove obvious download-site noise without changing the source file."""
    text = str(value or "").replace("_", " ").replace("\\", " ").strip()
    text = _SITE_SUFFIX.sub("", text)
    text = _NUMERIC_PREFIX.sub("", text)
    text = re.sub(r"\s{2,}", " ", text).strip(" -_()")
    return text or fallback


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
        "trackNumber": tags.get("tracknumber") or tags.get("track"),
        "embeddedArtwork": any(
            item.get("codec_type") == "video" and item.get("disposition", {}).get("attached_pic") == 1
            for item in probe.get("streams", [])
        ),
    }


def _int_or_none(value: Any) -> int | None:
    try:
        return int(float(value)) if value is not None else None
    except (TypeError, ValueError):
        return None


def infer_metadata(relative_path: str, title: str, album: str | None = None) -> dict[str, Any]:
    searchable = f"{relative_path} {title} {album or ''}".lower()
    deity_id = next((deity for deity, aliases in _DEITY_ALIASES.items() if any(alias.lower() in searchable for alias in aliases)), None)
    category = next((category for category, aliases in _CATEGORY_ALIASES.items() if any(alias.lower() in searchable for alias in aliases)), None)
    if category is None and any(token in searchable for token in ("amritwani", "amritvani", "vandana", "katha", "bhajan")):
        category = "BHAJAN"
    if category is None and "dhyan" in searchable:
        category = "MEDITATION"
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


def scan_directory(source: str | Path, ffprobe: str | None = None, rights_status: str = "UNKNOWN", rights_evidence: str | None = None) -> list[dict[str, Any]]:
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
            unavailable = [detail for detail in probe_errors if detail.lower().startswith("ffprobe unavailable")]
            warnings.extend(unavailable)
            errors.extend(detail for detail in probe_errors if detail not in unavailable)
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
        elif media.get("durationMs") is None and not errors:
            warnings.append("duration metadata is unavailable; review before publishing")
        clean_title = clean_display_text(media["title"], path.stem)
        clean_artist = clean_display_text(media.get("artist"), "Licensed recording")
        clean_album = clean_display_text(media.get("album"))
        inferred = infer_metadata(relative, clean_title, clean_album)
        object_key = _object_key(inferred["deityId"], clean_title, extension)
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
            "metadataTitle": clean_title,
            "metadataArtist": clean_artist,
            "metadataAlbum": clean_album,
            "trackNumber": media.get("trackNumber"),
            "embeddedArtwork": bool(media.get("embeddedArtwork", False)),
            "deityId": inferred["deityId"],
            "category": inferred["category"],
            "objectKey": object_key,
            "valid": not errors,
            "errors": errors,
            "warnings": warnings,
            "needsReview": inferred["needsReview"] or clean_artist == "Licensed recording" or media.get("durationMs") is None,
            "rightsStatus": rights_status.upper(),
            "rightsEvidence": rights_evidence,
        })
    _assign_duplicate_and_collision_metadata(records)
    return records


def _assign_duplicate_and_collision_metadata(records: list[dict[str, Any]]) -> None:
    """Mark exact duplicates and make object keys/IDs deterministic without deleting files."""
    by_hash: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for record in records:
        if record.get("valid") and record.get("sha256"):
            by_hash[str(record["sha256"])].append(record)
    for group in by_hash.values():
        group.sort(key=lambda item: str(item.get("relativePath", "")).lower())
        canonical = group[0]
        for duplicate in group[1:]:
            duplicate["duplicateOf"] = canonical.get("relativePath")
            duplicate["valid"] = False
            duplicate.setdefault("errors", []).append("exact duplicate of canonical source")
            duplicate["needsReview"] = True
    key_counts: dict[str, int] = defaultdict(int)
    id_counts: dict[str, int] = defaultdict(int)
    for record in records:
        base_key = str(record.get("objectKey", ""))
        stem, extension = os.path.splitext(base_key)
        key_counts[base_key] += 1
        if key_counts[base_key] > 1:
            suffix = str(record.get("sha256", ""))[:8] or str(key_counts[base_key])
            record["objectKey"] = f"{stem}-{suffix}-{key_counts[base_key]}{extension}"
        base_id = f"{record.get('deityId') or 'unknown'}_{slugify(str(record.get('metadataTitle') or 'untitled')).replace('-', '_')}"
        id_counts[base_id] += 1
        record["trackId"] = base_id if id_counts[base_id] == 1 else f"{base_id}_{str(record.get('sha256', ''))[:8] or 'copy'}_{id_counts[base_id]}"


def prepare_streaming_records(records: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Choose a lossless copy or an AAC derivative without modifying source masters."""
    for record in records:
        extension = str(record.get("extension", "")).lower()
        bitrate = record.get("bitrate")
        should_transcode = extension in {".wav", ".flac"} or (isinstance(bitrate, int) and bitrate > 192_000)
        record["streamingAction"] = "TRANSCODE_AAC_M4A" if should_transcode else "COPY_ORIGINAL"
        if should_transcode:
            key = str(record.get("objectKey", ""))
            record["objectKey"] = f"{os.path.splitext(key)[0]}.m4a"
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
        title = clean_display_text(str(record.get("metadataTitle") or Path(str(record.get("originalFilename", "untitled"))).stem))
        deity_id = record.get("deityId")
        category = record.get("category")
        track_id = str(record.get("trackId") or f"{deity_id or 'unknown'}_{slugify(title).replace('-', '_')}")
        object_key = record.get("objectKey") or _object_key(deity_id, title, record.get("extension", ".m4a"))
        tracks.append({
            "id": track_id,
            "titleEnglish": title,
            "titleHindi": "",
            "deityId": deity_id or "",
            "category": category or "",
            "artist": record.get("metadataArtist") or "Licensed recording",
            "source": record.get("metadataArtist") or "Licensed recording",
            "language": "HINDI",
            "durationMs": record.get("durationMs"),
            "audioObjectKey": object_key,
            "audioUrl": build_audio_url(normalized_base, object_key),
            "artworkUrl": None,
            "lyricsId": None,
            "rightsStatus": str(record.get("rightsStatus", "UNKNOWN")).upper(),
            "rightsEvidence": record.get("rightsEvidence"),
            "licenseReference": record.get("licenseReference"),
            "attribution": record.get("attribution") or record.get("metadataArtist"),
            "sha256": record.get("sha256"),
            "availability": "STAGED" if record.get("valid", True) and not record.get("duplicateOf") else "UNAVAILABLE",
            "needsReview": bool(record.get("needsReview", True)),
            "originalFilename": record.get("originalFilename"),
        })
    return {"schemaVersion": 1, "catalogVersion": catalog_version, "tracks": tracks}


def build_publishable_manifest(records: Iterable[dict[str, Any]], base_url: str | None = None, catalog_version: str = "draft") -> dict[str, Any]:
    """Return only records safe for a validated production catalog snapshot."""
    eligible = [record for record in records if (
        record.get("valid")
        and not record.get("duplicateOf")
        and not record.get("needsReview")
        and record.get("deityId")
        and record.get("category")
        and record.get("durationMs")
        and str(record.get("rightsStatus", "")).upper() in APPROVED_RIGHTS
    )]
    return build_manifest(eligible, base_url, catalog_version)


def build_remote_catalog(manifest: dict[str, Any], catalog_version: str | None = None) -> dict[str, Any]:
    """Adapt the validated manifest to the app's versioned RemoteCatalogDto wire shape."""
    tracks = manifest.get("tracks", [])
    deity_ids = sorted({str(track.get("deityId")) for track in tracks if track.get("deityId")})
    deities = [{
        "id": deity_id,
        "nameKey": f"deity_{deity_id}",
        "descriptionKey": f"deity_{deity_id}_description",
        "artworkKey": deity_id,
        "aliases": sorted(set(_DEITY_ALIASES.get(deity_id, (deity_id,)))),
        "supportedCategories": sorted({str(track.get("category")) for track in tracks if track.get("deityId") == deity_id and track.get("category")}),
    } for deity_id in deity_ids]
    remote_tracks = []
    for track in tracks:
        title = str(track.get("titleEnglish") or "").strip()
        subtitle = str(track.get("source") or "Licensed recording").strip()
        artist = str(track.get("artist") or track.get("source") or "Licensed recording").strip()
        remote_tracks.append({
            "id": track.get("id"),
            "titleEnglish": title,
            "titleHindi": track.get("titleHindi") or title,
            "subtitleEnglish": subtitle,
            "subtitleHindi": subtitle,
            "artistEnglish": artist,
            "artistHindi": artist,
            "deityId": track.get("deityId"),
            "category": track.get("category"),
            "language": track.get("language", "HINDI"),
            "durationMs": track.get("durationMs"),
            "artworkKey": track.get("deityId") or "default",
            "audioUrl": track.get("audioUrl"),
            "lyricsId": track.get("lyricsId"),
            "keywords": [],
            "aliases": [],
            "rightsStatus": track.get("rightsStatus"),
            "rightsEvidence": track.get("rightsEvidence"),
            "availability": track.get("availability"),
        })
    return {
        "schemaVersion": 1,
        "version": catalog_version or manifest.get("catalogVersion", "draft"),
        "deities": deities,
        "tracks": remote_tracks,
        "lyrics": [],
    }


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
            if not str(track.get(field, "")).strip() and not track.get("needsReview"):
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
        if duration is None:
            if track.get("needsReview"):
                warnings.append(f"{prefix}.durationMs is missing and needs review")
            else:
                errors.append(f"{prefix}.durationMs is required")
        else:
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
        elif rights in APPROVED_RIGHTS and not track.get("needsReview"):
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
        elif not key or root not in staged.parents:
            status, reason = "BLOCKED_PATH", "staged path escapes staging directory"
        elif track.get("needsReview"):
            status, reason = "BLOCKED_REVIEW", "metadata needs review"
        elif track.get("availability") == "UNAVAILABLE":
            status, reason = "BLOCKED_UNAVAILABLE", "source is invalid or duplicate"
        elif not staged.is_file():
            status, reason = "MISSING_STAGED_FILE", "staged file does not exist"
        plan.append({"id": track.get("id"), "localStagedFile": str(staged), "objectKey": key, "status": status, "reason": reason, "sha256": track.get("sha256")})
    return plan


def write_csv(path: Path, rows: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    fields = sorted({key for row in rows for key in row}) if rows else ["status"]
    with path.open("w", newline="", encoding="utf-8") as stream:
        writer = csv.DictWriter(stream, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def stage_valid_files(records: Iterable[dict[str, Any]], output_dir: str | Path, ffmpeg: str | None = None) -> list[dict[str, Any]]:
    destination = Path(output_dir).expanduser().resolve()
    staged: list[dict[str, Any]] = []
    for record in records:
        if not record.get("valid") or record.get("duplicateOf"):
            continue
        source = Path(str(record["sourcePath"])).resolve()
        target = (destination / str(record["objectKey"])).resolve()
        target.parent.mkdir(parents=True, exist_ok=True)
        if source == target or source in target.parents:
            raise ValueError("staging output must not be inside the source file")
        if record.get("streamingAction") == "TRANSCODE_AAC_M4A":
            if not ffmpeg:
                raise ValueError(f"ffmpeg is required to create a streaming derivative for {source.name}")
            command = [ffmpeg, "-v", "error", "-y", "-i", str(source), "-map", "0:a:0", "-vn", "-c:a", "aac", "-b:a", "128k", "-movflags", "+faststart"]
            channels = record.get("channels")
            if channels == 1:
                command.extend(["-ac", "1"])
            elif channels == 2:
                command.extend(["-ac", "2"])
            command.append(str(target))
            result = subprocess.run(command, capture_output=True, text=True, timeout=300, check=False)
            if result.returncode != 0:
                raise ValueError(f"ffmpeg failed for {source.name}: {result.stderr.strip()}")
        else:
            shutil.copy2(source, target)
        staged.append({"sourcePath": str(source), "stagedPath": str(target), "objectKey": record["objectKey"], "action": record.get("streamingAction", "COPY_ORIGINAL")})
    return staged
