package com.divyadhun.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Phase3FlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetPreferences() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        resetDivyaDhunState(context)
        rule.activityRule.scenario.recreate()
        waitForTag("screen_ONBOARDING")
    }

    @Test
    fun exploreCategoryDeityAndTrackDetailsHaveRealRoutes() {
        completeWithSkips()
        rule.onNodeWithTag("tab_EXPLORE").performClick()
        rule.onNodeWithTag("category_AARTI").performClick()
        waitForTag("screen_CATEGORY_AARTI")
        rule.onNodeWithTag("track_hanuman_aarti").performClick()
        waitForTag("screen_TRACK_hanuman_aarti")
        rule.onNodeWithTag("track_view_deity").performClick()
        waitForTag("screen_DEITY_hanuman")
        rule.onNodeWithTag("deity_category_AARTI").assertIsDisplayed()
    }

    @Test
    fun searchOpensTrackAndDeityDetails() {
        completeWithSkips()
        rule.onNodeWithTag("tab_SEARCH").performClick()
        rule.onNodeWithTag("search_field").performTextInput("mahadev")
        waitForTag("track_om_namah_shivaya")
        rule.onNodeWithTag("screen_SEARCH").performScrollToNode(hasTestTag("track_om_namah_shivaya"))
        rule.onNodeWithTag("track_title_om_namah_shivaya").performClick()
        waitForTag("screen_TRACK_om_namah_shivaya")
        rule.onNodeWithTag("screen_TRACK_om_namah_shivaya").performScrollToNode(hasTestTag("track_view_category"))
        rule.onNodeWithTag("track_view_category").assertIsDisplayed()
        rule.onNodeWithTag("back_button").performClick()
        waitForTag("screen_SEARCH")
        if (rule.onAllNodesWithTag("clear_search").fetchSemanticsNodes().isNotEmpty()) rule.onNodeWithTag("clear_search").performClick()
        rule.onNodeWithTag("search_field").performTextInput("mahadev")
        waitForTag("track_om_namah_shivaya")
        rule.onNodeWithTag("search_deity_shiv").performClick()
        waitForTag("screen_DEITY_shiv")
    }

    @Test
    fun preferredDeityAppearsInPersonalizedHome() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_deity_shiv").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_time_ANYTIME").performClick()
        rule.onNodeWithTag("onboarding_finish").performClick()
        waitForTag("screen_HOME")
        rule.onNodeWithTag("home_hero").assertIsDisplayed()
        rule.onNodeWithText("Shiv").assertIsDisplayed()
    }

    private fun completeWithSkips() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        waitForTag("screen_HOME")
    }
    private fun waitForTag(tag: String) {
        rule.waitUntil(10000) {
            runCatching { rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }
}
