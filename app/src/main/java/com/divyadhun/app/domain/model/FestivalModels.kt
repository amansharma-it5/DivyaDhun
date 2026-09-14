package com.divyadhun.app.domain.model

import java.time.LocalDate

data class Festival(
    val id: String,
    val nameKey: String,
    val descriptionKey: String,
    val month: Int,
    val day: Int,
    val associatedDeityIds: Set<String>
)

data class FestivalCollection(
    val id: String,
    val festivalId: String,
    val trackIds: List<String>
)

object FestivalCatalog {
    val festivals: List<Festival> = listOf(
        Festival("diwali", "festival_diwali", "festival_diwali_description", 10, 20, setOf("lakshmi", "ganesh")),
        Festival("janmashtami", "festival_janmashtami", "festival_janmashtami_description", 8, 16, setOf("krishna")),
        Festival("hanuman_jayanti", "festival_hanuman_jayanti", "festival_hanuman_jayanti_description", 4, 12, setOf("hanuman"))
    )

    fun byId(id: String): Festival? = festivals.firstOrNull { it.id == id }

    fun upcoming(from: LocalDate, limit: Int = festivals.size): List<Festival> = festivals
        .map { festival -> festival to nextOccurrence(festival, from) }
        .sortedWith(compareBy<Pair<Festival, LocalDate>> { it.second }.thenBy { it.first.id })
        .take(limit.coerceAtLeast(0))
        .map { it.first }

    private fun nextOccurrence(festival: Festival, from: LocalDate): LocalDate {
        val thisYear = runCatching { LocalDate.of(from.year, festival.month, festival.day) }.getOrElse { from.withDayOfMonth(1) }
        return if (!thisYear.isBefore(from)) thisYear else runCatching { LocalDate.of(from.year + 1, festival.month, festival.day) }.getOrElse { from.plusYears(1) }
    }
}
