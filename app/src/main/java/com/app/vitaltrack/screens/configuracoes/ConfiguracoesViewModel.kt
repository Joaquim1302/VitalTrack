package com.app.vitaltrack.screens.configuracoes

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.vitaltrack.database.AppDatabase
import com.app.vitaltrack.repository.ImportRepository
import com.app.vitaltrack.utils.JsonImportManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    data class Success(val message: String) : ImportState()
    data class Error(val message: String) : ImportState()
}

class ConfiguracoesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ImportRepository
    private val importManager: JsonImportManager

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ImportRepository(db.importDao())
        importManager = JsonImportManager(repository)
    }

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun importJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { 
                    it.bufferedReader().readText()
                }
                
                if (jsonString != null) {
                    val resultMessage = importManager.importFromJson(jsonString)
                    _importState.value = ImportState.Success(resultMessage)
                } else {
                    _importState.value = ImportState.Error("Não foi possível ler o arquivo.")
                }
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Erro: ${e.message}")
            }
        }
    }

    fun resetState() {
        _importState.value = ImportState.Idle
    }
}
