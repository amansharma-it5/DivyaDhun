"""Read-only audio inventory, catalog validation, and safe upload planning."""

from .pipeline import (
    APPROVED_RIGHTS,
    SUPPORTED_EXTENSIONS,
    build_manifest,
    build_publishable_manifest,
    build_remote_catalog,
    build_upload_plan,
    detect_duplicates,
    infer_metadata,
    normalize_base_url,
    prepare_streaming_records,
    scan_directory,
    sha256_file,
    slugify,
    validate_manifest,
)

__all__ = [
    "APPROVED_RIGHTS",
    "SUPPORTED_EXTENSIONS",
    "build_manifest",
    "build_publishable_manifest",
    "build_remote_catalog",
    "build_upload_plan",
    "detect_duplicates",
    "infer_metadata",
    "normalize_base_url",
    "prepare_streaming_records",
    "scan_directory",
    "sha256_file",
    "slugify",
    "validate_manifest",
]
