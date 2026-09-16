# Production audio pipeline

Phase 18B/19 adds a project-side, read-only inventory and publishing plan under
`tools/audio_ingest/`. The scanner accepts an explicit source directory and
never searches generic personal folders or the rest of the machine. Supported
inputs are MP3, M4A, AAC, WAV, and FLAC when the installed tools can inspect
them.

For each file it records the relative source name, size, SHA-256, media duration,
codec, bitrate, sample rate, channels, and embedded title/artist/album metadata
when `ffprobe` is installed, including track number and embedded-artwork
presence, plus conservative filename/folder inference for deity and category.
Obvious download-site suffixes are removed from display
metadata without changing source filenames. Zero-byte files, unreadable
containers, missing duration, exact SHA duplicates, and potential title
duplicates are reported. The source directory is never modified; staged copies
use deterministic `audio/<deity>/<slug>.<extension>` keys under the ignored
`build/production-audio/` directory.

`PRODUCTION_CONTENT_DRAFT.json` follows the existing Phase 12 catalog shape at
the metadata boundary and adds rights, rights evidence, object-key, hash,
availability, and review fields needed for publishing. Rights default to
`UNKNOWN`; this run used `LICENSED` with `USER_CONFIRMED` only because the owner
supplied that assertion. Only `OWNER_CREATED`, `LICENSED`, or
`PUBLIC_DOMAIN_RECORDING` entries without metadata review can appear in
`PRODUCTION_CONTENT_PUBLISHABLE.json`. Exact duplicates are retained in the
inventory but excluded from staging/upload. The uploader is a dry run unless
`--upload` is explicitly used and now verifies existing objects before allowing
an overwrite. It can use an authenticated Wrangler session or an AWS-compatible
CLI, and reads R2 credentials only from environment variables. No credential
value, audio master, or generated staging output belongs in Git.

The development `r2.dev` hostname is a configuration input. A future
`audio.divyadhun.com` CDN/custom domain can replace it without changing the
Media3 or catalog architecture. The explicitly authorized source directory was
inventoried in the Phase 19 run. On 2026-09-16, authenticated Wrangler 4.132.0
listed the `divyadhun-audio` bucket successfully, and the generated upload plan
completed its dry-run checks. No R2 object or catalog was uploaded: public
streaming and hosting permission in the license remains unconfirmed, and final
metadata review is still pending. The local production catalog draft remains
staged-only until those release gates are supplied.

The authenticated Cloudflare R2 object listing on 2026-09-16 reported three
existing objects totaling 23,263,652 bytes (23.3 MB): two zero-byte directory
markers and the existing Phase 18 Hanuman Chalisa fixture. None of the 54
production audio keys or `catalog/latest.json` exists yet; the read-only audit
found zero matching or conflicting target objects. The public `r2.dev` endpoint
returned HTTP 403 for HEAD probes, so the uploader must continue to fail closed
when its configured public verification URL is not readable.

The run discovered 130 MP3 files (1,411,677,680 original bytes), staged 127
canonical copies/derivatives (1,333,381,709 bytes), excluded one zero-byte file
and two exact duplicates, and produced 54 publishable metadata records. Seven
high-bitrate inputs were converted to audio-only AAC-LC M4A at 128 kbps; the
remaining valid MP3s were copied directly. These numbers are run artifacts and
should be regenerated whenever the source directory changes.
