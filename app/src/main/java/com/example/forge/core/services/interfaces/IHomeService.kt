package com.example.forge.core.services.interfaces

import com.example.forge.core.uiEntities.HomeHabit
import com.example.forge.feature.home.viewmodel.HomeDashboardUIState
import kotlinx.coroutines.flow.Flow

interface IHomeService {
    fun getDashboardData(): Flow<HomeDashboardUIState>
    fun searchHabits(query: String): List<HomeHabit>
}
