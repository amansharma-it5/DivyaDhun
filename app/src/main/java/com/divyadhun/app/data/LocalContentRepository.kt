package com.divyadhun.app.data

import com.divyadhun.app.domain.model.Deity
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.LocalizedText
import com.divyadhun.app.domain.model.Track
import java.util.Locale

class LocalContentRepository : ContentRepository {
    private val deities = listOf(
        Deity("hanuman", "deity_hanuman", "deity_hanuman_description", "hanuman", setOf("hanuman", "bajrangbali", "bajrang bali", "pawanputra", "हनुमान"), setOf(DevotionalCategory.AARTI, DevotionalCategory.BHAJAN, DevotionalCategory.MANTRA, DevotionalCategory.CHALISA)),
        Deity("shiv", "deity_shiv", "deity_shiv_description", "shiv", setOf("shiv", "shiva", "mahadev", "mahadeva", "bholenath", "शिव", "महादेव"), setOf(DevotionalCategory.AARTI, DevotionalCategory.BHAJAN, DevotionalCategory.MANTRA, DevotionalCategory.STOTRA)),
        Deity("krishna", "deity_krishna", "deity_krishna_description", "krishna", setOf("krishna", "kanha", "kanhaiya", "gopal", "कृष्ण"), setOf(DevotionalCategory.AARTI, DevotionalCategory.BHAJAN, DevotionalCategory.MANTRA)),
        Deity("ram", "deity_ram", "deity_ram_description", "ram", setOf("ram", "rama", "shri ram", "sri ram", "राम"), setOf(DevotionalCategory.AARTI, DevotionalCategory.BHAJAN, DevotionalCategory.MANTRA, DevotionalCategory.CHALISA)),
        Deity("ganesh", "deity_ganesh", "deity_ganesh_description", "ganesh", setOf("ganesh", "ganapati", "गणेश"), setOf(DevotionalCategory.AARTI, DevotionalCategory.BHAJAN, DevotionalCategory.MANTRA, DevotionalCategory.STOTRA)),
        Deity("durga", "deity_durga", "deity_durga_description", "durga", setOf("durga", "ambe", "jagdambe", "दुर्गा"), setOf(DevotionalCategory.AARTI, DevotionalCategory.BHAJAN, DevotionalCategory.MANTRA, DevotionalCategory.MEDITATION)),
        Deity("lakshmi", "deity_lakshmi", "deity_lakshmi_description", "lakshmi", setOf("lakshmi", "mahalakshmi", "लक्ष्मी"), setOf(DevotionalCategory.AARTI, DevotionalCategory.BHAJAN, DevotionalCategory.MANTRA, DevotionalCategory.STOTRA)),
        Deity("sai", "deity_sai", "deity_sai_description", "sai", setOf("sai", "saibaba", "sai baba", "साईं"), setOf(DevotionalCategory.AARTI, DevotionalCategory.BHAJAN, DevotionalCategory.MANTRA, DevotionalCategory.MEDITATION))
    )

    private val tracks = listOf(
        track("hanuman_chalisa", "Hanuman Chalisa", "हनुमान चालीसा", "A timeless prayer", "एक शाश्वत प्रार्थना", "hanuman", DevotionalCategory.CHALISA, setOf("hanuman", "bajrangbali", "हनुमान")),
        track("hanuman_aarti", "Aarti Kije Hanuman Lala Ki", "आरती कीजै हनुमान लला की", "Traditional prayer", "पारंपरिक प्रार्थना", "hanuman", DevotionalCategory.AARTI, setOf("aarti", "hanuman", "हनुमान")),
        track("bajrang_baan", "Bajrang Baan", "बजरंग बाण", "Traditional prayer", "पारंपरिक प्रार्थना", "hanuman", DevotionalCategory.BHAJAN, setOf("bajrang baan", "bajrangbali")),
        track("hanuman_mantra", "Hanuman Mantra", "हनुमान मंत्र", "DivyaDhun Demo", "दिव्यधुन डेमो", "hanuman", DevotionalCategory.MANTRA, setOf("mantra", "pawanputra")),
        track("om_namah_shivaya", "Om Namah Shivaya", "ॐ नमः शिवाय", "A meditative chant", "एक ध्यान मंत्र", "shiv", DevotionalCategory.MANTRA, setOf("shiv", "mahadev", "शिव", "महादेव")),
        track("mahamrityunjaya_mantra", "Mahamrityunjaya Mantra", "महामृत्युंजय मंत्र", "Traditional prayer", "पारंपरिक प्रार्थना", "shiv", DevotionalCategory.MANTRA, setOf("mrityunjaya", "mahadev")),
        track("shiv_aarti", "Shiv Aarti", "शिव आरती", "Traditional prayer", "पारंपरिक प्रार्थना", "shiv", DevotionalCategory.AARTI, setOf("aarti", "shiva")),
        track("shiv_tandav_stotram", "Shiv Tandav Stotram", "शिव तांडव स्तोत्रम्", "Traditional prayer", "पारंपरिक प्रार्थना", "shiv", DevotionalCategory.STOTRA, setOf("tandav", "stotram", "bholenath")),
        track("krishna_aarti", "Krishna Aarti", "कृष्ण आरती", "Traditional prayer", "पारंपरिक प्रार्थना", "krishna", DevotionalCategory.AARTI, setOf("aarti", "krishna", "कृष्ण")),
        track("achyutam_keshavam", "Achyutam Keshavam", "अच्युतम केशवम्", "Traditional prayer", "पारंपरिक प्रार्थना", "krishna", DevotionalCategory.BHAJAN, setOf("achyutam", "gopal")),
        track("hare_krishna_mantra", "Hare Krishna Mantra", "हरे कृष्ण मंत्र", "DivyaDhun Demo", "दिव्यधुन डेमो", "krishna", DevotionalCategory.MANTRA, setOf("hare krishna", "kanha")),
        track("gopal_bhajan", "Gopal Bhajan", "गोपाल भजन", "Traditional prayer", "पारंपरिक प्रार्थना", "krishna", DevotionalCategory.BHAJAN, setOf("gopal", "kanhaiya")),
        track("shri_ram_aarti", "Shri Ram Aarti", "श्री राम आरती", "Traditional prayer", "पारंपरिक प्रार्थना", "ram", DevotionalCategory.AARTI, setOf("aarti", "shri ram", "राम")),
        track("shri_ram_jai_ram", "Shri Ram Jai Ram Mantra", "श्री राम जय राम मंत्र", "A meditative chant", "एक ध्यान मंत्र", "ram", DevotionalCategory.MANTRA, setOf("mantra", "rama")),
        track("ram_bhajan", "Ram Bhajan", "राम भजन", "Traditional prayer", "पारंपरिक प्रार्थना", "ram", DevotionalCategory.BHAJAN, setOf("bhajan", "sri ram")),
        track("ram_stuti", "Ram Stuti", "राम स्तुति", "Traditional prayer", "पारंपरिक प्रार्थना", "ram", DevotionalCategory.CHALISA, setOf("stuti", "shri ram")),
        track("jai_ganesh_deva", "Jai Ganesh Deva", "जय गणेश देवा", "Traditional prayer", "पारंपरिक प्रार्थना", "ganesh", DevotionalCategory.AARTI, setOf("aarti", "ganesh", "गणेश")),
        track("vakratunda_mahakaya", "Vakratunda Mahakaya", "वक्रतुंड महाकाय", "Traditional prayer", "पारंपरिक प्रार्थना", "ganesh", DevotionalCategory.MANTRA, setOf("vakratunda", "ganapati")),
        track("ganesh_mantra", "Ganesh Mantra", "गणेश मंत्र", "DivyaDhun Demo", "दिव्यधुन डेमो", "ganesh", DevotionalCategory.MANTRA, setOf("mantra", "ganesh")),
        track("ganesh_bhajan", "Ganesh Bhajan", "गणेश भजन", "Traditional prayer", "पारंपरिक प्रार्थना", "ganesh", DevotionalCategory.BHAJAN, setOf("bhajan", "ganapati")),
        track("ambe_tu_hai", "Ambe Tu Hai Jagdambe Kali", "अंबे तू है जगदंबे काली", "Traditional prayer", "पारंपरिक प्रार्थना", "durga", DevotionalCategory.BHAJAN, setOf("durga", "ambe", "जगदंबे")),
        track("durga_aarti", "Durga Aarti", "दुर्गा आरती", "Traditional prayer", "पारंपरिक प्रार्थना", "durga", DevotionalCategory.AARTI, setOf("aarti", "durga")),
        track("ya_devi_sarva", "Ya Devi Sarva Bhuteshu", "या देवी सर्वभूतेषु", "Traditional prayer", "पारंपरिक प्रार्थना", "durga", DevotionalCategory.MANTRA, setOf("mantra", "devi")),
        track("durga_dhyan", "Durga Dhyan", "दुर्गा ध्यान", "A quiet meditation", "एक शांत ध्यान", "durga", DevotionalCategory.MEDITATION, setOf("meditation", "peace")),
        track("lakshmi_aarti", "Lakshmi Aarti", "लक्ष्मी आरती", "Traditional prayer", "पारंपरिक प्रार्थना", "lakshmi", DevotionalCategory.AARTI, setOf("aarti", "lakshmi", "लक्ष्मी")),
        track("mahalakshmi_mantra", "Mahalakshmi Mantra", "महालक्ष्मी मंत्र", "A meditative chant", "एक ध्यान मंत्र", "lakshmi", DevotionalCategory.MANTRA, setOf("mantra", "mahalakshmi")),
        track("lakshmi_bhajan", "Lakshmi Bhajan", "लक्ष्मी भजन", "Traditional prayer", "पारंपरिक प्रार्थना", "lakshmi", DevotionalCategory.BHAJAN, setOf("bhajan", "lakshmi")),
        track("shri_suktam", "Shri Suktam", "श्री सूक्तम्", "Traditional prayer", "पारंपरिक प्रार्थना", "lakshmi", DevotionalCategory.STOTRA, setOf("stotra", "suktam")),
        track("sai_aarti", "Sai Aarti", "साईं आरती", "Traditional prayer", "पारंपरिक प्रार्थना", "sai", DevotionalCategory.AARTI, setOf("aarti", "sai", "साईं")),
        track("sai_mantra", "Sai Mantra", "साईं मंत्र", "DivyaDhun Demo", "दिव्यधुन डेमो", "sai", DevotionalCategory.MANTRA, setOf("mantra", "saibaba")),
        track("sai_bhajan", "Sai Bhajan", "साईं भजन", "Traditional prayer", "पारंपरिक प्रार्थना", "sai", DevotionalCategory.BHAJAN, setOf("bhajan", "sai baba")),
        track("sai_shanti", "Sai Shanti Meditation", "साईं शांति ध्यान", "A quiet meditation", "एक शांत ध्यान", "sai", DevotionalCategory.MEDITATION, setOf("meditation", "peace"))
    )

    private fun track(id: String, english: String, hindi: String, subEnglish: String, subHindi: String, deityId: String, category: DevotionalCategory, aliases: Set<String>) = Track(
        id = id,
        title = LocalizedText(english, hindi),
        subtitle = LocalizedText(subEnglish, subHindi),
        artistOrSource = LocalizedText("Traditional", "पारंपरिक"),
        deityId = deityId,
        category = category,
        artworkKey = deityId,
        audioSource = null,
        keywords = setOf(category.name.lowercase(Locale.ROOT), deityId),
        aliases = aliases
    )

    override suspend fun getDeities() = deities
    override suspend fun getTracks() = tracks
    override suspend fun getTrack(id: String) = tracks.firstOrNull { it.id == id }
    override suspend fun getTracksForDeity(deityId: String) = tracks.filter { it.deityId == deityId }
    override suspend fun getTracksByCategory(category: DevotionalCategory) = tracks.filter { it.category == category }

    override suspend fun search(query: String): List<Track> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return emptyList()
        return tracks.filter { track ->
            val deity = deities.firstOrNull { it.id == track.deityId }
            val haystack = listOf(
                track.id, track.title.english, track.title.hindi, track.subtitle.english, track.subtitle.hindi,
                track.artistOrSource.english, track.artistOrSource.hindi, track.deityId,
                track.category.name, *track.keywords.toTypedArray(), *track.aliases.toTypedArray(), *(deity?.aliases?.toTypedArray() ?: emptyArray())
            ).joinToString(" ") { normalize(it) }
            haystack.contains(normalizedQuery)
        }
    }

    override suspend fun searchDeities(query: String): List<Deity> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return emptyList()
        return deities.filter { deity ->
            val haystack = (listOf(deity.id) + deity.aliases).joinToString(" ") { normalize(it) }
            haystack.contains(normalizedQuery)
        }
    }

    private fun normalize(value: String) = value.lowercase(Locale.ROOT)
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")
}
