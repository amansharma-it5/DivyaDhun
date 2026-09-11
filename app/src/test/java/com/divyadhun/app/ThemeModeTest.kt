package com.divyadhun.app
import com.divyadhun.app.ui.theme.usesDark
import org.junit.Assert.*
import org.junit.Test
class ThemeModeTest {
 @Test fun invalidStoredValueFallsBackToSystem() { assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStored("invalid")); assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStored(null)) }
 @Test fun persistedModesRoundTrip() { ThemeMode.entries.forEach { assertEquals(it, ThemeMode.fromStored(it.name)) } }
 @Test fun explicitModesOverrideSystem() { assertTrue(ThemeMode.DARK.usesDark(false)); assertFalse(ThemeMode.LIGHT.usesDark(true)) }
 @Test fun systemModeFollowsDevice() { assertTrue(ThemeMode.SYSTEM.usesDark(true)); assertFalse(ThemeMode.SYSTEM.usesDark(false)) }
}

