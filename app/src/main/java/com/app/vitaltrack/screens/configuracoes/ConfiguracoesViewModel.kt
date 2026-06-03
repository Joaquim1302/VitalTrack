package com.app.vitaltrack.screens.configuracoes

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.vitaltrack.database.AppDatabase
import com.app.vitaltrack.repository.ImportRepository
import com.app.vitaltrack.utils.JsonImportManager
import com.app.vitaltrack.data.export.JsonExportService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    data class Success(val message: String) : ImportState()
    data class Error(val message: String) : ImportState()
}

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Success(val message: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

class ConfiguracoesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ImportRepository
    private val importManager: JsonImportManager
    private val exportService: JsonExportService

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ImportRepository(db.importDao(), db)
        importManager = JsonImportManager(repository)
        exportService = JsonExportService(application, db.mealDao())
    }

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

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
        _exportState.value = ExportState.Idle
    }

    fun exportJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            try {
                val jsonString = exportService.generateExportJson()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(jsonString)
                    }
                }
                _exportState.value = ExportState.Success("Exportação concluída com sucesso.\nArquivo: vt_export_to_access.json")
            } catch (e: Exception) {
                Log.e("ConfiguracoesViewModel", "Falha ao exportar os dados", e)
                _exportState.value = ExportState.Error("Falha ao exportar os dados.")
            }
        }
    }
}
