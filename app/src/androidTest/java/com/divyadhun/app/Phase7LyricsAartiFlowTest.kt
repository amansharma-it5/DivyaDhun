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

class Phase7LyricsAartiFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetPreferences() {
        resetDivyaDhunState(ApplicationProvider.getApplicationContext())
        rule.activityRule.scenario.recreate()
        waitForTag("screen_ONBOARDING")
    }

    @Test
    fun aartiOpensTraditionalTextAndKeepAwakeControl() {
        completeWithSkips()
        rule.onNodeWithTag("tab_EXPLORE").performClick()
        waitForTag("screen_EXPLORE")
        rule.onNodeWithTag("category_AARTI").performClick()
        waitForTag("screen_CATEGORY_AARTI")
        rule.onNodeWithTag("track_hanuman_aarti").performClick()
        waitForTag("screen_TRACK_hanuman_aarti")
        rule.onNodeWithTag("screen_TRACK_hanuman_aarti").performScrollToNode(hasTestTag("track_open_lyrics"))
        rule.onNodeWithTag("track_open_lyrics").performClick()
        waitForTag("screen_LYRICS_hanuman_aarti")
        rule.onNodeWithTag("screen_LYRICS_hanuman_aarti").performScrollToNode(hasTestTag("lyrics_devanagari"))
        rule.onNodeWithTag("lyrics_devanagari").assertIsDisplayed()
        rule.onNodeWithTag("screen_LYRICS_hanuman_aarti").performScrollToNode(hasTestTag("lyrics_romanized"))
        rule.onNodeWithTag("lyrics_romanized").assertIsDisplayed()
        rule.onNodeWithTag("screen_LYRICS_hanuman_aarti").performScrollToNode(hasTestTag("lyrics_meaning"))
        rule.onNodeWithTag("lyrics_meaning").assertIsDisplayed()
        rule.onNodeWithTag("screen_LYRICS_hanuman_aarti").performScrollToNode(hasTestTag("keep_screen_awake"))
        rule.onNodeWithTag("keep_screen_awake").performClick()
        rule.onNodeWithTag("keep_screen_awake").assertIsOn()
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

    private fun waitForTag(tag: String) {
        rule.waitUntil(15000) {
            runCatching { rule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }
}
