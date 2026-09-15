package com.divyadhun.app

import com.divyadhun.app.data.ContentSource
import com.divyadhun.app.data.RemoteCatalogDto
import com.divyadhun.app.data.RemoteContentDataSource
import com.divyadhun.app.data.RemoteContentRepository
import com.divyadhun.app.data.RemoteDeityDto
import com.divyadhun.app.data.RemoteLyricsDto
import com.divyadhun.app.data.RemoteTrackDto
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.Language
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteContentRepositoryTest {
    @Test fun validVersionedSnapshotMapsRemoteAudioAndArtwork() = runBlocking {
        val repository = RemoteContentRepository(RemoteContentDataSource { sampleCatalog() })
        val track = repository.getTrack("remote_shiv")

        assertNotNull(track)
        assertEquals(ContentSource.REMOTE, repository.status.value.source)
        assertEquals("2026.09", repository.status.value.catalogVersion)
        assertEquals(DevotionalCategory.MANTRA, track?.category)
        assertEquals(Language.HINDI, track?.language)
        assertTrue(track?.isPlayable == true)
        assertEquals("remote_shiv", repository.search("mahadev").single().id)
        assertEquals("remote_shiv", repository.search("महादेव").single().id)
    }

    @Test fun networkFailureUsesLocalFallbackAndExposesError() = runBlocking {
        val repository = RemoteContentRepository(RemoteContentDataSource { error("offline") })
        val tracks = repository.getTracks()

        assertEquals(32, tracks.size)
        assertEquals(ContentSource.LOCAL_FALLBACK, repository.status.value.source)
        assertEquals("offline", repository.status.value.errorMessage)
    }

    @Test fun invalidRemoteSnapshotFallsBackWithoutPartialCatalog() = runBlocking {
        val repository = RemoteContentRepository(RemoteContentDataSource {
            sampleCatalog().copy(tracks = listOf(sampleCatalog().tracks.single().copy(deityId = "missing")))
        })
        val deities = repository.getDeities()

        assertEquals(8, deities.size)
        assertEquals(ContentSource.LOCAL_FALLBACK, repository.status.value.source)
        assertTrue(repository.status.value.errorMessage.orEmpty().contains("unknown deity"))
    }

    @Test fun refreshKeepsLastValidRemoteSnapshotWhenLaterFetchFails() = runBlocking {
        var shouldFail = false
        val repository = RemoteContentRepository(RemoteContentDataSource {
            if (shouldFail) error("timeout") else sampleCatalog()
        })
        repository.getTracks()
        shouldFail = true
        repository.refresh()

        assertEquals(ContentSource.REMOTE, repository.status.value.source)
        assertEquals("2026.09", repository.status.value.catalogVersion)
        assertEquals("timeout", repository.status.value.errorMessage)
        assertEquals("remote_shiv", repository.getTracks().single().id)
    }

    @Test fun cleartextRemoteAudioIsRejectedAndUsesLocalFallback() = runBlocking {
        val repository = RemoteContentRepository(RemoteContentDataSource {
            sampleCatalog().copy(tracks = listOf(sampleCatalog().tracks.single().copy(audioUrl = "http://cdn.example.test/shiv.ogg")))
        })

        repository.getTracks()

        assertEquals(ContentSource.LOCAL_FALLBACK, repository.status.value.source)
        assertTrue(repository.status.value.errorMessage.orEmpty().contains("HTTPS"))
        assertEquals(32, repository.getTracks().size)
        assertEquals(null, repository.getTrack("hanuman_chalisa")?.audioSource)
    }

    private fun sampleCatalog() = RemoteCatalogDto(
        version = "2026.09",
        deities = listOf(
            RemoteDeityDto(
                id = "shiv",
                nameKey = "deity_shiv",
                descriptionKey = "deity_shiv_description",
                artworkKey = "shiv",
                artworkUrl = "https://cdn.example.test/shiv.png",
                aliases = listOf("shiv", "mahadev", "महादेव"),
                supportedCategories = listOf("MANTRA")
            )
        ),
        tracks = listOf(
            RemoteTrackDto(
                id = "remote_shiv",
                titleEnglish = "Om Namah Shivaya",
                titleHindi = "ॐ नमः शिवाय",
                subtitleEnglish = "A traditional prayer",
                subtitleHindi = "पारंपरिक प्रार्थना",
                artistEnglish = "Traditional",
                artistHindi = "पारंपरिक",
                deityId = "shiv",
                category = "mantra",
                language = "hi",
                artworkKey = "shiv",
                artworkUrl = "https://cdn.example.test/shiv-track.png",
                audioUrl = "https://cdn.example.test/shiv.ogg",
                keywords = listOf("mahadev"),
                aliases = listOf("महादेव")
            )
        ),
        lyrics = listOf(RemoteLyricsDto("remote_lyrics", "ॐ नमः शिवाय", "Om Namah Shivaya", "A salutation to Shiva"))
    )
}
