# DivyaDhun audio ingestion tool

This tool reads only an explicitly supplied source directory. It never renames,
moves, deletes, or overwrites source audio. Generated inventories, staged copies,
and upload plans belong in a separate output directory (the default is
`build/audio-ingest/`, which is ignored by Git).

```powershell
python -m tools.audio_ingest scan --source "D:\DivyaDhun Audio" --output build/audio-ingest --base-url https://audio.divyadhun.com --stage
python -m tools.audio_ingest validate --manifest build/audio-ingest/PRODUCTION_CONTENT_DRAFT.json
python -m tools.audio_ingest upload --manifest build/audio-ingest/PRODUCTION_CONTENT_PUBLISHABLE.json --staging-dir build/audio-ingest/staged --catalog-file build/audio-ingest/catalog/latest.json
```

The scan also writes `METADATA_REVIEW_REPORT.md`, a deterministic row-by-row
review of every record excluded from publication. It names missing fields and
source errors without assigning deity, category, artist, duration, or rights
values automatically.

The upload command is a dry run unless `--upload` is explicitly supplied. It
requires `wrangler` or the AWS CLI and a configured `R2_BUCKET`; credentials are
read from the process environment or the authenticated CLI session and are
never written to reports. `UNKNOWN` and `NOT_APPROVED` rights statuses are
always blocked from upload. The AWS-compatible path also requires `R2_ENDPOINT`
or `R2_ACCOUNT_ID`, preventing an accidental upload to an unrelated AWS
endpoint. Pass `--catalog-file` to include `catalog/latest.json` after the
audio objects; its object key is restricted to the `catalog/` prefix. Existing
objects are checked before upload and conflicting replacements are blocked by
default.

The scanner uses `ffprobe` when available and falls back to safe WAV inspection.
Missing ffprobe metadata is reported for review rather than treated as proof of
quality. Loudness is not changed automatically and source masters remain
untouched.
