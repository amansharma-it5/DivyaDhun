package com.divyadhun.app

import com.divyadhun.app.data.HttpRemoteContentDataSource
import com.divyadhun.app.data.RemoteContentDataSource
import com.divyadhun.app.data.RemoteContentRepository
import com.divyadhun.app.domain.model.ContentAvailability
import com.divyadhun.app.data.RemoteCatalogJson
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RemoteCatalogJsonTest {
    @Test
    fun decodesCatalogAndUsesEnglishFallbackForMissingHindiText() {
        val catalog = RemoteCatalogJson.decode(
            """
            {
              "version": "v1",
              "deities": [{"id":"hanuman","nameKey":"deity_hanuman","descriptionKey":"deity_hanuman_description","artworkKey":"hanuman"}],
              "tracks": [{"id":"hanuman_chalisa","titleEnglish":"Hanuman Chalisa","deityId":"hanuman","category":"CHALISA","audioObjectKey":"audio/hanuman/hanuman-chalisa.mp3","audioUrl":"https://audio.example/audio/hanuman/hanuman-chalisa.mp3","rightsStatus":"LICENSED","rightsEvidence":"USER_CONFIRMED","availability":"STAGED","sha256":"abc","durationMs":1000}],
              "lyrics": []
            }
            """.trimIndent()
        )
        assertEquals("v1", catalog.version)
        assertEquals("Hanuman Chalisa", catalog.tracks.single().titleHindi)
        assertEquals("Licensed recording", catalog.tracks.single().artistEnglish)
        assertEquals("audio/hanuman/hanuman-chalisa.mp3", catalog.tracks.single().audioObjectKey)
        assertEquals("LICENSED", catalog.tracks.single().rightsStatus)
        assertEquals("STAGED", catalog.tracks.single().availability)
        assertEquals("abc", catalog.tracks.single().sha256)
    }

    @Test
    fun stagedRemoteItemsRemainMetadataOnlyUntilCatalogPromotesAvailability() = runBlocking {
        val catalog = RemoteCatalogJson.decode(
            """
            {"version":"v1","deities":[{"id":"hanuman","nameKey":"deity_hanuman","descriptionKey":"deity_hanuman_description","artworkKey":"hanuman"}],"tracks":[{"id":"t","titleEnglish":"T","titleHindi":"T","subtitleEnglish":"S","subtitleHindi":"S","artistEnglish":"A","artistHindi":"A","deityId":"hanuman","category":"CHALISA","artworkKey":"hanuman","audioUrl":"https://audio.example/t.mp3","availability":"STAGED"}]}
            """.trimIndent()
        )
        val track = RemoteContentRepository(RemoteContentDataSource { catalog }).getTracks().single()
        assertEquals(ContentAvailability.METADATA_ONLY, track.availability)
        assertEquals(false, track.isPlayable)
    }

    @Test
    fun httpDataSourceRejectsCleartextBeforeOpeningConnection() {
        assertThrows(IllegalStateException::class.java) {
            runBlocking { HttpRemoteContentDataSource("http://catalog.example/latest.json").fetchCatalog() }
        }
    }
}
