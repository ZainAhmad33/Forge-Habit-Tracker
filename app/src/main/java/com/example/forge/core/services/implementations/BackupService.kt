package com.example.forge.core.services.implementations

import androidx.room.withTransaction
import com.example.forge.core.database.ForgeDatabase
import com.example.forge.core.database.dao.HabitActivityDao
import com.example.forge.core.database.dao.HabitDao
import com.example.forge.core.database.dao.UserDao
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.entity.User
import com.example.forge.core.services.interfaces.IBackupService
import com.example.forge.core.services.interfaces.IWidgetUpdater
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type
import java.time.LocalTime
import javax.inject.Inject

data class ForgeBackupPayload(
    val version: Int = 1,
    val exportedAt: Long,
    val user: User?,
    val habits: List<Habit>,
    val activities: List<HabitActivity>
)

class LocalTimeAdapter : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    override fun serialize(src: LocalTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.toString() ?: "")
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalTime {
        val str = json?.asString
        return if (str.isNullOrBlank()) LocalTime.MIDNIGHT else LocalTime.parse(str)
    }
}

class BackupService @Inject constructor(
    private val database: ForgeDatabase,
    private val userDao: UserDao,
    private val habitDao: HabitDao,
    private val habitActivityDao: HabitActivityDao,
    private val widgetUpdater: IWidgetUpdater
) : IBackupService {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    override suspend fun exportBackup(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val user = userDao.getUser().firstOrNull()
            val habits = habitDao.getAllHabitsSync()
            val activities = habitActivityDao.getAllActivities().firstOrNull() ?: emptyList()

            val rawPayload = ForgeBackupPayload(
                version = 1,
                exportedAt = System.currentTimeMillis(),
                user = user,
                habits = habits,
                activities = activities
            )

            val finalJson = gson.toJson(rawPayload)

            val writer = outputStream.bufferedWriter(Charsets.UTF_8)
            writer.write(finalJson)
            writer.flush()
        }
    }

    override suspend fun importBackup(inputStream: InputStream): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = inputStream.bufferedReader(Charsets.UTF_8).readText()

            val payload = gson.fromJson(jsonString, ForgeBackupPayload::class.java)
                ?: throw IllegalArgumentException("Failed to parse backup JSON.")

            val applyImport: suspend () -> Unit = {
                payload.user?.let { user ->
                    userDao.insertUser(user)
                }
                payload.habits.forEach { habit ->
                    habitDao.insertHabit(habit)
                }
                payload.activities.forEach { activity ->
                    habitActivityDao.insertActivity(activity)
                }
            }

            runCatching {
                database.withTransaction { applyImport() }
            }.getOrElse {
                applyImport()
            }

            widgetUpdater.updateAllWidgets()
        }
    }
}
