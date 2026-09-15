# Production audio pipeline

Phase 18B adds a project-side, read-only inventory and publishing plan under
`tools/audio_ingest/`. The scanner accepts an explicit source directory and
never searches generic personal folders or the rest of the machine. Supported
inputs are MP3, M4A, AAC, WAV, and FLAC when the installed tools can inspect
them.

For each file it records the original name, size, SHA-256, media duration and
codec metadata when `ffprobe` is installed, plus conservative filename/folder
inference for deity and category. Zero-byte files, unreadable containers,
missing duration, exact SHA duplicates, and potential title duplicates are
reported. The source directory is never modified; optional staged copies use
deterministic `audio/<deity>/<slug>.<extension>` keys under the ignored
`build/audio-ingest/` directory.

`PRODUCTION_CONTENT_DRAFT.json` follows the existing Phase 12 catalog shape at
the metadata boundary and adds rights, object-key, hash, availability, and
review fields needed for publishing. Rights default to `UNKNOWN`. Only
`OWNER_CREATED`, `LICENSED`, or `PUBLIC_DOMAIN_RECORDING` entries can appear as
upload-ready; the uploader is a dry run unless `--upload` is explicitly used.
The tool can use an authenticated Wrangler session or an AWS-compatible CLI,
and reads R2 credentials only from environment variables. No credential value,
audio master, or generated staging output belongs in Git.

The development `r2.dev` hostname is a configuration input. A future
`audio.divyadhun.com` CDN/custom domain can replace it without changing the
Media3 or catalog architecture. No production directory was available for
this run, so no local recordings were inventoried or uploaded.
