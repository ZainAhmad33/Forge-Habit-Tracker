package com.example.forge.core.services.interfaces

import java.io.InputStream
import java.io.OutputStream

interface IBackupService {
    suspend fun exportBackup(outputStream: OutputStream): Result<Unit>
    suspend fun importBackup(inputStream: InputStream): Result<Unit>
}
