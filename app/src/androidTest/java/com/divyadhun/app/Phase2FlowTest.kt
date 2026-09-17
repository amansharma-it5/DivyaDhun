package com.divyadhun.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Phase2FlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun resetPreferences() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        resetDivyaDhunState(context)
        rule.activityRule.scenario.recreate()
        waitForTag("screen_ONBOARDING")
    }

    @Test fun newUserCompletesHindiFlowAndRestartSkipsOnboarding() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        rule.onNodeWithTag("onboarding_language_HINDI").performClick()
        waitForTag("onboarding_language_HINDI")
        rule.onNodeWithText("अपनी भाषा चुनें").assertIsDisplayed()
        rule.onNodeWithTag("onboarding_language_HINDI").assertIsSelected()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_deity_hanuman").performClick()
        rule.onNodeWithTag("screen_ONBOARDING").performScrollToNode(hasTestTag("onboarding_deity_shiv"))
        rule.onNodeWithTag("onboarding_deity_shiv").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_time_MORNING").performClick()
        rule.onNodeWithTag("onboarding_finish").performClick()
        waitForTag("screen_HOME")
        rule.onAllNodesWithText("आपकी दैनिक भक्ति").assertCountEquals(2)
        rule.onNodeWithTag("screen_HOME").performScrollToNode(hasText("हनुमान"))
        rule.onNodeWithText("हनुमान").assertIsDisplayed()
        rule.activityRule.scenario.recreate()
        waitForTag("screen_HOME")
        rule.onNodeWithTag("screen_HOME").assertIsDisplayed()
        rule.onNodeWithTag("screen_ONBOARDING").assertDoesNotExist()
    }

    @Test fun skipPathStillProvidesMainNavigationAndSearch() {
        completeWithSkips()
        listOf("EXPLORE", "SEARCH", "LIBRARY", "HOME").forEach { destination ->
            rule.onNodeWithTag("tab_$destination").performClick()
            rule.onNodeWithTag("screen_$destination").assertIsDisplayed()
            rule.onNodeWithTag("tab_$destination").assertIsSelected()
        }
        rule.onNodeWithTag("tab_SEARCH").performClick()
        rule.onNodeWithTag("search_field").performTextInput("shiv")
        waitForText("Shiv", substring = true)
        rule.onAllNodesWithText("Shiv", substring = true)[0].assertIsDisplayed()
    }

    @Test fun preferencesCanBeRevisitedAfterOnboarding() {
        completeWithSkips()
        rule.onNodeWithTag("tab_LIBRARY").performClick()
        waitForTag("screen_LIBRARY")
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag("setting_deities"))
        rule.onNodeWithTag("setting_language_HINDI").performClick()
        waitForTag("setting_language_HINDI")
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag("setting_deities"))
        rule.onNodeWithTag("setting_deities").performClick()
        rule.onNodeWithTag("setting_deity_hanuman").performClick()
        rule.onNodeWithTag("setting_deities_done").performClick()
        waitForText("हनुमान")
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasText("हनुमान"))
        rule.onNodeWithText("हनुमान").assertIsDisplayed()
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag("setting_time"))
        rule.onNodeWithTag("setting_time").performClick()
        rule.onNodeWithTag("setting_time_EVENING").performClick()
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag("theme_DARK"))
        rule.onNodeWithTag("theme_DARK").performClick()
        rule.activityRule.scenario.recreate()
        waitForTag("screen_LIBRARY")
        rule.onNodeWithTag("setting_language_HINDI").assertIsSelected()
        waitForText("हनुमान")
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasText("हनुमान"))
        rule.onNodeWithText("हनुमान").assertIsDisplayed()
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag("theme_DARK"))
        rule.onNodeWithTag("theme_DARK").assertIsSelected()
    }

    private fun completeWithSkips() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        // The skip tag exists on both steps; wait for the step-3 marker before clicking it again.
        waitForTag("onboarding_time_ANYTIME")
        rule.onNodeWithTag("onboarding_skip").performClick()
        waitForTag("screen_HOME")
    }
    private fun waitForTag(tag: String) {
        rule.waitUntil(10000) {
            runCatching { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }

    private fun waitForText(text: String, substring: Boolean = false) {
        rule.waitUntil(10000) {
            runCatching { rule.onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }
}
