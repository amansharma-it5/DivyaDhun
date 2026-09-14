package com.divyadhun.app

import com.divyadhun.app.domain.model.FestivalCatalog
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FestivalCatalogTest {
    @Test fun catalogUsesStableIdsAndUpcomingDatesAreOrdered() {
        val festivals = FestivalCatalog.upcoming(LocalDate.of(2026, 1, 1))
        assertEquals(3, festivals.size)
        assertEquals(listOf("hanuman_jayanti", "janmashtami", "diwali"), festivals.map { it.id })
        assertTrue(festivals.map { it.id }.distinct().size == festivals.size)
    }

    @Test fun nextYearWrapIsSupported() {
        val festivals = FestivalCatalog.upcoming(LocalDate.of(2026, 12, 31), limit = 1)
        assertEquals("hanuman_jayanti", festivals.single().id)
    }
}
