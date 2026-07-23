package com.example.habitz.core.services.interfaces

import com.example.habitz.core.database.entity.HomeDashboard

interface IHomeService {
    fun getDashboardData(): HomeDashboard
}