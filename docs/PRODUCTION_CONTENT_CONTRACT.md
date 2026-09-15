# Production content ingestion contract

Phase 17 keeps the local catalog and five developer-generated WAV fixtures. Production content must be supplied through a reviewed catalog snapshot; no commercial recording or artwork is included in this repository.

Every track record must provide:

- a globally stable `id` that never changes after publication;
- localized title and subtitle values for every supported language;
- a valid deity ID or an explicit general-content value;
- one `DevotionalCategory` value from the app contract;
- a truthful artist/source label and attribution;
- duration metadata that matches the delivered audio;
- an HTTPS audio source or an approved bundled resource;
- an artwork key or HTTPS artwork URL with an explicit license;
- a lyrics ID when text is supplied;
- keywords and aliases for deterministic search;
- a rights/license record, territory, and attribution text;
- an availability state (`draft`, `published`, `withdrawn`, or equivalent).

Ingestion validation must reject duplicate IDs, unknown deity/category/language values, malformed localized fields, non-HTTPS remote media, missing rights records, and media whose declared duration differs from the asset. A withdrawn item remains addressable by ID for cache invalidation but must not be presented as playable content.

Artwork and audio must be original, public-domain, or covered by a written license that permits the intended distribution. Store the license and attribution alongside the source record; metadata alone is not evidence of recording rights. Traditional devotional titles and words do not grant rights to a particular commercial recording or translation.

The local repository remains the offline fallback. A production importer should produce the same immutable domain models used by `ContentRepository`, so the UI and playback layers do not depend on the transport format.
