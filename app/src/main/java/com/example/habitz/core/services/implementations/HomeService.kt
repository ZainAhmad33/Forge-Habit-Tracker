package com.example.habitz.core.services.implementations

import com.example.habitz.core.database.entity.CategoryToImage
import com.example.habitz.core.database.entity.HomeDashboard
import com.example.habitz.core.database.interfaces.ICategoryRepository
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.database.interfaces.IUserRepository
import com.example.habitz.core.services.interfaces.IHomeService
import com.example.habitz.core.uiEntities.CategoryPill
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class HomeService @Inject constructor(
    private val habitRepository: IHabitRepository,
    private val categoryRepository: ICategoryRepository,
    private val userRepository: IUserRepository
) : IHomeService {
    override fun getDashboardData(): HomeDashboard {
        var user = userRepository.getUserDetails()
        var habits = habitRepository.getHabits()
        var habitSummary = habitRepository.getHabitSummary()
        var categories = categoryRepository.getCategories().distinct().map{ CategoryPill(it,
            CategoryToImage[it]!!
        ) }

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)

        return HomeDashboard(
            getDynamicGreeting(),
            user.firstName,
            currentDate.format(formatter),
            habitSummary,
            categories,
            habits
        )
    }

    private fun getDynamicGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night" // 22:00 (10 PM) to 3:59 AM
        }
    }
}