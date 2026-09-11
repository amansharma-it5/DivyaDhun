package com.divyadhun.app.ui

import androidx.annotation.StringRes
import com.divyadhun.app.R
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.ListeningTime

@StringRes fun deityNameRes(id: String) = when (id) {
    "hanuman" -> R.string.deity_hanuman
    "shiv" -> R.string.deity_shiv
    "krishna" -> R.string.deity_krishna
    "ram" -> R.string.deity_ram
    "ganesh" -> R.string.deity_ganesh
    "durga" -> R.string.deity_durga
    "lakshmi" -> R.string.deity_lakshmi
    "sai" -> R.string.deity_sai
    else -> R.string.unknown_deity
}

@StringRes fun deityDescriptionRes(id: String) = when (id) {
    "hanuman" -> R.string.deity_hanuman_description
    "shiv" -> R.string.deity_shiv_description
    "krishna" -> R.string.deity_krishna_description
    "ram" -> R.string.deity_ram_description
    "ganesh" -> R.string.deity_ganesh_description
    "durga" -> R.string.deity_durga_description
    "lakshmi" -> R.string.deity_lakshmi_description
    "sai" -> R.string.deity_sai_description
    else -> R.string.deity_generic_description
}

@StringRes fun categoryNameRes(category: DevotionalCategory) = when (category) {
    DevotionalCategory.AARTI -> R.string.category_aarti
    DevotionalCategory.BHAJAN -> R.string.category_bhajan
    DevotionalCategory.MANTRA -> R.string.category_mantra
    DevotionalCategory.CHALISA -> R.string.category_chalisa
    DevotionalCategory.STOTRA -> R.string.category_stotra
    DevotionalCategory.MEDITATION -> R.string.category_meditation
}

@StringRes fun listeningTimeNameRes(time: ListeningTime) = when (time) {
    ListeningTime.MORNING -> R.string.time_morning
    ListeningTime.EVENING -> R.string.time_evening
    ListeningTime.BEFORE_SLEEP -> R.string.time_before_sleep
    ListeningTime.ANYTIME -> R.string.time_anytime
}

fun Language.other() = if (this == Language.ENGLISH) Language.HINDI else Language.ENGLISH
