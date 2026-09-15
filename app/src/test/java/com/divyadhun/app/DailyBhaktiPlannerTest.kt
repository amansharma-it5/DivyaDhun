package com.divyadhun.app

import com.divyadhun.app.data.LocalContentRepository
import com.divyadhun.app.domain.model.DailyBhaktiPlanner
import com.divyadhun.app.domain.model.ListeningTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyBhaktiPlannerTest {
    @Test fun routineUsesPreferredPlayableTracksInStableOrder() = runBlocking {
        val repository = LocalContentRepository()
        val routine = DailyBhaktiPlanner.plan(setOf("shiv"), ListeningTime.EVENING, repository.getTracks())
        assertEquals("evening_om_namah_shivaya", routine.id)
        assertEquals(listOf("om_namah_shivaya"), routine.trackIds)
    }

    @Test fun routineFallsBackToAllPlayableTracksAndIncludesRemoteFixture() = runBlocking {
        val routine = DailyBhaktiPlanner.plan(setOf("unknown"), ListeningTime.ANYTIME, LocalContentRepository().getTracks())
        assertTrue(routine.trackIds.isNotEmpty())
        assertTrue(routine.trackIds.contains("hanuman_chalisa"))
        assertTrue(routine.trackIds.size <= 3)
    }
}
