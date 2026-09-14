package com.divyadhun.app

import android.content.Context
import android.content.Intent
import com.divyadhun.app.data.UserPreferencesRepository
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.playback.PlaybackService
import kotlinx.coroutines.runBlocking

/** Keeps instrumentation flows independent from playback and locale state left by another test. */
fun resetDivyaDhunState(context: Context) {
    context.stopService(Intent(context, PlaybackService::class.java))
    runBlocking { UserPreferencesRepository(context).resetForTest() }
    LocaleController.apply(context, Language.ENGLISH, recreate = false)
}
