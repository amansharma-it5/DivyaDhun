package com.divyadhun.app

import com.divyadhun.app.content.HomeContentAssembler
import com.divyadhun.app.data.LocalContentRepository
import com.divyadhun.app.domain.model.DevotionalCategory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase3ContentTest {
    private val repository = LocalContentRepository()

    @Test
    fun homePersonalizationPrioritizesPreferredDeities() = runBlocking {
        val assembler = HomeContentAssembler(repository.getDeities(), repository.getTracks())
        val content = assembler.assemble(setOf("shiv"))
        assertEquals("shiv", content.yourGods.first().id)
        assertTrue(content.recommended.take(4).all { it.deityId == "shiv" })
    }

    @Test
    fun homeFallbackAndCuratedSectionsAreDeterministic() = runBlocking {
        val assembler = HomeContentAssembler(repository.getDeities(), repository.getTracks())
        val first = assembler.assemble(emptySet())
        val second = assembler.assemble(emptySet())
        assertEquals(first, second)
        assertEquals(listOf("hanuman", "shiv", "krishna", "ram"), first.yourGods.map { it.id })
        assertTrue(first.today.isNotEmpty())
        assertTrue(first.popularAartis.all { it.category == DevotionalCategory.AARTI })
        assertTrue(first.mantras.all { it.category == DevotionalCategory.MANTRA })
    }

    @Test
    fun invalidLookupsAndCatalogRelationshipsAreSafe() = runBlocking {
        val deities = repository.getDeities()
        val tracks = repository.getTracks()
        assertEquals(null, repository.getTrack("missing-track"))
        assertTrue(repository.getTracksForDeity("missing-deity").isEmpty())
        assertEquals(tracks.size, tracks.map { it.id }.toSet().size)
        assertTrue(tracks.all { track -> deities.any { it.id == track.deityId } })
        assertTrue(deities.all { deity -> deity.supportedCategories.containsAll(tracks.filter { it.deityId == deity.id }.map { it.category }.toSet()) })
        assertTrue(tracks.any { it.audioSource != null })
        assertTrue(tracks.any { it.audioSource == null })
        assertTrue(tracks.none { it.artistOrSource.english.contains("T-Series", ignoreCase = true) })
    }

    @Test
    fun categoryAndDeitySearchAreUnicodeSafe() = runBlocking {
        assertFalse(repository.getTracksByCategory(DevotionalCategory.CHALISA).isEmpty())
        assertEquals(listOf("shiv"), repository.searchDeities("महादेव").map { it.id })
        assertEquals(listOf("hanuman"), repository.searchDeities("bajrang bali").map { it.id })
        assertTrue(repository.searchDeities(" ").isEmpty())
    }

}
