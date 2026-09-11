package com.divyadhun.app

import com.divyadhun.app.data.UserPreferences
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.ListeningTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesModelTest {
    @Test fun newUserStartsWithSafeDefaults() {
        val preferences = UserPreferences()
        assertTrue(!preferences.onboardingComplete)
        assertEquals(Language.ENGLISH, preferences.language)
        assertTrue(preferences.preferredDeityIds.isEmpty())
        assertEquals(ListeningTime.ANYTIME, preferences.listeningTime)
        assertEquals(ThemeMode.SYSTEM, preferences.theme)
    }

    @Test fun stablePreferenceEnumsRoundTrip() {
        Language.entries.forEach { assertEquals(it, Language.fromStored(it.name)) }
        ListeningTime.entries.forEach { assertEquals(it, ListeningTime.fromStored(it.name)) }
        assertEquals(Language.ENGLISH, Language.fromStored("unknown"))
        assertEquals(ListeningTime.ANYTIME, ListeningTime.fromStored(null))
    }
}
