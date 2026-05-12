package com.example.conscia.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.conscia.model.TrackedAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val recommendedPackages = setOf(
        "com.zhiliaoapp.musically", 
        "com.instagram.android", 
        "com.facebook.katana",
        "com.google.android.youtube", 
        "com.twitter.android", 
        "com.discord",
        "com.netflix.mediaclient",
        "com.snapchat.android"
    )

    suspend fun getInstalledApps(): List<TrackedAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        
        resolveInfos.map { resolveInfo ->
            val appInfo = resolveInfo.activityInfo.applicationInfo
            TrackedAppInfo(
                appName = pm.getApplicationLabel(appInfo).toString(),
                packageName = appInfo.packageName,
                isRecommended = recommendedPackages.contains(appInfo.packageName)
            )
        }.distinctBy { it.packageName }.sortedBy { it.appName }
    }
}
