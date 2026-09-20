package com.divyadhun.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import com.divyadhun.app.domain.model.AudioSource
import com.divyadhun.app.domain.model.DailyBhaktiRoutine
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.LocalizedText
import com.divyadhun.app.domain.model.Track
import com.divyadhun.app.playback.PlaybackController
import com.divyadhun.app.playback.PlaybackState
import com.divyadhun.app.ui.DailyBhaktiScreen
import com.divyadhun.app.ui.TrackRow
import com.divyadhun.app.ui.theme.DivyaTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReleaseRegressionLayoutTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun longTrackRowKeepsTitleInAUsableSingleLine() {
        val track = longTrack("long_row")
        rule.setContent {
            DivyaTheme {
                androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.width(320.dp)) {
                    TrackRow(
                        track = track,
                        language = Language.ENGLISH,
                        playbackController = FakePlaybackController(),
                        onClick = {},
                        onFavorite = {}
                    )
                }
            }
        }

        rule.onNodeWithTag("track_long_row").assertIsDisplayed()
        val title = rule.onNodeWithTag("track_title_long_row")
        title.assertWidthIsAtLeast(80.dp)
        val titleBounds = title.getBoundsInRoot()
        assertTrue("title must be bounded to one compact line", titleBounds.bottom - titleBounds.top <= 32.dp)
    }

    @Test
    fun dailyBhaktiCurrentTrackBoundsLongTitleOnNarrowViewport() {
        val track = longTrack("daily_long")
        val viewModel = com.divyadhun.app.content.DailyBhaktiViewModel()
        rule.setContent {
            DivyaTheme {
                androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.width(320.dp)) {
                    DailyBhaktiScreen(
                        viewModel = viewModel,
                        routine = DailyBhaktiRoutine("test", com.divyadhun.app.domain.model.ListeningTime.ANYTIME, listOf(track.id)),
                        tracks = listOf(track),
                        language = Language.ENGLISH,
                        playbackState = PlaybackState(),
                        playbackController = null,
                        onBack = {}
                    )
                }
            }
        }

        val title = rule.onNodeWithTag("daily_current_title")
        title.assertIsDisplayed()
        val titleBounds = title.getBoundsInRoot()
        assertTrue("daily title must not grow vertically without bound", titleBounds.bottom - titleBounds.top <= 56.dp)
    }

    private fun longTrack(id: String) = Track(
        id = id,
        title = LocalizedText(
            "A very long devotional title that must stay readable in a compact row",
            "बहुत लंबा भक्तिमय शीर्षक"
        ),
        subtitle = LocalizedText("A long supporting subtitle for narrow screens", "संक्षिप्त विवरण"),
        artistOrSource = LocalizedText("DivyaDhun", "दिव्यधुन"),
        deityId = "hanuman",
        category = DevotionalCategory.BHAJAN,
        language = Language.ENGLISH,
        artworkKey = "hanuman",
        audioSource = AudioSource.RemoteUrl("https://example.test/audio.mp3")
    )

    private class FakePlaybackController : PlaybackController {
        override val state = MutableStateFlow(PlaybackState())
        override fun playTrack(track: Track, queue: List<Track>) = Unit
        override fun pause() = Unit
        override fun resume() = Unit
        override fun togglePlayPause() = Unit
        override fun seekTo(positionMs: Long) = Unit
        override fun skipNext() = Unit
        override fun skipPrevious() = Unit
        override fun toggleRepeatMode() = Unit
        override fun stop() = Unit
        override fun retry() = Unit
        override fun release() = Unit
    }
}
