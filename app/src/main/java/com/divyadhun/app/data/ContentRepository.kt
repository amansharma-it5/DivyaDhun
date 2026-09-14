package com.divyadhun.app.data

import com.divyadhun.app.domain.model.DevotionalCategory
import com.divyadhun.app.domain.model.Deity
import com.divyadhun.app.domain.model.Track
import com.divyadhun.app.domain.model.Lyrics

interface ContentRepository {
    suspend fun getDeities(): List<Deity>
    suspend fun getTracks(): List<Track>
    suspend fun getTrack(id: String): Track?
    suspend fun getLyrics(id: String): Lyrics?
    suspend fun getTracksForDeity(deityId: String): List<Track>
    suspend fun getTracksByCategory(category: DevotionalCategory): List<Track>
    suspend fun search(query: String): List<Track>
    suspend fun searchDeities(query: String): List<Deity>
}
