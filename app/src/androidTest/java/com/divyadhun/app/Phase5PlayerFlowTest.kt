package com.divyadhun.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.divyadhun.app.data.UserPreferencesRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Phase5PlayerFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetPreferences() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        runBlocking { UserPreferencesRepository(context).resetForTest() }
        rule.activityRule.scenario.recreate()
        waitForTag("screen_ONBOARDING")
    }

    @Test
    fun miniPlayerOpensFullPlayerAndExposesTransportControls() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        waitForTag("screen_HOME")
        rule.onNodeWithTag("tab_SEARCH").performClick()
        rule.onNodeWithTag("search_field").performTextInput("Hanuman Mantra")
        waitForTag("play_hanuman_mantra")
        rule.onNodeWithTag("play_hanuman_mantra", useUnmergedTree = true).performClick()
        waitForTag("mini_player")
        rule.onNodeWithTag("mini_player").performClick()
        waitForTag("screen_PLAYER")
        rule.onNodeWithTag("player_title").assertIsDisplayed()
        rule.onNodeWithTag("player_play_pause").performClick()
        rule.onNodeWithTag("player_previous").performClick()
        rule.onNodeWithTag("player_next").performClick()
        rule.onNodeWithTag("player_repeat").performClick()
        rule.onNodeWithTag("screen_PLAYER").performScrollToNode(hasTestTag("queue_hanuman_mantra"))
        rule.onNodeWithTag("queue_hanuman_mantra").assertIsDisplayed()
    }

    private fun waitForTag(tag: String) {
        rule.waitUntil(15000) {
            runCatching { rule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }
}
