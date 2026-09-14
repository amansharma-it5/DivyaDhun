package com.divyadhun.app.domain.model

enum class MantraSessionStatus {
    IDLE, RUNNING, PAUSED, COMPLETE
}

data class MantraProgress(
    val targetCount: Int = 11,
    val completedCount: Int = 0,
    val status: MantraSessionStatus = MantraSessionStatus.IDLE
)

object MantraProgression {
    val supportedTargets: List<Int> = listOf(11, 21, 51, 108)

    fun selectTarget(progress: MantraProgress, target: Int): MantraProgress {
        require(target in supportedTargets)
        return MantraProgress(targetCount = target)
    }

    fun toggleRunning(progress: MantraProgress): MantraProgress = when (progress.status) {
        MantraSessionStatus.COMPLETE -> progress
        MantraSessionStatus.RUNNING -> progress.copy(status = MantraSessionStatus.PAUSED)
        MantraSessionStatus.IDLE, MantraSessionStatus.PAUSED -> progress.copy(status = MantraSessionStatus.RUNNING)
    }

    fun increment(progress: MantraProgress): MantraProgress {
        if (progress.status != MantraSessionStatus.RUNNING || progress.completedCount >= progress.targetCount) return progress
        val completed = progress.completedCount + 1
        return progress.copy(
            completedCount = completed,
            status = if (completed >= progress.targetCount) MantraSessionStatus.COMPLETE else MantraSessionStatus.RUNNING
        )
    }

    fun reset(progress: MantraProgress): MantraProgress = progress.copy(completedCount = 0, status = MantraSessionStatus.IDLE)
}
