package com.example.conscia.domain.usecase

import com.example.conscia.data.usage.UsageStatsRepository
import com.example.conscia.model.AppUsageInfo
import javax.inject.Inject

class GetTodayUsageUseCase @Inject constructor(
    private val repository: UsageStatsRepository
) {
    suspend operator fun invoke(): List<AppUsageInfo> = repository.getTodayUsage()
}
