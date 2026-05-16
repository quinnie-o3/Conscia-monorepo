package com.example.conscia.di

import android.content.Context
import com.example.conscia.data.TrackedAppsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideTrackedAppsDataStore(@ApplicationContext context: Context): TrackedAppsDataStore {
        return TrackedAppsDataStore(context)
    }
}
