package com.example.habitz.core.database

import androidx.room.TypeConverter
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.database.entity.HabitFrequency
import com.example.habitz.core.database.entity.HabitType
import com.example.habitz.core.uiEntities.ProgressShape
import java.time.LocalTime
import java.util.Date
import java.util.UUID

class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(uuid: String?): UUID? = uuid?.let { UUID.fromString(it) }

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? = millisSinceEpoch?.let { Date(it) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toLocalTime(timeStr: String?): LocalTime? = timeStr?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromLocalTimeList(times: List<LocalTime>?): String? = 
        times?.joinToString(",") { it.toString() }

    @TypeConverter
    fun toLocalTimeList(timesStr: String?): List<LocalTime>? =
        if (timesStr.isNullOrEmpty()) emptyList() 
        else timesStr.split(",").map { LocalTime.parse(it) }

    @TypeConverter
    fun fromIntList(ints: List<Int>?): String? = ints?.joinToString(",")

    @TypeConverter
    fun toIntList(intsStr: String?): List<Int>? =
        if (intsStr.isNullOrEmpty()) emptyList()
        else intsStr.split(",").map { it.toInt() }

    @TypeConverter
    fun fromHabitCategory(value: HabitCategory): String = value.name

    @TypeConverter
    fun toHabitCategory(value: String): HabitCategory = HabitCategory.valueOf(value)

    @TypeConverter
    fun fromHabitType(value: HabitType): String = value.name

    @TypeConverter
    fun toHabitType(value: String): HabitType = HabitType.valueOf(value)

    @TypeConverter
    fun fromHabitFrequency(value: HabitFrequency): String = value.name

    @TypeConverter
    fun toHabitFrequency(value: String): HabitFrequency = HabitFrequency.valueOf(value)

    @TypeConverter
    fun fromProgressShape(value: ProgressShape): String = value.name

    @TypeConverter
    fun toProgressShape(value: String): ProgressShape = ProgressShape.valueOf(value)
}
