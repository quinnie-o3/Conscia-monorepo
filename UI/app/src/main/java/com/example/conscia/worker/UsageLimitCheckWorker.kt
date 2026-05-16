package com.example.conscia.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.conscia.data.remote.RemoteUsageSyncRepository
import com.example.conscia.data.weekly.WeeklySummaryManager
import com.example.conscia.domain.usecase.CheckUsageLimitWarningsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UsageLimitCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val checkWarningsUseCase: CheckUsageLimitWarningsUseCase,
    private val weeklySummaryManager: WeeklySummaryManager,
    private val remoteSyncRepository: RemoteUsageSyncRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Check for usage limits and show notifications
            checkWarningsUseCase.execute()
            
            // 2. Refresh weekly summary if needed
            weeklySummaryManager.refreshSnapshotIfNeeded()
            
            // 3. Sync data to backend
            runCatching {
                remoteSyncRepository.syncRecentUsage()
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
