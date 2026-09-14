package com.divyadhun.app.data

import com.divyadhun.app.domain.model.ArtworkSource
import com.divyadhun.app.domain.model.AudioSource
import com.divyadhun.app.domain.model.Deity
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.LocalizedText
import com.divyadhun.app.domain.model.Lyrics
import com.divyadhun.app.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale

/** Versioned wire shape for a future CDN/API catalog. It intentionally has no network dependency. */
data class RemoteCatalogDto(
    val version: String,
    val deities: List<RemoteDeityDto>,
    val tracks: List<RemoteTrackDto>,
    val lyrics: List<RemoteLyricsDto> = emptyList(),
    val fetchedAtEpochMs: Long? = null
)

data class RemoteDeityDto(
    val id: String,
    val nameKey: String,
    val descriptionKey: String,
    val artworkKey: String,
    val artworkUrl: String? = null,
    val aliases: List<String> = emptyList(),
    val supportedCategories: List<String> = emptyList()
)

data class RemoteTrackDto(
    val id: String,
    val titleEnglish: String,
    val titleHindi: String,
    val subtitleEnglish: String,
    val subtitleHindi: String,
    val artistEnglish: String,
    val artistHindi: String,
    val deityId: String,
    val category: String,
    val language: String = Language.HINDI.name,
    val durationMs: Long? = null,
    val artworkKey: String,
    val artworkUrl: String? = null,
    val audioUrl: String? = null,
    val lyricsId: String? = null,
    val keywords: List<String> = emptyList(),
    val aliases: List<String> = emptyList()
)

data class RemoteLyricsDto(
    val id: String,
    val devanagari: String,
    val romanized: String,
    val meaning: String
)

/** Integration point for a HTTP/CDN client that may be added in a later phase. */
fun interface RemoteContentDataSource {
    suspend fun fetchCatalog(): RemoteCatalogDto
}

/** Explicitly unavailable until production backend credentials and infrastructure are supplied. */
object UnavailableRemoteContentDataSource : RemoteContentDataSource {
    override suspend fun fetchCatalog(): RemoteCatalogDto =
        error("Remote catalog is not configured: production backend credentials are required")
}

enum class ContentSource {
    REMOTE,
    LOCAL_FALLBACK
}

data class ContentRepositoryStatus(
    val source: ContentSource = ContentSource.LOCAL_FALLBACK,
    val catalogVersion: String? = null,
    val errorMessage: String? = null
)

/**
 * Remote-capable repository with a single in-memory snapshot and a deterministic local fallback.
 * No call is made until the first repository read or an explicit [refresh].
 */
class RemoteContentRepository(
    private val dataSource: RemoteContentDataSource,
    private val fallback: ContentRepository = LocalContentRepository()
) : ContentRepository {
    private data class Catalog(
        val deities: List<Deity>,
        val tracks: List<Track>,
        val lyrics: Map<String, Lyrics>
    )

    private val loadMutex = Mutex()
    private var cachedCatalog: Catalog? = null
    private var cachedRemoteVersion: String? = null
    private val mutableStatus = MutableStateFlow(ContentRepositoryStatus())
    val status: StateFlow<ContentRepositoryStatus> = mutableStatus.asStateFlow()

    suspend fun refresh(): ContentRepositoryStatus = loadMutex.withLock {
        try {
            val remote = dataSource.fetchCatalog()
            val mapped = remote.toDomainCatalog()
            cachedCatalog = mapped
            cachedRemoteVersion = remote.version.trim()
            mutableStatus.value = ContentRepositoryStatus(
                source = ContentSource.REMOTE,
                catalogVersion = cachedRemoteVersion
            )
        } catch (error: Exception) {
            if (cachedCatalog == null) {
                cachedCatalog = fallback.toCatalog()
            }
            mutableStatus.value = ContentRepositoryStatus(
                source = if (cachedRemoteVersion == null) ContentSource.LOCAL_FALLBACK else ContentSource.REMOTE,
                catalogVersion = cachedRemoteVersion,
                errorMessage = error.message ?: error::class.simpleName
            )
        }
        mutableStatus.value
    }

    private suspend fun catalog(): Catalog {
        cachedCatalog?.let { return it }
        refresh()
        return cachedCatalog ?: error("Content repository could not load a catalog")
    }

    override suspend fun getDeities() = catalog().deities
    override suspend fun getTracks() = catalog().tracks
    override suspend fun getTrack(id: String) = catalog().tracks.firstOrNull { it.id == id }
    override suspend fun getLyrics(id: String) = catalog().lyrics[id]
    override suspend fun getTracksForDeity(deityId: String) = catalog().tracks.filter { it.deityId == deityId }
    override suspend fun getTracksByCategory(category: DevotionalCategory) = catalog().tracks.filter { it.category == category }

    override suspend fun search(query: String): List<Track> {
        val normalized = normalize(query)
        if (normalized.isBlank()) return emptyList()
        val current = catalog()
        val deityAliases = current.deities.associate { it.id to it.aliases }
        return current.tracks.filter { track ->
            val haystack = listOf(
                track.id, track.title.english, track.title.hindi, track.subtitle.english, track.subtitle.hindi,
                track.artistOrSource.english, track.artistOrSource.hindi, track.deityId, track.category.name,
                *track.keywords.toTypedArray(), *track.aliases.toTypedArray(),
                *(deityAliases[track.deityId]?.toTypedArray() ?: emptyArray())
            ).joinToString(" ", transform = ::normalize)
            haystack.contains(normalized)
        }
    }

    override suspend fun searchDeities(query: String): List<Deity> {
        val normalized = normalize(query)
        if (normalized.isBlank()) return emptyList()
        return catalog().deities.filter { deity ->
            (listOf(deity.id) + deity.aliases).joinToString(" ", transform = ::normalize).contains(normalized)
        }
    }

    private suspend fun ContentRepository.toCatalog(): Catalog {
        val loadedTracks = getTracks()
        val loadedLyrics = loadedTracks.mapNotNull { it.lyricsId }
            .distinct()
            .mapNotNull { id -> getLyrics(id)?.let { id to it } }
            .toMap()
        return Catalog(getDeities(), loadedTracks, loadedLyrics)
    }

    private fun RemoteCatalogDto.toDomainCatalog(): Catalog {
        val normalizedVersion = version.trim()
        require(normalizedVersion.isNotEmpty()) { "Remote catalog version is blank" }
        require(deities.isNotEmpty()) { "Remote catalog has no deities" }
        val mappedDeities = deities.map { deity ->
            requireStableId(deity.id)
            Deity(
                id = deity.id.trim(),
                nameKey = deity.nameKey.requireText("nameKey"),
                descriptionKey = deity.descriptionKey.requireText("descriptionKey"),
                artworkKey = deity.artworkKey.requireText("artworkKey"),
                aliases = deity.aliases.map(String::trim).filter(String::isNotEmpty).toSet(),
                supportedCategories = deity.supportedCategories.map(::parseCategory).toSet(),
                artworkSource = deity.artworkUrl?.validatedArtworkUrl()?.let(ArtworkSource::RemoteUrl)
            )
        }
        require(mappedDeities.map { it.id }.toSet().size == mappedDeities.size) { "Remote deity IDs are duplicated" }
        val deityIds = mappedDeities.map { it.id }.toSet()
        val mappedTracks = tracks.map { track ->
            requireStableId(track.id)
            val deityId = track.deityId.trim()
            require(deityId in deityIds) { "Track ${track.id} references unknown deity $deityId" }
            Track(
                id = track.id.trim(),
                title = LocalizedText(track.titleEnglish.requireText("titleEnglish"), track.titleHindi.requireText("titleHindi")),
                subtitle = LocalizedText(track.subtitleEnglish.requireText("subtitleEnglish"), track.subtitleHindi.requireText("subtitleHindi")),
                artistOrSource = LocalizedText(track.artistEnglish.requireText("artistEnglish"), track.artistHindi.requireText("artistHindi")),
                deityId = deityId,
                category = parseCategory(track.category),
                language = parseLanguage(track.language),
                durationMs = track.durationMs?.takeIf { it >= 0L },
                artworkKey = track.artworkKey.requireText("artworkKey"),
                artworkSource = track.artworkUrl?.validatedArtworkUrl()?.let(ArtworkSource::RemoteUrl),
                audioSource = track.audioUrl?.validatedAudioUrl()?.let(AudioSource::RemoteUrl),
                lyricsId = track.lyricsId?.trim()?.takeIf(String::isNotEmpty),
                keywords = track.keywords.map(String::trim).filter(String::isNotEmpty).toSet(),
                aliases = track.aliases.map(String::trim).filter(String::isNotEmpty).toSet()
            )
        }
        require(mappedTracks.map { it.id }.toSet().size == mappedTracks.size) { "Remote track IDs are duplicated" }
        val mappedLyrics = lyrics.associate { lyric ->
            requireStableId(lyric.id)
            lyric.id.trim() to Lyrics(
                id = lyric.id.trim(),
                devanagari = lyric.devanagari.requireText("devanagari"),
                romanized = lyric.romanized.requireText("romanized"),
                meaning = lyric.meaning.requireText("meaning")
            )
        }
        return Catalog(mappedDeities, mappedTracks, mappedLyrics)
    }

    private fun parseCategory(value: String): DevotionalCategory =
        runCatching { DevotionalCategory.valueOf(value.trim().uppercase(Locale.ROOT)) }
            .getOrElse { error("Unknown devotional category: $value") }

    private fun parseLanguage(value: String): Language =
        Language.entries.firstOrNull { it.name.equals(value.trim(), true) || it.tag.equals(value.trim(), true) }
            ?: error("Unknown language: $value")

    private fun requireStableId(value: String) {
        require(value.trim().matches(Regex("[a-z0-9][a-z0-9_-]*"))) { "Invalid stable ID: $value" }
    }

    private fun String.requireText(field: String) = trim().also { require(it.isNotEmpty()) { "Remote $field is blank" } }
    private fun String.validatedAudioUrl() = trim().takeIf { it.startsWith("https://", ignoreCase = true) }
        ?: error("Remote audio URL must use HTTPS")
    private fun String.validatedArtworkUrl() = trim().takeIf { it.startsWith("https://", ignoreCase = true) }
        ?: error("Remote artwork URL must use HTTPS")
    private fun normalize(value: String) = value.lowercase(Locale.ROOT)
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")
}
