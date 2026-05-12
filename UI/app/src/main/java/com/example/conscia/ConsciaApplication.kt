package com.example.conscia

import android.app.Application
import com.example.conscia.notification.ConsciaNotificationManager
import com.example.conscia.worker.WorkScheduler

class ConsciaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Notification Channels
        ConsciaNotificationManager(this).createNotificationChannels()
        
        // Start periodic usage checking
        WorkScheduler.schedulePeriodicCheck(this)
    }
}
