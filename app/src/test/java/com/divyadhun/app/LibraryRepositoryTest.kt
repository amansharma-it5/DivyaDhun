package com.divyadhun.app

import com.divyadhun.app.data.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryRepositoryTest {
    @Test
    fun recentTrackEncodingPreservesOrderAndRemovesDuplicates() {
        val encoded = UserPreferencesRepository.encodeIds(listOf("track_c", "track_a", "track_c"))
        assertEquals(listOf("track_c", "track_a"), UserPreferencesRepository.decodeIds(encoded))
    }

    @Test
    fun missingRecentTrackStorageUsesEmptyHistory() {
        assertEquals(emptyList<String>(), UserPreferencesRepository.decodeIds(null))
        assertEquals(emptyList<String>(), UserPreferencesRepository.decodeIds(""))
    }
}
