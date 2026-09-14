package com.divyadhun.app

import androidx.compose.ui.test.assertIsDisplayed
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

class Phase11FestivalFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetPreferences() {
        resetDivyaDhunState(ApplicationProvider.getApplicationContext())
        rule.activityRule.scenario.recreate()
        waitForTag("screen_ONBOARDING")
    }

    @Test
    fun homeFestivalCardOpensReusableDetail() {
        rule.onNodeWithTag("onboarding_get_started").performClick()
        rule.onNodeWithTag("onboarding_continue").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        rule.onNodeWithTag("onboarding_skip").performClick()
        waitForTag("screen_HOME")
        rule.onNodeWithTag("screen_HOME").performScrollToNode(hasTestTag("festival_diwali"))
        rule.onNodeWithTag("festival_diwali").performClick()
        waitForTag("screen_FESTIVAL_diwali")
        rule.onNodeWithTag("screen_FESTIVAL_diwali").performScrollToNode(hasTestTag("festival_collection"))
        rule.onNodeWithTag("festival_collection").assertIsDisplayed()
    }

    private fun waitForTag(tag: String) {
        rule.waitUntil(15000) {
            runCatching { rule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }
}
