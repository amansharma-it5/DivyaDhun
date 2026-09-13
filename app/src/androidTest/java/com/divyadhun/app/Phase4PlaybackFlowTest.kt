package com.divyadhun.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.divyadhun.app.data.UserPreferencesRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Phase4PlaybackFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetPreferences() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        runBlocking { UserPreferencesRepository(context).resetForTest() }
        rule.activityRule.scenario.recreate()
        waitForTag("screen_ONBOARDING")
    }

    @Test
    fun playableSearchResultStartsAndShowsMiniPlayer() {
        completeWithSkips()
        rule.onNodeWithTag("tab_SEARCH").performClick()
        rule.onNodeWithTag("search_field").performTextInput("Hanuman Mantra")
        waitForTag("play_hanuman_mantra")
        rule.onNodeWithTag("play_hanuman_mantra", useUnmergedTree = true).performClick()
        waitForTag("mini_player")
        rule.onNodeWithTag("mini_player").assertIsDisplayed()
        rule.onNodeWithTag("mini_player_play_pause").performClick()
        rule.onNodeWithTag("mini_player_play_pause").performClick()
        rule.onNodeWithTag("mini_player_stop").performClick()
        waitForTag("screen_SEARCH")
    }

    private fun completeWithSkips() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        waitForTag("screen_HOME")
    }

    private fun waitForTag(tag: String) {
        rule.waitUntil(15000) {
            runCatching { rule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }
}
