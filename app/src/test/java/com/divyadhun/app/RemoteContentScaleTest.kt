package com.divyadhun.app

import com.divyadhun.app.data.RemoteCatalogDto
import com.divyadhun.app.data.RemoteContentDataSource
import com.divyadhun.app.data.RemoteContentRepository
import com.divyadhun.app.data.RemoteDeityDto
import com.divyadhun.app.data.RemoteTrackDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class RemoteContentScaleTest {
    @Test fun indexedSearchHandlesTwelveHundredTrackCatalog() = runBlocking {
        val repository = RemoteContentRepository(RemoteContentDataSource {
            RemoteCatalogDto(
                version = "scale-test",
                deities = listOf(RemoteDeityDto("shiv", "deity_shiv", "deity_shiv_description", "shiv", aliases = listOf("shiv", "mahadev"), supportedCategories = listOf("MANTRA"))),
                tracks = (0 until 1_200).map { index ->
                    RemoteTrackDto(
                        id = "scale_track_$index",
                        titleEnglish = "Track $index",
                        titleHindi = "ट्रैक $index",
                        subtitleEnglish = "Indexed catalog fixture",
                        subtitleHindi = "इंडेक्स कैटलॉग",
                        artistEnglish = "Traditional",
                        artistHindi = "पारंपरिक",
                        deityId = "shiv",
                        category = "MANTRA",
                        artworkKey = "shiv",
                        aliases = listOf("mahadev")
                    )
                }
            )
        })

        var elapsedMs = 0L
        var result = emptyList<com.divyadhun.app.domain.model.Track>()
        elapsedMs = measureTimeMillis { result = repository.search("track 1199") }

        assertEquals(listOf("scale_track_1199"), result.map { it.id })
        assertTrue("search should remain lightweight for the scale fixture", elapsedMs < 5_000L)
    }
}
