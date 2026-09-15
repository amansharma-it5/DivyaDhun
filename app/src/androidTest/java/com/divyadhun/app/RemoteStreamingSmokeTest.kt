package com.divyadhun.app

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.divyadhun.app.data.LocalContentRepository
import com.divyadhun.app.domain.model.AudioSource
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

    @Test
    fun r2TrackPreparesAdvancesAndSupportsPauseResumeAndSeek() = runBlocking {
        context.stopService(Intent(context, PlaybackService::class.java))
        val repository = LocalContentRepository()
        val track = repository.getTrack("hanuman_chalisa") ?: error("Phase 18A fixture is missing")
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
        val track = LocalContentRepository().getTrack("hanuman_chalisa")!!
        val mapper = TrackMediaItemMapper(context)
        val mapped = mapper.map(track)

        assertNotNull(mapped)
        assertEquals("https://pub-867b1331c5b9468db9b3fce509773119.r2.dev/audio/hanuman/Hanuman%20Chalisa.mp3", mapped?.localConfiguration?.uri.toString())
        assertEquals(track.id, mapped?.mediaId)
        assertNull(mapper.map(track.copy(audioSource = AudioSource.RemoteUrl("http://example.test/audio.mp3"))))
    }
}
