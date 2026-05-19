package com.example.conscia.domain.usecase

import com.example.conscia.data.usage.UsageStatsRepository
import com.example.conscia.model.DailyUsagePoint
import javax.inject.Inject

class GetWeeklyUsageUseCase @Inject constructor(
    private val repository: UsageStatsRepository
) {
    suspend operator fun invoke(packageNames: Set<String> = emptySet()): List<DailyUsagePoint> =
        repository.getWeeklyUsageBreakdown(packageNames)
}
