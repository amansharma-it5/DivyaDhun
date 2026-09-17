package com.divyadhun.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
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

class Phase10SimpleModeFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetPreferences() {
        resetDivyaDhunState(ApplicationProvider.getApplicationContext())
        rule.activityRule.scenario.recreate()
        waitForTag("screen_ONBOARDING")
    }

    @Test
    fun simpleModePersistsAndHomeRemainsUsable() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        waitForTag("onboarding_time_ANYTIME")
        rule.onNodeWithTag("onboarding_skip").performClick()
        waitForTag("screen_HOME")
        rule.onNodeWithTag("tab_LIBRARY").performClick()
        waitForTag("screen_LIBRARY")
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag("simple_mode_toggle"))
        rule.onNodeWithTag("simple_mode_toggle").performClick()
        rule.onNodeWithTag("simple_mode_toggle").assertIsOn()
        rule.activityRule.scenario.recreate()
        waitForTag("screen_LIBRARY")
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag("simple_mode_toggle"))
        rule.onNodeWithTag("simple_mode_toggle").assertIsOn()
        rule.onNodeWithTag("tab_HOME").performClick()
        waitForTag("screen_HOME")
        rule.onNodeWithTag("home_hero").assertIsDisplayed()
    }

    private fun waitForTag(tag: String) {
        rule.waitUntil(15000) {
            runCatching { rule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }
}
