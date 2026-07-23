package com.example.habitz.core.services.implementations

import com.example.habitz.core.database.entity.HomeDashboard
import com.example.habitz.core.database.interfaces.ICategoryRepository
import com.example.habitz.core.database.interfaces.IHabitRepository
import com.example.habitz.core.database.interfaces.IUserRepository
import com.example.habitz.core.services.interfaces.IHomeService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
        var categories = categoryRepository.getCategories()
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)

        return HomeDashboard(
            user.firstName,
            currentDate.format(formatter),
            habitSummary,
            categories,
            habits
        )
    }
}