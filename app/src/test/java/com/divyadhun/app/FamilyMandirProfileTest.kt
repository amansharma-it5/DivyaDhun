package com.divyadhun.app

import com.divyadhun.app.domain.model.FamilyMandirProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyMandirProfileTest {
    @Test fun profileSanitizesSeparatorsDuplicatesAndMemberLimit() {
        val profile = FamilyMandirProfile("  घर|का मंदिर ", listOf("A", "A", " ", "B|C") + (1..10).map { "M$it" }).sanitized()
        assertEquals("घर का मंदिर", profile.name)
        assertEquals(listOf("A", "B C", "M1", "M2", "M3", "M4", "M5", "M6"), profile.memberNames)
        assertTrue(profile.isConfigured)
    }

    @Test fun emptyProfileIsUnconfigured() {
        assertFalse(FamilyMandirProfile().isConfigured)
    }
}
