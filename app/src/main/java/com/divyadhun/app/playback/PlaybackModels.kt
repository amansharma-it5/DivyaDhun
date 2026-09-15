package com.divyadhun.app.playback

import com.divyadhun.app.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

enum class PlaybackStatus {
    IDLE, LOADING, READY, PLAYING, PAUSED, ENDED, ERROR
}

enum class RepeatMode {
    OFF, ALL, ONE
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
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val errorMessage: String? = null,
    val networkAvailable: Boolean = true
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
    fun toggleRepeatMode()
    fun stop()
    fun retry()
    fun release()
}

fun playableQueue(tracks: List<Track>): List<Track> = tracks.filter { it.isPlayable }

fun queueStartIndex(queue: List<Track>, trackId: String): Int = queue.indexOfFirst { it.id == trackId }

fun nextRepeatMode(mode: RepeatMode): RepeatMode = when (mode) {
    RepeatMode.OFF -> RepeatMode.ALL
    RepeatMode.ALL -> RepeatMode.ONE
    RepeatMode.ONE -> RepeatMode.OFF
}
