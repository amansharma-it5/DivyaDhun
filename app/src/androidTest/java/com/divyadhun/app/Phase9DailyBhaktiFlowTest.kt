package com.divyadhun.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Phase9DailyBhaktiFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetPreferences() {
        resetDivyaDhunState(ApplicationProvider.getApplicationContext())
        rule.activityRule.scenario.recreate()
        waitForTag("screen_ONBOARDING")
    }

    @Test
    fun dailyBhaktiOpensPlayableRoutineAndAdvances() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        waitForTag("onboarding_time_ANYTIME")
        rule.onNodeWithTag("onboarding_skip").performClick()
        waitForTag("screen_HOME")
        rule.onNodeWithTag("home_daily_bhakti_cta").performClick()
        waitForTag("screen_DAILY_BHAKTI")
        rule.onNodeWithTag("routine_progress").assertIsDisplayed()
        rule.onNodeWithTag("routine_next").performClick()
        rule.onNodeWithTag("routine_previous").performClick()
        rule.onNodeWithTag("routine_play_pause").performClick()
        rule.onNodeWithTag("routine_play_pause").assertIsDisplayed()
    }

    private fun waitForTag(tag: String) {
        rule.waitUntil(15000) {
            runCatching { rule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }
}
