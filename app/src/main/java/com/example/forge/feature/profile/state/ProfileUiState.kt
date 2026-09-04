package com.example.forge.feature.profile.state

import java.util.Date
import java.util.UUID

data class ProfileUiState(
    val id: UUID = UUID.randomUUID(),
    val firstName: String = "",
    val lastName: String = "",
    val dob: Date? = null,
    val firstNameError: Boolean = false,
    val lastNameError: Boolean = false,
    val dobError: Boolean = false,
    val isSaving: Boolean = false,
    val isUserExisting: Boolean = false
)
