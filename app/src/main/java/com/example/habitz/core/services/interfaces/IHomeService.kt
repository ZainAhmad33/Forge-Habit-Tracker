package com.example.habitz.core.services.interfaces

import com.example.habitz.core.uiEntities.HomeHabit
import com.example.habitz.feature.home.viewmodel.HomeDashboardUIState
import kotlinx.coroutines.flow.Flow

interface IHomeService {
    fun getDashboardData(): Flow<HomeDashboardUIState>
    fun searchHabits(query: String): List<HomeHabit>
}
