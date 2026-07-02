package com.app.vitaltrack.screens.treinos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.vitaltrack.data.markdown.MarkdownTreinoParseResult
import com.app.vitaltrack.repository.treinos.TreinoMarkdownRepository
import com.app.vitaltrack.repository.treinos.TreinoAcademiaRepository
import com.app.vitaltrack.repository.UserPreferencesRepository
import com.app.vitaltrack.database.AppDatabase
import android.net.Uri
import android.content.Intent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TreinoMarkdownImportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TreinoMarkdownRepository.getInstance(application)
    private val academiaRepository: TreinoAcademiaRepository
    private val userPrefs: UserPreferencesRepository

    init {
        val db = AppDatabase.getDatabase(application)
        academiaRepository = TreinoAcademiaRepository(db.treinoAcademiaDao())
        userPrefs = UserPreferencesRepository(application)
    }

    private val _uiState = MutableStateFlow(TreinoMarkdownImportUiState())
    val uiState: StateFlow<TreinoMarkdownImportUiState> = _uiState.asStateFlow()

    fun carregarTreinos() {
        val imported = repository.importedResult.value
        if (imported != null) {
            _uiState.update { it.copy(isLoading = false, fichaImportada = imported, errorMessage = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.carregarTreinosDoMarkdown()) {
                is MarkdownTreinoParseResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, fichaImportada = result.resultado) }
                }
                is MarkdownTreinoParseResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun carregarDeUri(uri: Uri) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            
            // Tenta garantir a permissão persistente novamente no nível do ViewModel
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                // Silencioso se já tiver ou não suportar, o erro real virá na leitura
            }

            val result = repository.carregarTreinosDeUri(context, uri)
            when (result) {
                is MarkdownTreinoParseResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, fichaImportada = result.resultado) }
                }
                is MarkdownTreinoParseResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun selecionarTreino(index: Int) {
        _uiState.update { it.copy(treinoSelecionadoIndex = index) }
    }

    fun usarComoFichaDigital() {
        val ficha = _uiState.value.fichaImportada ?: return
        
        viewModelScope.launch {
            try {
                val userPreferences = userPrefs.userPreferencesFlow.first()
                val clienteId = userPreferences.clienteAtivoId ?: 1L // Fallback para ID 1 se não houver
                
                academiaRepository.importarFichaMarkdown(clienteId, ficha)
                
                _uiState.update { it.copy(snackbarMessage = "Ficha '${ficha.dsFicha}' importada com sucesso!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro ao salvar no banco: ${e.message}") }
            }
        }
    }

    fun resetSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
