package com.divyadhun.app

import com.divyadhun.app.data.LocalContentRepository
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.sharing.ShareBlessingCard
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareBlessingCardTest {
    @Test fun shareCopyUsesLocalizedTitleAndDeity() = runBlocking {
        val track = LocalContentRepository().getTrack("om_namah_shivaya")!!
        val english = ShareBlessingCard.message(track, "Shiv", Language.ENGLISH)
        val hindi = ShareBlessingCard.message(track, "शिव", Language.HINDI)

        assertTrue(english.contains("Om Namah Shivaya"))
        assertTrue(english.contains("Shiv"))
        assertTrue(hindi.contains("ॐ नमः शिवाय"))
        assertTrue(hindi.contains("शिव"))
    }
}
