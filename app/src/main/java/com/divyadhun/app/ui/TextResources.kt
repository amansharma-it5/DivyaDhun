package com.divyadhun.app.ui

import androidx.annotation.StringRes
import com.divyadhun.app.R
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.ContentAvailability
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.ListeningTime
import com.divyadhun.app.content.DeitySection

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

@StringRes fun categoryDescriptionRes(category: DevotionalCategory) = when (category) {
    DevotionalCategory.AARTI -> R.string.category_aarti_description
    DevotionalCategory.BHAJAN -> R.string.category_bhajan_description
    DevotionalCategory.MANTRA -> R.string.category_mantra_description
    DevotionalCategory.CHALISA -> R.string.category_chalisa_description
    DevotionalCategory.STOTRA -> R.string.category_stotra_description
    DevotionalCategory.MEDITATION -> R.string.category_meditation_description
}

@StringRes fun availabilityNameRes(availability: ContentAvailability) = when (availability) {
    ContentAvailability.METADATA_ONLY -> R.string.availability_metadata_only
    ContentAvailability.BUNDLED_DEMO -> R.string.availability_bundled_demo
    ContentAvailability.REMOTE_PRODUCTION -> R.string.availability_remote_stream
    ContentAvailability.UNAVAILABLE -> R.string.availability_unavailable
}

@StringRes fun deityInvocationRes(id: String) = when (id) {
    "hanuman" -> R.string.invocation_hanuman
    "shiv" -> R.string.invocation_shiv
    "krishna" -> R.string.invocation_krishna
    "ram" -> R.string.invocation_ram
    "ganesh" -> R.string.invocation_ganesh
    "durga" -> R.string.invocation_durga
    "lakshmi" -> R.string.invocation_lakshmi
    "sai" -> R.string.invocation_sai
    else -> R.string.invocation_generic
}

@StringRes fun deitySectionNameRes(section: DeitySection) = when (section) {
    DeitySection.POPULAR -> R.string.section_popular
    DeitySection.AARTIS -> R.string.section_aartis
    DeitySection.MANTRAS -> R.string.section_mantras
    DeitySection.CHALISA_STOTRA -> R.string.section_chalisa_stotra
    DeitySection.BHAJANS -> R.string.section_bhajans
}

@StringRes fun listeningTimeNameRes(time: ListeningTime) = when (time) {
    ListeningTime.MORNING -> R.string.time_morning
    ListeningTime.EVENING -> R.string.time_evening
    ListeningTime.BEFORE_SLEEP -> R.string.time_before_sleep
    ListeningTime.ANYTIME -> R.string.time_anytime
}

fun Language.other() = if (this == Language.ENGLISH) Language.HINDI else Language.ENGLISH

@StringRes fun festivalNameRes(id: String) = when (id) {
    "diwali" -> R.string.festival_diwali
    "janmashtami" -> R.string.festival_janmashtami
    "hanuman_jayanti" -> R.string.festival_hanuman_jayanti
    else -> R.string.festival_unknown
}

@StringRes fun festivalDescriptionRes(id: String) = when (id) {
    "diwali" -> R.string.festival_diwali_description
    "janmashtami" -> R.string.festival_janmashtami_description
    "hanuman_jayanti" -> R.string.festival_hanuman_jayanti_description
    else -> R.string.festival_unknown_description
}
