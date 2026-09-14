package com.divyadhun.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Phase8MantraFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetPreferences() {
        resetDivyaDhunState(ApplicationProvider.getApplicationContext())
        rule.activityRule.scenario.recreate()
        waitForTag("screen_ONBOARDING")
    }

    @Test
    fun mantraModeSupportsTargetPauseCountAndReset() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        waitForTag("screen_HOME")
        rule.onNodeWithTag("tab_EXPLORE").performClick()
        waitForTag("screen_EXPLORE")
        rule.onNodeWithTag("category_MANTRA").performClick()
        waitForTag("screen_CATEGORY_MANTRA")
        rule.onNodeWithTag("screen_CATEGORY_MANTRA").performScrollToNode(hasTestTag("track_om_namah_shivaya"))
        rule.onNodeWithTag("track_title_om_namah_shivaya").performClick()
        waitForTag("screen_TRACK_om_namah_shivaya")
        rule.onNodeWithTag("screen_TRACK_om_namah_shivaya").performScrollToNode(hasTestTag("track_open_mantra"))
        rule.onNodeWithTag("track_open_mantra").performClick()
        waitForTag("screen_MANTRA_om_namah_shivaya")
        rule.onNodeWithTag("mantra_target_21").performClick()
        rule.onNodeWithTag("mantra_toggle_running").performClick()
        rule.onNodeWithTag("mantra_count_repetition").performClick()
        rule.onNodeWithTag("mantra_progress").assertTextEquals("1 of 21 repetitions")
        rule.onNodeWithTag("mantra_toggle_running").performClick()
        rule.onNodeWithTag("mantra_reset").performClick()
        rule.onNodeWithTag("mantra_progress").assertTextEquals("0 of 21 repetitions")
        rule.onNodeWithTag("mantra_progress").assertIsDisplayed()
    }

    private fun waitForTag(tag: String) {
        rule.waitUntil(15000) {
            runCatching { rule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }
}
