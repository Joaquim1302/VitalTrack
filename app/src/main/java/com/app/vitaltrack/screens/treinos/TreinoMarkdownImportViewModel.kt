package com.app.vitaltrack.screens.treinos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.vitaltrack.data.markdown.MarkdownAssetReader
import com.app.vitaltrack.data.markdown.MarkdownTreinoParseResult
import com.app.vitaltrack.repository.treinos.TreinoMarkdownRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TreinoMarkdownImportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TreinoMarkdownRepository.getInstance(application)

    private val _uiState = MutableStateFlow(TreinoMarkdownImportUiState())
    val uiState: StateFlow<TreinoMarkdownImportUiState> = _uiState.asStateFlow()

    init {
        carregarTreinos()
    }

    fun carregarTreinos() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.carregarTreinosDoMarkdown()) {
                is MarkdownTreinoParseResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, treinos = result.treinos) }
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
        val selected = _uiState.value.treinoSelecionado ?: return
        repository.setActiveTreino(selected)
        _uiState.update { it.copy(snackbarMessage = "Plano '${selected.nome}' definido como ficha ativa.") }
    }

    fun resetSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
