package com.example.forge.feature.profile.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forge.core.database.entity.User
import com.example.forge.core.services.interfaces.IBackupService
import com.example.forge.core.services.interfaces.IUserService
import com.example.forge.core.services.interfaces.ProfileStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

data class ProfileDetailsUiState(
    val user: User? = null,
    val stats: ProfileStats? = null,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class ProfileDetailsViewModel @Inject constructor(
    private val userService: IUserService,
    private val backupService: IBackupService
) : ViewModel() {

    private val _exportImportState = MutableStateFlow(
        Triple(false, false, null as String?)
    )

    val uiState: StateFlow<ProfileDetailsUiState> = combine(
        userService.getUser(),
        userService.getProfileStats(),
        _exportImportState
    ) { user, stats, exportImport ->
        ProfileDetailsUiState(
            user = user,
            stats = stats,
            isLoading = false,
            isExporting = exportImport.first,
            isImporting = exportImport.second,
            statusMessage = exportImport.third
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileDetailsUiState()
    )

    fun exportBackup(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _exportImportState.update { Triple(true, false, null) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        backupService.exportBackup(outputStream).getOrThrow()
                    } ?: throw IOException("Could not open file for writing")
                }
            }
            _exportImportState.update {
                Triple(
                    false,
                    false,
                    if (result.isSuccess) "Backup exported successfully!" else "Failed to export backup: ${result.exceptionOrNull()?.localizedMessage}"
                )
            }
        }
    }

    fun importBackup(uri: Uri, contentResolver: ContentResolver, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _exportImportState.update { Triple(false, true, null) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        backupService.importBackup(inputStream).getOrThrow()
                    } ?: throw IOException("Could not open file for reading")
                }
            }
            if (result.isSuccess) {
                _exportImportState.update {
                    Triple(
                        false,
                        false,
                        "Backup imported successfully!"
                    )
                }
                onSuccess?.invoke()
            } else {
                _exportImportState.update {
                    Triple(
                        false,
                        false,
                        "Failed to import backup: ${result.exceptionOrNull()?.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearStatusMessage() {
        _exportImportState.update { Triple(it.first, it.second, null) }
    }
}
