package com.example.forge.core.services.implementations

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.forge.core.services.interfaces.ITimeService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeService @Inject constructor(
    @ApplicationContext private val context: Context
) : ITimeService {

    override fun getCurrentDateFlow(): Flow<LocalDate> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(LocalDate.now())
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }

        context.registerReceiver(receiver, filter)

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.onStart {
        emit(LocalDate.now())
    }

    override fun getCurrentDate(): LocalDate = LocalDate.now()

    override fun toLocalDate(date: Date): LocalDate {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    override fun toStartOfDayDate(localDate: LocalDate): Date {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    override fun toEndOfDayDate(localDate: LocalDate): Date {
        return Date.from(localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant())
    }
}
