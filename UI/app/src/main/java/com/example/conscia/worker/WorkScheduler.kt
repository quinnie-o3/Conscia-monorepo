package com.example.conscia.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {
    private const val WORK_NAME = "usage_limit_check_work"

    fun schedulePeriodicCheck(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<UsageLimitCheckWorker>(
            15, TimeUnit.MINUTES // Minimum interval for PeriodicWork
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
