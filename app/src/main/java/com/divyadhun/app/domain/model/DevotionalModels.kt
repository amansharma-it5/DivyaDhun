package com.divyadhun.app.domain.model

data class LocalizedText(val english: String, val hindi: String)

enum class Language(val tag: String) {
    ENGLISH("en"), HINDI("hi");

    companion object {
        fun fromStored(value: String?) = entries.firstOrNull { it.name == value } ?: ENGLISH
    }
}

enum class ListeningTime {
    MORNING, EVENING, BEFORE_SLEEP, ANYTIME;

    companion object {
        fun fromStored(value: String?) = entries.firstOrNull { it.name == value } ?: ANYTIME
    }
}

enum class DevotionalCategory {
    AARTI, BHAJAN, MANTRA, CHALISA, STOTRA, MEDITATION
}

data class Lyrics(
    val id: String,
    val devanagari: String,
    val romanized: String,
    val meaning: String
)

/** A playback-ready source that stays independent from ExoPlayer and Android resources. */
sealed interface AudioSource {
    data class BundledResource(val resourceName: String) : AudioSource
    data class RemoteUrl(val url: String) : AudioSource
}

/** Artwork can stay local today while a future catalog supplies a validated CDN URL. */
sealed interface ArtworkSource {
    data class LocalKey(val key: String) : ArtworkSource
    data class RemoteUrl(val url: String) : ArtworkSource
}

data class Deity(
    val id: String,
    val nameKey: String,
    val descriptionKey: String,
    val artworkKey: String,
    val aliases: Set<String>,
    val supportedCategories: Set<DevotionalCategory>,
    val artworkSource: ArtworkSource? = null
)

data class Track(
    val id: String,
    val title: LocalizedText,
    val subtitle: LocalizedText,
    val artistOrSource: LocalizedText,
    val deityId: String,
    val category: DevotionalCategory,
    val language: Language = Language.HINDI,
    val durationMs: Long? = null,
    val artworkKey: String,
    val audioSource: AudioSource? = null,
    val lyricsId: String? = null,
    val keywords: Set<String> = emptySet(),
    val aliases: Set<String> = emptySet(),
    val artworkSource: ArtworkSource? = null
) {
    val isPlayable: Boolean get() = audioSource != null
}

data class DevotionalCollection(
    val id: String,
    val title: LocalizedText,
    val subtitle: LocalizedText,
    val trackIds: List<String>,
    val category: DevotionalCategory? = null,
    val deityId: String? = null,
    val artworkKey: String,
    val artworkSource: ArtworkSource? = null
)
