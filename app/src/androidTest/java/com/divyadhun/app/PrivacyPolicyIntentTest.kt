package com.divyadhun.app

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPolicyIntentTest {
    @Test
    fun publishedPolicyIntentTargetsBrowsableHttpsUrl() {
        val intent = privacyPolicyIntent()
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(Uri.parse(PRIVACY_POLICY_URL), intent.data)
        assertTrue(intent.categories.orEmpty().contains(Intent.CATEGORY_BROWSABLE))
        assertNotNull(ApplicationProvider.getApplicationContext<android.content.Context>().packageManager.resolveActivity(intent, 0))
    }
}
