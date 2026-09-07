package com.example.forge.core.services.interfaces

import java.util.UUID

interface IHabitMaintenanceService {
    suspend fun performMaintenance(habitId: UUID)
    suspend fun performMaintenanceForAll()
}
