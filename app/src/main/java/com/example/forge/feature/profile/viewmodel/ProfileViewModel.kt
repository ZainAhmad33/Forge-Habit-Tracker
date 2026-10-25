package com.example.forge.feature.profile.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forge.core.database.entity.User
import com.example.forge.core.services.interfaces.IBackupService
import com.example.forge.core.services.interfaces.IUserService
import com.example.forge.feature.profile.state.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userService: IUserService,
    private val backupService: IBackupService
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

    fun exportBackup(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, statusMessage = null) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        backupService.exportBackup(outputStream).getOrThrow()
                    } ?: throw IOException("Could not open file for writing")
                }
            }
            _uiState.update {
                it.copy(
                    isExporting = false,
                    statusMessage = if (result.isSuccess) "Backup exported successfully!" else "Failed to export backup: ${result.exceptionOrNull()?.localizedMessage}"
                )
            }
        }
    }

    fun importBackup(uri: Uri, contentResolver: ContentResolver, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, statusMessage = null) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        backupService.importBackup(inputStream).getOrThrow()
                    } ?: throw IOException("Could not open file for reading")
                }
            }
            if (result.isSuccess) {
                loadUser()
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        statusMessage = "Backup imported successfully!"
                    )
                }
                onSuccess?.invoke()
            } else {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        statusMessage = "Failed to import backup: ${result.exceptionOrNull()?.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
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
