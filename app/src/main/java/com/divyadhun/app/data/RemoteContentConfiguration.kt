package com.divyadhun.app.data

import java.net.URI

/**
 * The single boundary for resolving remote media locations.
 *
 * The catalog stores object keys, while deployment configuration supplies the
 * HTTPS base URL. Changing from an R2 development hostname to a CDN/custom
 * domain therefore does not affect playback or presentation code.
 */
data class RemoteContentConfiguration(
    val audioBaseUrl: String? = null,
    val artworkBaseUrl: String? = null,
    val catalogUrl: String? = null
) {
    fun audioUrl(objectKey: String): String? = resolve(audioBaseUrl, objectKey)

    fun artworkUrl(objectKey: String): String? = resolve(artworkBaseUrl, objectKey)

    fun catalogUrl(): String? = catalogUrl?.let(RemoteContentUrlPolicy::validateHttpsUrl)

    private fun resolve(baseUrl: String?, objectKey: String): String? {
        val base = baseUrl?.trim()?.takeIf(String::isNotEmpty) ?: return null
        val key = objectKey.trim().trimStart('/')
        if (key.isEmpty()) return null
        val joined = base.trimEnd('/') + "/" + key
        return RemoteContentUrlPolicy.validateHttpsUrl(joined)
    }
}

/** Shared URL policy used by catalog mapping and Media3 item mapping. */
object RemoteContentUrlPolicy {
    fun validateHttpsUrl(rawUrl: String): String? {
        val value = rawUrl.trim()
        if (value.isEmpty()) return null
        val uri = runCatching { URI(value) }.getOrNull() ?: return null
        if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank()) return null
        return value
    }

    fun validateAudioUrl(rawUrl: String): String? {
        // Keep the policy codec-agnostic: Media3 can negotiate the response
        // MIME type, and signed/CDN URLs often have no meaningful extension.
        // MP3, M4A and AAC are therefore accepted alongside other HTTPS media.
        return validateHttpsUrl(rawUrl)
    }

    fun requireAudioUrl(rawUrl: String): String =
        validateAudioUrl(rawUrl) ?: error("Remote audio URL must use HTTPS")

    fun requireArtworkUrl(rawUrl: String): String =
        validateHttpsUrl(rawUrl) ?: error("Remote artwork URL must use HTTPS")
}
