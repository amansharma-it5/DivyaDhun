package com.divyadhun.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Phase6LibraryFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetPreferences() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        resetDivyaDhunState(context)
        rule.activityRule.scenario.recreate()
        waitForTag("screen_ONBOARDING")
    }

    @Test
    fun favoriteFromTrackRowAppearsInLibraryAndCanBeRemoved() {
        completeWithSkips()
        openSearchForHanuman()
        rule.onNodeWithTag("favorite_hanuman_mantra").performClick()
        rule.onNodeWithTag("tab_LIBRARY").performClick()
        waitForTag("screen_LIBRARY")
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag("favorite_hanuman_mantra"))
        rule.onAllNodesWithTag("favorite_hanuman_mantra")[0].assertIsDisplayed()
        rule.onAllNodesWithTag("favorite_hanuman_mantra")[0].performClick()
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag("favorites_empty_title"))
        rule.onNodeWithTag("favorites_empty_title").assertIsDisplayed()
    }

    @Test
    fun playerFavoriteAndRecentHistoryPersistAcrossLibraryNavigation() {
        completeWithSkips()
        openSearchForHanuman()
        rule.onNodeWithTag("play_hanuman_mantra", useUnmergedTree = true).performClick()
        waitForTag("mini_player")
        rule.onNodeWithTag("mini_player").performClick()
        waitForTag("screen_PLAYER")
        rule.onNodeWithTag("favorite_player").performClick()
        rule.onNodeWithTag("back_button").performClick()
        rule.onNodeWithTag("tab_LIBRARY").performClick()
        waitForTag("screen_LIBRARY")
        rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag("favorite_hanuman_mantra"))
        rule.onAllNodesWithTag("favorite_hanuman_mantra").assertCountEquals(2)
        rule.onNodeWithTag("clear_history").performClick()
        waitForScrolledTag("recently_played_empty")
        rule.onNodeWithTag("recently_played_empty").assertIsDisplayed()
    }

    private fun completeWithSkips() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        waitForTag("onboarding_continue")
        rule.onNodeWithTag("onboarding_continue").performClick()
        waitForTag("onboarding_skip")
        rule.onNodeWithTag("onboarding_skip").performClick()
        waitForTag("onboarding_skip")
        rule.onNodeWithTag("onboarding_skip").performClick()
        waitForTag("screen_HOME")
    }

    private fun openSearchForHanuman() {
        rule.onNodeWithTag("tab_SEARCH").performClick()
        waitForTag("search_field")
        rule.onNodeWithTag("search_field").performTextInput("Hanuman Mantra")
        waitForTag("favorite_hanuman_mantra")
    }

    private fun waitForTag(tag: String) {
        rule.waitUntil(15000) {
            runCatching { rule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }

    private fun waitForScrolledTag(tag: String) {
        rule.waitUntil(5000) {
            runCatching {
                rule.onNodeWithTag("screen_LIBRARY").performScrollToNode(hasTestTag(tag))
                true
            }.getOrDefault(false)
        }
    }
}
