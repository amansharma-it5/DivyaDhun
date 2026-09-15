package com.divyadhun.app

import com.divyadhun.app.data.LocalContentRepository
import com.divyadhun.app.domain.model.AudioSource
import com.divyadhun.app.playback.playableQueue
import com.divyadhun.app.playback.queueStartIndex
import com.divyadhun.app.playback.RepeatMode
import com.divyadhun.app.playback.nextRepeatMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackQueueTest {
    @Test
    fun queueFiltersMetadataOnlyTracksAndKeepsOrder() = runBlocking {
        val tracks = LocalContentRepository().getTracks()
        val queue = playableQueue(tracks)
        assertTrue(queue.isNotEmpty())
        assertTrue(queue.all { it.isPlayable })
        assertEquals(queue.map { it.id }, tracks.filter { it.isPlayable }.map { it.id })
    }

    @Test
    fun queueStartIndexAndBoundsAreDeterministic() = runBlocking {
        val queue = playableQueue(LocalContentRepository().getTracks())
        assertEquals(0, queueStartIndex(queue, queue.first().id))
        assertEquals(-1, queueStartIndex(queue, "missing"))
        assertEquals(queue.lastIndex, queueStartIndex(queue, queue.last().id))
    }

    @Test
    fun repeatModeCyclesWithoutSkippingStates() {
        assertEquals(RepeatMode.ALL, nextRepeatMode(RepeatMode.OFF))
        assertEquals(RepeatMode.ONE, nextRepeatMode(RepeatMode.ALL))
        assertEquals(RepeatMode.OFF, nextRepeatMode(RepeatMode.ONE))
    }

    @Test
    fun mixedQueueKeepsRemoteAndBundledSourcesButSkipsMetadataOnly() = runBlocking {
        val metadataOnly = LocalContentRepository().getTrack("hanuman_chalisa")!!
        val remote = metadataOnly.copy(audioSource = AudioSource.RemoteUrl("https://cdn.example.test/hanuman.m4a"))
        val bundled = LocalContentRepository().getTrack("hanuman_mantra")!!
        assertEquals(listOf(remote.id, bundled.id), playableQueue(listOf(remote, metadataOnly, bundled)).map { it.id })
    }
}
