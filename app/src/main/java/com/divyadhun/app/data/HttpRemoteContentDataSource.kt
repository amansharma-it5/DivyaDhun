package com.divyadhun.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Reads a versioned HTTPS catalog without coupling the UI to a transport client. */
class HttpRemoteContentDataSource(
    private val catalogUrl: String,
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 15_000,
    private val maxCatalogBytes: Int = 5 * 1024 * 1024
) : RemoteContentDataSource {
    override suspend fun fetchCatalog(): RemoteCatalogDto = withContext(Dispatchers.IO) {
        val validatedUrl = RemoteContentUrlPolicy.validateHttpsUrl(catalogUrl)
            ?: error("Remote catalog URL must use HTTPS")
        val connection = (URL(validatedUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = false
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Cache-Control", "no-cache")
        }
        try {
            check(connection.responseCode in 200..299) { "Remote catalog HTTP ${connection.responseCode}" }
            check(connection.contentLengthLong <= maxCatalogBytes || connection.contentLengthLong < 0) { "Remote catalog is too large" }
            val body = connection.inputStream.use { input -> readBoundedUtf8(input, maxCatalogBytes) }
            RemoteCatalogJson.decode(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun readBoundedUtf8(input: java.io.InputStream, limit: Int): String {
        require(limit > 0) { "Catalog size limit must be positive" }
        val output = ByteArrayOutputStream(minOf(limit, 16 * 1024))
        val buffer = ByteArray(16 * 1024)
        var total = 0
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            check(total <= limit) { "Remote catalog is too large" }
            output.write(buffer, 0, count)
        }
        return output.toString(Charsets.UTF_8.name())
    }
}

/** Strict enough for production data while accepting the draft fields emitted by the ingest tool. */
object RemoteCatalogJson {
    private val json = Json { ignoreUnknownKeys = true; isLenient = false }

    fun decode(payload: String): RemoteCatalogDto {
        val root = json.parseToJsonElement(payload).jsonObject
        val version = root.string("version") ?: root.string("catalogVersion")
            ?: error("Remote catalog version is missing")
        return RemoteCatalogDto(
            version = version,
            deities = root.array("deities").map(::deity),
            tracks = root.array("tracks").map(::track),
            lyrics = root.array("lyrics").map(::lyrics),
            fetchedAtEpochMs = root.long("fetchedAtEpochMs")
        )
    }

    private fun deity(value: kotlinx.serialization.json.JsonElement): RemoteDeityDto {
        val item = value.jsonObject
        return RemoteDeityDto(
            id = item.required("id"),
            nameKey = item.required("nameKey"),
            descriptionKey = item.required("descriptionKey"),
            artworkKey = item.required("artworkKey"),
            artworkUrl = item.string("artworkUrl"),
            aliases = item.array("aliases").strings(),
            supportedCategories = item.array("supportedCategories").strings()
        )
    }

    private fun track(value: kotlinx.serialization.json.JsonElement): RemoteTrackDto {
        val item = value.jsonObject
        val title = item.required("titleEnglish")
        val subtitle = item.string("subtitleEnglish") ?: "Licensed recording"
        val artist = item.string("artistEnglish") ?: item.string("source") ?: "Licensed recording"
        return RemoteTrackDto(
            id = item.required("id"),
            titleEnglish = title,
            titleHindi = item.string("titleHindi") ?: title,
            subtitleEnglish = subtitle,
            subtitleHindi = item.string("subtitleHindi") ?: subtitle,
            artistEnglish = artist,
            artistHindi = item.string("artistHindi") ?: artist,
            deityId = item.required("deityId"),
            category = item.required("category"),
            language = item.string("language") ?: "HINDI",
            durationMs = item.long("durationMs"),
            artworkKey = item.string("artworkKey") ?: item.required("deityId"),
            artworkUrl = item.string("artworkUrl"),
            audioUrl = item.string("audioUrl"),
            lyricsId = item.string("lyricsId"),
            keywords = item.array("keywords").strings(),
            aliases = item.array("aliases").strings(),
            audioObjectKey = item.string("audioObjectKey"),
            rightsStatus = item.string("rightsStatus"),
            rightsEvidence = item.string("rightsEvidence"),
            availability = item.string("availability"),
            sha256 = item.string("sha256")
        )
    }

    private fun lyrics(value: kotlinx.serialization.json.JsonElement): RemoteLyricsDto {
        val item = value.jsonObject
        return RemoteLyricsDto(
            id = item.required("id"),
            devanagari = item.required("devanagari"),
            romanized = item.required("romanized"),
            meaning = item.required("meaning")
        )
    }

    private fun JsonObject.required(name: String): String = string(name)
        ?: error("Remote catalog field '$name' is missing")

    private fun JsonObject.string(name: String): String? = this[name]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf(String::isNotEmpty)
    private fun JsonObject.long(name: String): Long? = this[name]?.jsonPrimitive?.longOrNull
    private fun JsonObject.array(name: String): JsonArray = this[name]?.jsonArray ?: JsonArray(emptyList())
    private fun JsonArray.strings(): List<String> = mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf(String::isNotEmpty) }
}
