package com.divyadhun.app.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.divyadhun.app.R
import com.divyadhun.app.data.RemoteContentUrlPolicy
import com.divyadhun.app.domain.model.AudioSource
import com.divyadhun.app.domain.model.Track

class TrackMediaItemMapper(private val context: Context) {
    @Suppress("UseKtx")
    fun map(track: Track): MediaItem? {
        val source = track.audioSource ?: return null
        val uri = when (source) {
            is AudioSource.BundledResource -> {
                val resourceId = when (source.resourceName) {
                    "demo_ganesh_dhun" -> R.raw.demo_ganesh_dhun
                    "demo_hanuman_dhun" -> R.raw.demo_hanuman_dhun
                    "demo_krishna_dhun" -> R.raw.demo_krishna_dhun
                    "demo_ram_dhun" -> R.raw.demo_ram_dhun
                    "demo_shiv_dhun" -> R.raw.demo_shiv_dhun
                    else -> 0
                }
                if (resourceId == 0) return null
                Uri.parse("android.resource://${context.packageName}/$resourceId")
            }
            is AudioSource.RemoteUrl -> {
                val remoteUrl = RemoteContentUrlPolicy.validateAudioUrl(source.url) ?: return null
                Uri.parse(remoteUrl)
            }
        }
        return MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title.english)
                    .setArtist(track.artistOrSource.english)
                    .setAlbumTitle("DivyaDhun")
                    .setDescription(track.subtitle.english)
                    .build()
            )
            .build()
    }
}
