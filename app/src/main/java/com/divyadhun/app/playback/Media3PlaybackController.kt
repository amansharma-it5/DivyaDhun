package com.divyadhun.app.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel

class Media3PlaybackController(context: Context) : PlaybackController {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mapper = TrackMediaItemMapper(appContext)
    private val mutableState = MutableStateFlow(PlaybackState())
    private var controller: MediaController? = null
    private var connectionJob: Job? = null
    private var pendingAction: ((MediaController) -> Unit)? = null

    override val state = mutableState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = publish(player)
        override fun onPlayerError(error: PlaybackException) {
            mutableState.value = mutableState.value.copy(status = PlaybackStatus.ERROR, isPlaying = false, errorMessage = "Unable to play this track.")
        }
    }

    init {
        val token = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        val future = MediaController.Builder(appContext, token).buildAsync()
        connectionJob = scope.launch(Dispatchers.IO) {
            runCatching {
                val connected = future.get()
                withContext(Dispatchers.Main.immediate) {
                    controller = connected
                    connected.addListener(listener)
                    publish(connected)
                    pendingAction?.invoke(connected)
                    pendingAction = null
                    while (isActive) {
                        publish(connected)
                        delay(750L)
                    }
                }
            }.onFailure {
                mutableState.value = mutableState.value.copy(status = PlaybackStatus.ERROR, errorMessage = "Playback is unavailable right now.")
            }
        }
    }

    override fun playTrack(track: com.divyadhun.app.domain.model.Track, queue: List<com.divyadhun.app.domain.model.Track>) {
        val playable = playableQueue(queue).ifEmpty { playableQueue(listOf(track)) }
        val mapped = playable.mapNotNull { candidate -> mapper.map(candidate)?.let { candidate to it } }
        if (mapped.isEmpty()) return
        val start = mapped.indexOfFirst { it.first.id == track.id }.coerceAtLeast(0)
        withController { mediaController ->
            mediaController.setMediaItems(mapped.map { it.second }, start, 0L)
            mediaController.prepare()
            mediaController.play()
        }
    }

    override fun pause() = withController { it.pause() }
    override fun resume() = withController { it.play() }
    override fun togglePlayPause() = withController { if (it.isPlaying) it.pause() else it.play() }
    override fun seekTo(positionMs: Long) = withController { it.seekTo(positionMs.coerceAtLeast(0L)) }
    override fun skipNext() = withController { if (it.hasNextMediaItem()) it.seekToNextMediaItem() }
    override fun skipPrevious() = withController { if (it.hasPreviousMediaItem()) it.seekToPreviousMediaItem() else it.seekTo(0L) }
    override fun toggleRepeatMode() = withController {
        it.repeatMode = when (it.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        publish(it)
    }
    override fun stop() = withController { it.stop(); it.clearMediaItems() }

    private fun withController(action: (MediaController) -> Unit) {
        val connected = controller
        if (connected == null) pendingAction = action else action(connected)
    }

    private fun publish(player: Player) {
        val status = when {
            player.playerError != null -> PlaybackStatus.ERROR
            player.playbackState == Player.STATE_BUFFERING -> PlaybackStatus.LOADING
            player.playbackState == Player.STATE_ENDED -> PlaybackStatus.ENDED
            player.playbackState == Player.STATE_READY && player.isPlaying -> PlaybackStatus.PLAYING
            player.playbackState == Player.STATE_READY -> PlaybackStatus.PAUSED
            else -> PlaybackStatus.IDLE
        }
        mutableState.value = PlaybackState(
            currentTrackId = player.currentMediaItem?.mediaId,
            isPlaying = player.isPlaying,
            status = status,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it > 0L } ?: 0L,
            bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L),
            queue = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).mediaId },
            currentIndex = player.currentMediaItemIndex,
            repeatMode = when (player.repeatMode) {
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                else -> RepeatMode.OFF
            },
            errorMessage = if (status == PlaybackStatus.ERROR) "Unable to play this track." else null
        )
    }

    override fun release() {
        connectionJob?.cancel()
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        scope.cancel()
    }
}
