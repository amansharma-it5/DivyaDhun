package com.divyadhun.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.divyadhun.app.data.UserPreferencesRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Phase2FlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun resetPreferences() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        runBlocking { UserPreferencesRepository(context).resetForTest() }
        rule.activityRule.scenario.recreate()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("screen_ONBOARDING").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun newUserCompletesHindiFlowAndRestartSkipsOnboarding() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        rule.onNodeWithTag("onboarding_language_HINDI").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("onboarding_language_HINDI").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("अपनी भाषा चुनें").assertIsDisplayed()
        rule.onNodeWithTag("onboarding_language_HINDI").assertIsSelected()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_deity_hanuman").performClick()
        rule.onNodeWithTag("onboarding_deity_shiv").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_time_MORNING").performClick()
        rule.onNodeWithTag("onboarding_finish").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("screen_HOME").fetchSemanticsNodes().isNotEmpty() }
        rule.onAllNodesWithText("आपकी दैनिक भक्ति").assertCountEquals(2)
        rule.onNodeWithText("हनुमान").assertIsDisplayed()
        rule.activityRule.scenario.recreate()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("screen_HOME").fetchSemanticsNodes().isNotEmpty() }
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
        rule.onNodeWithTag("search_field").performTextInput("mahadev")
        rule.waitUntil(10000) { rule.onAllNodesWithText("Om Namah Shivaya").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Om Namah Shivaya").assertIsDisplayed()
    }

    @Test fun preferencesCanBeRevisitedAfterOnboarding() {
        completeWithSkips()
        rule.onNodeWithTag("tab_LIBRARY").performClick()
        rule.onNodeWithTag("setting_language_HINDI").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("setting_language_HINDI").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("setting_deities").performClick()
        rule.onNodeWithTag("setting_deity_hanuman").performClick()
        rule.onNodeWithTag("setting_deities_done").performClick()
        rule.onNodeWithText("हनुमान").assertIsDisplayed()
        rule.onNodeWithTag("setting_time").performClick()
        rule.onNodeWithTag("setting_time_EVENING").performClick()
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag("theme_DARK"))
        rule.onNodeWithTag("theme_DARK").performClick()
        rule.activityRule.scenario.recreate()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("screen_LIBRARY").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("setting_language_HINDI").assertIsSelected()
        rule.onNodeWithText("हनुमान").assertIsDisplayed()
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag("theme_DARK"))
        rule.onNodeWithTag("theme_DARK").assertIsSelected()
    }

    private fun completeWithSkips() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("screen_HOME").fetchSemanticsNodes().isNotEmpty() }
    }
}
