package com.divyadhun.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PrivacyPolicyTest {
    @Test
    fun usesPublishedHttpsPolicyUrl() {
        assertEquals("https://amansharma-it5.github.io/divyadhun-privacy/", PRIVACY_POLICY_URL)
    }
}
