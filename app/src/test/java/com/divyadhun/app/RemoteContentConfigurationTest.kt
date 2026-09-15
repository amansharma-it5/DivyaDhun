package com.divyadhun.app

import com.divyadhun.app.data.RemoteContentConfiguration
import com.divyadhun.app.data.RemoteContentUrlPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteContentConfigurationTest {
    @Test fun baseUrlResolutionIsCentralizedAndReplaceable() {
        val config = RemoteContentConfiguration("https://audio.divyadhun.com", "https://art.divyadhun.com/")
        assertEquals("https://audio.divyadhun.com/audio/shiv/om-namah-shivaya.m4a", config.audioUrl("audio/shiv/om-namah-shivaya.m4a"))
        assertEquals("https://art.divyadhun.com/shiv.png", config.artworkUrl("shiv.png"))
    }

    @Test fun urlPolicyRejectsMalformedAndCleartextValues() {
        assertNull(RemoteContentUrlPolicy.validateAudioUrl("http://cdn.example/audio.mp3"))
        assertNull(RemoteContentUrlPolicy.validateAudioUrl("https:///missing-host.m4a"))
        assertEquals("https://cdn.example/audio.aac", RemoteContentUrlPolicy.validateAudioUrl(" https://cdn.example/audio.aac "))
    }
}
