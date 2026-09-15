# Production backend handoff

The Phase 12 remote-content seam is ready for an implementation, but production credentials and infrastructure are not available in this repository. The app must continue to boot from `LocalContentRepository` when the remote source is absent or invalid.

## Catalog contract

The remote endpoint returns an immutable snapshot with a monotonically increasing `version`, fetch metadata, localized deity records, localized track records, optional lyrics, and media references. Track IDs, deity IDs, category names, language tags, and lyrics IDs use the same stable domain contract described in `PRODUCTION_CONTENT_CONTRACT.md`.

The client validates the complete snapshot before publication: IDs and relationships are unique and known, required localized values are present, media URLs are HTTPS, and rights metadata is present. A rejected snapshot never partially replaces a valid one.

## Delivery and caching

- Serve catalog JSON and artwork/audio through HTTPS endpoints or a CDN with immutable versioned paths.
- Publish an explicit content version and cache validators (ETag or equivalent).
- Keep the last valid remote snapshot in the client cache and use local content when no valid snapshot exists.
- Bound response size and request timeouts; do not block the main thread.
- Treat a withdrawn track as unavailable and invalidate cached media on the next refresh.

## Failure behavior

Network, parsing, validation, and HTTP errors are surfaced as repository status while the last valid snapshot or local fallback remains available. No screen should show a playable control for a track without a validated audio source. Diagnostics must not include authorization headers, tokens, or personally identifying data.

## Required future configuration

The eventual integration needs a production base URL, CDN policy, cache/retention policy, environment-specific access credentials, monitoring ownership, and a rights-review process. Put secrets in the deployment environment or secret manager; never add them to Gradle files, `local.properties`, source control, or this handoff document. No paid service or fake credential is created by Phase 17.
