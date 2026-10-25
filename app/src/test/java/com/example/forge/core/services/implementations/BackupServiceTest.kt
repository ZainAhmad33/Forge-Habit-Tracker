package com.example.forge.core.services.implementations

import com.example.forge.core.database.ForgeDatabase
import com.example.forge.core.database.dao.HabitActivityDao
import com.example.forge.core.database.dao.HabitDao
import com.example.forge.core.database.dao.UserDao
import com.example.forge.core.database.entity.Habit
import com.example.forge.core.database.entity.HabitActivity
import com.example.forge.core.database.entity.HabitCategory
import com.example.forge.core.database.entity.HabitFrequency
import com.example.forge.core.database.entity.HabitType
import com.example.forge.core.database.entity.User
import com.example.forge.core.services.interfaces.IWidgetUpdater
import com.example.forge.core.uiEntities.ProgressShape
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Date
import java.util.UUID

class BackupServiceTest {

    private lateinit var backupService: BackupService
    private val insertedHabits = mutableListOf<Habit>()
    private val insertedActivities = mutableListOf<HabitActivity>()
    private var insertedUser: User? = null

    private val sampleUser = User(
        id = UUID.randomUUID(),
        firstName = "John",
        lastName = "Doe",
        dob = Date(946684800000L) // Jan 1, 2000
    )

    private val sampleHabit = Habit(
        id = UUID.randomUUID(),
        title = "Morning Run",
        category = HabitCategory.Health,
        emoji = "🏃",
        habitType = HabitType.YesNo,
        reminders = emptyList(),
        frequencyType = HabitFrequency.EveryDay,
        trackedDays = emptyList(),
        numberOfTrackedDays = 7,
        completionTargetPerDay = 1,
        targetUnit = "Per Day",
        progressShape = ProgressShape.Circle,
        skipDaysAllowed = 2,
        lastMilestoneRewarded = 0,
        isLocked = false,
        isLockingEnabled = true,
        lockedAt = null,
        lastMaintenanceDate = null,
        createdAt = Date(),
        updatedAt = Date()
    )

    private val sampleActivity = HabitActivity(
        id = UUID.randomUUID(),
        habitId = sampleHabit.id,
        quantity = 1,
        isSkip = false,
        createdAt = Date()
    )

    private val fakeUserDao = object : UserDao {
        override fun getUser(): Flow<User?> = flowOf(insertedUser)
        override suspend fun insertUser(user: User) {
            insertedUser = user
        }
    }

    private val fakeHabitDao = object : HabitDao {
        override fun getHabits(): Flow<List<Habit>> = flowOf(insertedHabits)
        override fun getHabitFlow(habitId: UUID): Flow<Habit?> = flowOf(insertedHabits.find { it.id == habitId })
        override suspend fun getHabitById(habitId: UUID): Habit? = insertedHabits.find { it.id == habitId }
        override suspend fun getAllHabitsSync(): List<Habit> = insertedHabits
        override suspend fun insertHabit(habit: Habit) {
            insertedHabits.removeAll { it.id == habit.id }
            insertedHabits.add(habit)
        }
        override suspend fun deleteHabit(habit: Habit) {
            insertedHabits.removeAll { it.id == habit.id }
        }
    }

    private val fakeActivityDao = object : HabitActivityDao {
        override fun getActivitiesForHabit(habitId: UUID): Flow<List<HabitActivity>> = flowOf(insertedActivities.filter { it.habitId == habitId })
        override fun getDailyQuantitiesForHabit(habitId: UUID) = flowOf(emptyList<com.example.forge.core.database.pojo.DailyHabitQuantity>())
        override suspend fun getDailyQuantitiesForHabitSync(habitId: UUID) = emptyList<com.example.forge.core.database.pojo.DailyHabitQuantity>()
        override fun getAllDailyQuantities() = flowOf(emptyList<com.example.forge.core.database.pojo.DailyHabitQuantity>())
        override suspend fun getDailyQuantitiesByRange(habitId: UUID, from: Long, to: Long) = emptyList<com.example.forge.core.database.pojo.DailyHabitQuantity>()
        override fun getActivitiesForHabits(habitIds: List<UUID>, from: Date, to: Date) = flowOf(insertedActivities.filter { it.habitId in habitIds })
        override fun getAllActivities(): Flow<List<HabitActivity>> = flowOf(insertedActivities)
        override suspend fun getActivitiesByRange(habitId: UUID, from: Date, to: Date): List<HabitActivity> = insertedActivities.filter { it.habitId == habitId }
        override suspend fun insertActivity(activity: HabitActivity) {
            insertedActivities.removeAll { it.id == activity.id }
            insertedActivities.add(activity)
        }
        override suspend fun deleteActivityById(activityId: UUID) {
            insertedActivities.removeAll { it.id == activityId }
        }
        override suspend fun deleteActivitiesForHabit(habitId: UUID) {
            insertedActivities.removeAll { it.habitId == habitId }
        }
    }

    private val fakeWidgetUpdater = object : IWidgetUpdater {
        override suspend fun updateAllWidgets() {}
    }

    private val fakeDatabase = object : ForgeDatabase() {
        override fun habitDao(): HabitDao = fakeHabitDao
        override fun habitActivityDao(): HabitActivityDao = fakeActivityDao
        override fun userDao(): UserDao = fakeUserDao
        override fun clearAllTables() {}
        override fun createInvalidationTracker(): androidx.room.InvalidationTracker {
            return androidx.room.InvalidationTracker(this, "habits", "habit_activities", "users")
        }
        @Suppress("DEPRECATION")
        override fun createOpenHelper(config: androidx.room.DatabaseConfiguration): androidx.sqlite.db.SupportSQLiteOpenHelper {
            throw UnsupportedOperationException()
        }
    }

    @Before
    fun setup() {
        insertedHabits.clear()
        insertedActivities.clear()
        insertedUser = sampleUser
        insertedHabits.add(sampleHabit)
        insertedActivities.add(sampleActivity)

        backupService = BackupService(
            database = fakeDatabase,
            userDao = fakeUserDao,
            habitDao = fakeHabitDao,
            habitActivityDao = fakeActivityDao,
            widgetUpdater = fakeWidgetUpdater
        )
    }

    @Test
    fun `exportBackup creates valid JSON output with signature`() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        val result = backupService.exportBackup(outputStream)

        assertTrue(result.isSuccess)
        val jsonOutput = outputStream.toString(Charsets.UTF_8.name())
        assertTrue(jsonOutput.contains("\"signature\":"))
        assertTrue(jsonOutput.contains("Morning Run"))
        assertTrue(jsonOutput.contains("John"))
    }

    @Test
    fun `importBackup succeeds with valid exported backup`() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        backupService.exportBackup(outputStream)

        insertedHabits.clear()
        insertedActivities.clear()
        insertedUser = null

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val result = backupService.importBackup(inputStream)

        assertTrue(result.isSuccess)
        assertEquals("John", insertedUser?.firstName)
        assertEquals(1, insertedHabits.size)
        assertEquals("Morning Run", insertedHabits[0].title)
        assertEquals(1, insertedActivities.size)
    }

    @Test
    fun `importBackup fails when backup payload is tampered with`() = runBlocking {
        val outputStream = ByteArrayOutputStream()
        backupService.exportBackup(outputStream)

        val originalJson = outputStream.toString(Charsets.UTF_8.name())
        val tamperedJson = originalJson.replace("Morning Run", "Tampered Habit")

        val inputStream = ByteArrayInputStream(tamperedJson.toByteArray(Charsets.UTF_8))
        val result = backupService.importBackup(inputStream)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }

    @Test
    fun `importBackup fails when signature is missing`() = runBlocking {
        val gson = GsonBuilder()
            .registerTypeAdapter(java.time.LocalTime::class.java, LocalTimeAdapter())
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
        val payloadWithoutSignature = ForgeBackupPayload(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            user = sampleUser,
            habits = listOf(sampleHabit),
            activities = listOf(sampleActivity),
            signature = null
        )
        val json = gson.toJson(payloadWithoutSignature)

        val inputStream = ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        val result = backupService.importBackup(inputStream)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
