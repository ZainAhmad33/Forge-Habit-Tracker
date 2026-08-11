package com.example.forge.core.services.interfaces

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.Date

interface ITimeService {
    fun getCurrentDateFlow(): Flow<LocalDate>
    fun getCurrentDate(): LocalDate
    fun toLocalDate(date: Date): LocalDate
    fun toStartOfDayDate(localDate: LocalDate): Date
    fun toEndOfDayDate(localDate: LocalDate): Date
}
