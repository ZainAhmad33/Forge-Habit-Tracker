package com.example.forge

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.forge.core.services.interfaces.INotificationService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ForgeApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var notificationService: INotificationService

    override fun onCreate() {
        super.onCreate()
        notificationService.createNotificationChannel()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
