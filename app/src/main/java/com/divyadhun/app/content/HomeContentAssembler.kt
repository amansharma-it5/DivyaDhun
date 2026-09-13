package com.divyadhun.app.content

import com.divyadhun.app.domain.model.Deity
import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.Track

/** Pure, deterministic assembly of the sections shown on Home. */
class HomeContentAssembler(
    private val deities: List<Deity>,
    private val tracks: List<Track>
) {
    fun assemble(preferredDeityIds: Set<String>): HomeContent {
        val preferred = deities.filter { it.id in preferredDeityIds }
        val yourGods = if (preferred.isEmpty()) deities.take(MAX_YOUR_GODS) else preferred
        val preferredTracks = tracks.filter { it.deityId in preferredDeityIds }
        val recommended = (preferredTracks + tracks.filter { it !in preferredTracks })
            .distinctBy { it.id }
            .take(MAX_TRACKS)
        val today = tracks.sortedBy { it.id }.take(MAX_TRACKS)
        return HomeContent(
            yourGods = yourGods,
            recommended = recommended,
            today = today,
            popularAartis = tracks.filter { it.category == DevotionalCategory.AARTI }.take(MAX_TRACKS),
            mantras = tracks.filter { it.category == DevotionalCategory.MANTRA }.take(MAX_TRACKS)
        )
    }

    private companion object {
        const val MAX_TRACKS = 4
        const val MAX_YOUR_GODS = 4
    }
}

data class HomeContent(
    val yourGods: List<Deity> = emptyList(),
    val recommended: List<Track> = emptyList(),
    val today: List<Track> = emptyList(),
    val popularAartis: List<Track> = emptyList(),
    val mantras: List<Track> = emptyList()
)
