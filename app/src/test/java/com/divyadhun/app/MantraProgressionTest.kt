package com.divyadhun.app

import com.divyadhun.app.domain.model.MantraProgress
import com.divyadhun.app.domain.model.MantraProgression
import com.divyadhun.app.domain.model.MantraSessionStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class MantraProgressionTest {
    @Test fun supportedTargetsAreStable() {
        assertEquals(listOf(11, 21, 51, 108), MantraProgression.supportedTargets)
    }

    @Test fun countOnlyAdvancesWhileRunningAndCompletesAtTarget() {
        val idle = MantraProgress(targetCount = 11)
        assertEquals(idle, MantraProgression.increment(idle))
        val running = MantraProgression.toggleRunning(idle)
        val almostDone = running.copy(completedCount = 10)
        val complete = MantraProgression.increment(almostDone)
        assertEquals(11, complete.completedCount)
        assertEquals(MantraSessionStatus.COMPLETE, complete.status)
        assertEquals(complete, MantraProgression.increment(complete))
    }

    @Test fun selectingAndResettingTargetClearsSession() {
        val active = MantraProgress(targetCount = 21, completedCount = 4, status = MantraSessionStatus.RUNNING)
        assertEquals(MantraProgress(targetCount = 51), MantraProgression.selectTarget(active, 51))
        assertEquals(MantraProgress(targetCount = 21), MantraProgression.reset(active))
    }
}
