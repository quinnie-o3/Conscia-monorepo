package com.example.conscia

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.conscia.notification.ConsciaNotificationManager
import com.example.conscia.worker.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ConsciaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Notification Channels
        ConsciaNotificationManager(this).createNotificationChannels()
        
        // Start periodic usage checking
        WorkScheduler.schedulePeriodicCheck(this)
    }
}
