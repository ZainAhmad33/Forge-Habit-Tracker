package com.example.forge

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.forge.core.services.interfaces.IHabitMaintenanceService
import com.example.forge.core.services.interfaces.INotificationService
import com.example.forge.feature.widget.WidgetUpdateWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ForgeApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var notificationService: INotificationService

    @Inject
    lateinit var maintenanceService: IHabitMaintenanceService

    private val applicationScope = MainScope()

    override fun onCreate() {
        super.onCreate()
        notificationService.createNotificationChannel()

        // Schedule daily widget update work at midnight
        WidgetUpdateWorker.scheduleNextMidnightUpdate(this)

        // Register dynamic broadcast receiver for runtime date/time/timezone changes
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                applicationScope.launch {
                    try {
                        maintenanceService.performMaintenanceForAll()
                        // Reschedule just in case time settings were changed
                        WidgetUpdateWorker.scheduleNextMidnightUpdate(this@ForgeApplication)
                    } catch (ignored: Exception) {
                    }
                }
            }
        }, filter)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
