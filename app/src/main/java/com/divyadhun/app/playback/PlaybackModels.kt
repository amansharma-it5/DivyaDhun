package com.divyadhun.app.playback

import com.divyadhun.app.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

enum class PlaybackStatus {
    IDLE, LOADING, READY, PLAYING, PAUSED, ENDED, ERROR
}

data class PlaybackState(
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val queue: List<String> = emptyList(),
    val currentIndex: Int = -1,
    val errorMessage: String? = null
)

interface PlaybackController {
    val state: StateFlow<PlaybackState>

    fun playTrack(track: Track, queue: List<Track> = listOf(track))
    fun pause()
    fun resume()
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun skipNext()
    fun skipPrevious()
    fun stop()
    fun release()
}

fun playableQueue(tracks: List<Track>): List<Track> = tracks.filter { it.isPlayable }

fun queueStartIndex(queue: List<Track>, trackId: String): Int = queue.indexOfFirst { it.id == trackId }
