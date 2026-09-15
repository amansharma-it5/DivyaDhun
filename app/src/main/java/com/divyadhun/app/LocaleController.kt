package com.divyadhun.app

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.divyadhun.app.domain.model.Language
import java.util.Locale

object LocaleController {
    @Suppress("DEPRECATION")
    fun apply(context: Context, language: Language, recreate: Boolean = true) {
        val current = context.resources.configuration.locales[0].language
        if (current == language.tag) return
        val locales = LocaleListCompatBridge.forLanguage(language.tag)
        if (Build.VERSION.SDK_INT >= 33 && recreate) {
            context.getSystemService(android.app.LocaleManager::class.java)?.applicationLocales = locales
            // LocaleManager applies the new application locale and schedules the
            // required Activity recreation itself on Android 13 and newer.
            // Calling recreate() here would race that lifecycle transition.
        } else {
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(Locale.forLanguageTag(language.tag))
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            if (recreate && context is Activity) context.recreate()
        }
    }
}

private object LocaleListCompatBridge {
    fun forLanguage(tag: String): android.os.LocaleList = android.os.LocaleList.forLanguageTags(tag)
}
