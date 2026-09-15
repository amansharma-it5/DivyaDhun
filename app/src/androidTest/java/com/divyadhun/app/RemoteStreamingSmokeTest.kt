package com.divyadhun.app

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.divyadhun.app.data.LocalContentRepository
import com.divyadhun.app.data.RemoteCatalogDto
import com.divyadhun.app.data.RemoteContentDataSource
import com.divyadhun.app.data.RemoteContentRepository
import com.divyadhun.app.data.RemoteDeityDto
import com.divyadhun.app.data.RemoteTrackDto
import com.divyadhun.app.domain.model.AudioSource
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.Track
import com.divyadhun.app.playback.Media3PlaybackController
import com.divyadhun.app.playback.PlaybackService
import com.divyadhun.app.playback.PlaybackStatus
import com.divyadhun.app.playback.TrackMediaItemMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class RemoteStreamingSmokeTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private companion object {
        const val R2_TEST_AUDIO_URL =
            "https://pub-867b1331c5b9468db9b3fce509773119.r2.dev/audio/hanuman/Hanuman%20Chalisa.mp3"
    }

    @Test
    fun r2TrackPreparesAdvancesAndSupportsPauseResumeAndSeek() = runBlocking {
        context.stopService(Intent(context, PlaybackService::class.java))
        val track = remoteR2Track()
        val controller = Media3PlaybackController(context)

        try {
            withContext(Dispatchers.Main.immediate) { controller.playTrack(track, listOf(track)) }
            val started = withTimeout(60_000) {
                controller.state.first {
                    it.currentTrackId == track.id &&
                        it.status in setOf(PlaybackStatus.PLAYING, PlaybackStatus.PAUSED, PlaybackStatus.ERROR)
                }
            }
            assertNotEquals("R2 track reported a Media3 playback error", PlaybackStatus.ERROR, started.status)

            val progressed = withTimeout(45_000) {
                controller.state.first { it.currentTrackId == track.id && it.status == PlaybackStatus.PLAYING && it.positionMs >= 1_000L }
            }
            assertTrue("R2 playback position did not advance", progressed.positionMs >= 1_000L)

            withContext(Dispatchers.Main.immediate) { controller.pause() }
            val paused = withTimeout(10_000) {
                controller.state.first { it.currentTrackId == track.id && it.status == PlaybackStatus.PAUSED && !it.isPlaying }
            }
            val target = minOf(paused.positionMs + 3_000L, (paused.durationMs - 1_000L).coerceAtLeast(1_000L))
            withContext(Dispatchers.Main.immediate) { controller.seekTo(target) }
            val sought = withTimeout(10_000) {
                controller.state.first {
                    it.currentTrackId == track.id && !it.isPlaying && abs(it.positionMs - target) <= 1_000L
                }
            }
            assertTrue("R2 seek did not reach the requested position", abs(sought.positionMs - target) <= 1_000L)

            withContext(Dispatchers.Main.immediate) { controller.resume() }
            val resumed = withTimeout(10_000) {
                controller.state.first { it.currentTrackId == track.id && it.status == PlaybackStatus.PLAYING && it.positionMs > sought.positionMs }
            }
            assertTrue("R2 playback did not resume", resumed.isPlaying)
        } finally {
            withContext(Dispatchers.Main.immediate) {
                controller.stop()
                controller.release()
            }
        }
    }

    @Test
    fun mediaItemMapperAcceptsHttpsAndRejectsCleartext() = runBlocking {
        val track = remoteR2Track()
        val mapper = TrackMediaItemMapper(context)
        val mapped = mapper.map(track)

        assertNotNull(mapped)
        assertEquals(R2_TEST_AUDIO_URL, mapped?.localConfiguration?.uri.toString())
        assertEquals(track.id, mapped?.mediaId)
        assertNull(mapper.map(track.copy(audioSource = AudioSource.RemoteUrl("http://example.test/audio.mp3"))))
    }

    private suspend fun remoteR2Track(): Track {
        val local = LocalContentRepository()
        val fallbackTrack = local.getTrack("hanuman_chalisa") ?: error("Hanuman fixture is missing")
        val deity = local.getDeities().single { it.id == fallbackTrack.deityId }
        val repository = RemoteContentRepository(RemoteContentDataSource {
            RemoteCatalogDto(
                version = "phase18a-r2-smoke",
                deities = listOf(
                    RemoteDeityDto(
                        id = deity.id,
                        nameKey = deity.nameKey,
                        descriptionKey = deity.descriptionKey,
                        artworkKey = deity.artworkKey,
                        aliases = deity.aliases.toList(),
                        supportedCategories = deity.supportedCategories.map(DevotionalCategory::name)
                    )
                ),
                tracks = listOf(
                    RemoteTrackDto(
                        id = fallbackTrack.id,
                        titleEnglish = fallbackTrack.title.english,
                        titleHindi = fallbackTrack.title.hindi,
                        subtitleEnglish = fallbackTrack.subtitle.english,
                        subtitleHindi = fallbackTrack.subtitle.hindi,
                        artistEnglish = fallbackTrack.artistOrSource.english,
                        artistHindi = fallbackTrack.artistOrSource.hindi,
                        deityId = fallbackTrack.deityId,
                        category = fallbackTrack.category.name,
                        language = Language.HINDI.name,
                        durationMs = fallbackTrack.durationMs,
                        artworkKey = fallbackTrack.artworkKey,
                        audioUrl = R2_TEST_AUDIO_URL,
                        lyricsId = fallbackTrack.lyricsId,
                        keywords = fallbackTrack.keywords.toList(),
                        aliases = fallbackTrack.aliases.toList()
                    )
                )
            )
        }, fallback = local)
        return repository.getTrack(fallbackTrack.id) ?: error("Remote R2 fixture is missing")
    }
}
