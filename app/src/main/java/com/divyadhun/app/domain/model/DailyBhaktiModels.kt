package com.divyadhun.app.domain.model

data class DailyBhaktiRoutine(
    val id: String,
    val listeningTime: ListeningTime,
    val trackIds: List<String>
)

object DailyBhaktiPlanner {
    fun plan(preferredDeityIds: Set<String>, listeningTime: ListeningTime, tracks: List<Track>): DailyBhaktiRoutine {
        val playable = tracks.filter { it.isPlayable }
        val preferred = playable.filter { it.deityId in preferredDeityIds }
        val ordered = (if (preferred.isNotEmpty()) preferred else playable).sortedBy { it.id }.take(3)
        return DailyBhaktiRoutine(
            id = "${listeningTime.name.lowercase()}_${ordered.joinToString("_") { it.id }}",
            listeningTime = listeningTime,
            trackIds = ordered.map { it.id }
        )
    }
}
