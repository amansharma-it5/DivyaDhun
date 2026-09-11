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

data class Deity(
    val id: String,
    val nameKey: String,
    val descriptionKey: String,
    val artworkKey: String,
    val aliases: Set<String>,
    val supportedCategories: Set<DevotionalCategory>
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
    val audioSource: String? = null,
    val lyricsId: String? = null,
    val keywords: Set<String> = emptySet(),
    val aliases: Set<String> = emptySet()
)

data class DevotionalCollection(
    val id: String,
    val title: LocalizedText,
    val subtitle: LocalizedText,
    val trackIds: List<String>,
    val category: DevotionalCategory? = null,
    val deityId: String? = null,
    val artworkKey: String
)
