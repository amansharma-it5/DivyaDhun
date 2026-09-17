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
completed its dry-run checks. The owner has explicitly confirmed that the
supplied licenses allow public streaming through DivyaDhun and public hosting
on Cloudflare R2; records remain marked `LICENSED` / `USER_CONFIRMED`.
Documentary license agreements were not independently reviewed. The original
76 metadata-review records were re-investigated with local technical evidence
and advisory web research: 22 were verified and published in the separate
Phase 20 canary-gated expansion, one additional exact catalog match was
verified and published in Phase 21, and 53 remain metadata/media-review-blocked
and excluded from the catalog.

The Phase 20 expansion was published only after a 22-record dry run, remote
conflict checks, a one-track canary, and catalog/reference verification. The
Phase 21 expansion followed the same gates for one additional record. The
versioned catalogs are `catalog/phase20-2026-09-16.json` and
`catalog/phase21-2026-09-17.json`; `catalog/latest.json` now contains 77
unique verified tracks. All 77 referenced objects are HTTPS-readable with the
expected MIME type, Content-Length, and byte-range support. The two distinct
Shiv Shambhu recordings remain separate because their source SHA-256 values,
durations, and credited artists differ.

The authenticated Cloudflare R2 bucket audit before expansion reported 59
objects and `603,046,365` bytes (603 MB in Wrangler's rounded display). The
the latest direct object-level ledger is `819,795,136` bytes across 84 known
objects, including 77 audio objects, the existing Phase 18 fixture and
directory markers, and four catalog objects. It adds the 7,468,681-byte Phase
21 audio object, the 76,402-byte versioned catalog, and the 894-byte increase
to `catalog/latest.json` over the post-Phase-20 ledger. The authenticated
Wrangler bucket-info display may lag and currently reports 82 objects/812 MB;
the direct ledger is retained as the exact usage audit until Cloudflare's
usage view catches up. The
public `r2.dev` endpoint returned HTTP 403 for a default `HEAD` request, but
the uploader's explicit `DivyaDhun-Phase19/1.0` user agent gets HTTP 200 with
the expected media headers; a range `GET` returns HTTP 206. The catalog and
media verification path therefore remains fail-closed without weakening
Cloudflare security settings.

The blocked-track recovery report is generated with:

```text
python tools/audio_research/recover_blocked_metadata.py \
  --inventory build/phase19-verification-20260916/AUDIO_INVENTORY.csv \
  --output-dir build/metadata-recovery \
  --evidence tools/audio_research/web_research_evidence.json
```

It writes ignored JSON, CSV, and Markdown reports for all 76 originally blocked records,
including exact missing fields, SHA-256/ffprobe evidence, deterministic local
candidates, and separately labeled web research suggestions. It never listens
to or uploads audio, fabricates metadata, changes source files, or promotes a
record into the catalog.

The run discovered 130 MP3 files (1,411,677,680 original bytes), staged 127
canonical copies/derivatives (1,333,381,709 bytes), excluded one zero-byte file
and two exact duplicates, and produced 54 initial publishable metadata
records. The recovery workflow later verified 22 additional unique MP3s;
seven high-bitrate inputs were converted to audio-only AAC-LC M4A at 128 kbps;
the remaining valid MP3s were copied directly. These numbers are run artifacts
and should be regenerated whenever the source directory changes.
