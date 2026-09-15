package com.divyadhun.app

import com.divyadhun.app.data.LocalContentRepository
import com.divyadhun.app.domain.model.DevotionalCategory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalContentRepositoryTest {
    private val repository = LocalContentRepository()

    @Test fun catalogHasEightStableDeitiesAndThirtyTwoTracks() = runBlocking {
        val deities = repository.getDeities()
        val tracks = repository.getTracks()
        assertEquals(8, deities.size)
        assertEquals(32, tracks.size)
        assertEquals(tracks.size, tracks.map { it.id }.toSet().size)
        assertTrue(tracks.all { track -> deities.any { it.id == track.deityId } })
    }

    @Test fun categoryAndDeityFiltersReturnOnlyMatchingRecords() = runBlocking {
        assertTrue(repository.getTracksByCategory(DevotionalCategory.AARTI).all { it.category == DevotionalCategory.AARTI })
        assertTrue(repository.getTracksForDeity("shiv").all { it.deityId == "shiv" })
        assertEquals(4, repository.getTracksForDeity("hanuman").size)
    }

    @Test fun searchIsCaseInsensitiveAndSupportsAliases() = runBlocking {
        assertTrue(repository.search("ShIv").any { it.id == "om_namah_shivaya" })
        assertTrue(repository.search("shiv").any { it.id == "shiv_aarti" })
        assertTrue(repository.search("shiv aarti").any { it.id == "shiv_aarti" })
        assertTrue(repository.search("mahadev").all { it.deityId == "shiv" })
        assertTrue(repository.search("bajrang bali").all { it.deityId == "hanuman" })
    }

    @Test fun searchSupportsHindiAndHonestEmptyStates() = runBlocking {
        assertTrue(repository.search("महादेव").all { it.deityId == "shiv" })
        assertTrue(repository.search("   ").isEmpty())
        assertTrue(repository.search("not a prayer").isEmpty())
    }

    @Test fun traditionalLyricsMapToTracksWithoutMissingReferences() = runBlocking {
        val tracks = repository.getTracks()
        val withLyrics = tracks.filter { it.lyricsId != null }
        assertTrue(withLyrics.isNotEmpty())
        assertTrue(withLyrics.all { track -> repository.getLyrics(track.lyricsId!!)?.id == track.lyricsId })
        assertEquals(null, repository.getLyrics("missing-lyrics"))
    }

    @Test fun localCatalogDoesNotEmbedThePhase18RemoteStreamingFixture() = runBlocking {
        val tracks = repository.getTracks()
        val hanumanChalisa = tracks.single { it.id == "hanuman_chalisa" }

        assertEquals(null, hanumanChalisa.audioSource)
        assertTrue(tracks.none { it.audioSource is com.divyadhun.app.domain.model.AudioSource.RemoteUrl })
    }
}
