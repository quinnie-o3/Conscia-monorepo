package com.example.conscia.monitoring

import android.content.Context

object PurposeGateStore {
    private const val PREF_NAME = "purpose_gate"
    private const val KEY_ALLOWED_PACKAGE = "allowed_package"

    fun allowCurrentSession(context: Context, packageName: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ALLOWED_PACKAGE, packageName)
            .apply()
    }

    fun isAllowedForCurrentSession(context: Context, packageName: String): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ALLOWED_PACKAGE, null) == packageName
    }

    fun clearIfDifferentPackage(context: Context, packageName: String) {
        val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val allowedPackage = preferences.getString(KEY_ALLOWED_PACKAGE, null)
        if (allowedPackage != null && allowedPackage != packageName) {
            preferences.edit().remove(KEY_ALLOWED_PACKAGE).apply()
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_ALLOWED_PACKAGE)
            .apply()
    }
}
