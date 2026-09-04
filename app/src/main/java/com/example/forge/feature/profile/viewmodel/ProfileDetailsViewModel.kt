package com.example.forge.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forge.core.database.entity.User
import com.example.forge.core.services.interfaces.IUserService
import com.example.forge.core.services.interfaces.ProfileStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Date
import javax.inject.Inject

data class ProfileDetailsUiState(
    val user: User? = null,
    val stats: ProfileStats? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileDetailsViewModel @Inject constructor(
    private val userService: IUserService
) : ViewModel() {

    val uiState: StateFlow<ProfileDetailsUiState> = combine(
        userService.getUser(),
        userService.getProfileStats()
    ) { user, stats ->
        ProfileDetailsUiState(
            user = user,
            stats = stats,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileDetailsUiState()
    )
}
