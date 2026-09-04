package com.example.forge.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forge.core.database.entity.User
import com.example.forge.core.services.interfaces.IUserService
import com.example.forge.feature.profile.state.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userService: IUserService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            userService.getUser().firstOrNull()?.let { user ->
                _uiState.update {
                    it.copy(
                        id = user.id,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        dob = user.dob,
                        isUserExisting = true
                    )
                }
            }
        }
    }

    fun onFirstNameChange(name: String) {
        _uiState.update { it.copy(firstName = name, firstNameError = false) }
    }

    fun onLastNameChange(name: String) {
        _uiState.update { it.copy(lastName = name, lastNameError = false) }
    }

    fun onDobChange(date: Date?) {
        _uiState.update { it.copy(dob = date, dobError = false) }
    }

    fun onSaveClick(onSuccess: () -> Unit) {
        if (validate()) {
            viewModelScope.launch {
                val state = _uiState.value
                val user = User(
                    id = state.id,
                    firstName = state.firstName,
                    lastName = state.lastName,
                    dob = state.dob!!
                )
                userService.saveUser(user)
                onSuccess()
            }
        }
    }

    private fun validate(): Boolean {
        val firstNameError = _uiState.value.firstName.isBlank()
        val dobError = _uiState.value.dob == null

        _uiState.update {
            it.copy(
                firstNameError = firstNameError,
                dobError = dobError
            )
        }

        return !firstNameError && !dobError
    }
}
