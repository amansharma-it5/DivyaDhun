package com.divyadhun.app
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
class NavigationTest {
 @get:Rule val rule = createAndroidComposeRule<MainActivity>()
 @Test fun allDestinationsWorkAndRestoreAfterRecreation() {
  rule.waitUntil(10000) { rule.onAllNodesWithTag("tab_HOME").fetchSemanticsNodes().isNotEmpty() }
  listOf("EXPLORE","SEARCH","LIBRARY","HOME","SEARCH").forEach {
   rule.onNodeWithTag("tab_" + it).performClick()
   rule.onNodeWithTag("screen_" + it).assertIsDisplayed()
   rule.onNodeWithTag("tab_" + it).assertIsSelected()
  }
  rule.activityRule.scenario.recreate()
  rule.waitUntil(10000) { rule.onAllNodesWithTag("screen_SEARCH").fetchSemanticsNodes().isNotEmpty() }
  rule.onNodeWithTag("screen_SEARCH").assertIsDisplayed()
 }
 @Test fun appearanceSurvivesRecreation() {
  rule.waitUntil(10000) { rule.onAllNodesWithTag("tab_LIBRARY").fetchSemanticsNodes().isNotEmpty() }
  rule.onNodeWithTag("tab_LIBRARY").performClick()
  rule.onNodeWithTag("theme_DARK").performScrollTo().performClick()
  rule.waitForIdle()
  rule.activityRule.scenario.recreate()
  rule.waitUntil(10000) { rule.onAllNodesWithTag("screen_LIBRARY").fetchSemanticsNodes().isNotEmpty() }
  rule.onNodeWithTag("theme_DARK").performScrollTo()
  rule.onNodeWithTag("theme_DARK").assertIsSelected()
  rule.onNodeWithTag("theme_SYSTEM").performScrollTo().performClick()
 }
}
