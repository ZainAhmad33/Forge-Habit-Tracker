package com.example.habitz.core.services.interfaces

import com.example.habitz.core.database.entity.HomeDashboard
import com.example.habitz.core.database.entity.HomeHabit

interface IHomeService {
    fun getDashboardData(): HomeDashboard
    fun searchHabits(query: String): List<HomeHabit>
}