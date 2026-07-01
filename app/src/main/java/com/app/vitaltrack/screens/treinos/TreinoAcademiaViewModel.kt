package com.app.vitaltrack.screens.treinos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.vitaltrack.data.entity.treinos.TreinoFichaDiaEntity
import com.app.vitaltrack.data.entity.treinos.TreinoFichaEntity
import com.app.vitaltrack.data.entity.treinos.TreinoFichaExercicioEntity
import com.app.vitaltrack.database.AppDatabase
import com.app.vitaltrack.repository.UserPreferencesRepository
import com.app.vitaltrack.repository.treinos.TreinoAcademiaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TreinoAcademiaUiState(
    val clienteId: Long? = null,
    val fichaAtiva: TreinoFichaEntity? = null,
    val divisoes: List<TreinoFichaDiaEntity> = emptyList(),
    val divisaoSelecionada: TreinoFichaDiaEntity? = null,
    val exercicios: List<TreinoFichaExercicioEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isFichaVazia: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class TreinoAcademiaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TreinoAcademiaRepository
    private val userPrefs: UserPreferencesRepository

    private val _uiState = MutableStateFlow(TreinoAcademiaUiState())
    val uiState: StateFlow<TreinoAcademiaUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TreinoAcademiaRepository(db.treinoAcademiaDao())
        userPrefs = UserPreferencesRepository(application)

        observeClienteAtivo()
    }

    private fun observeClienteAtivo() {
        userPrefs.userPreferencesFlow
            .map { it.clienteAtivoId }
            .distinctUntilChanged()
            .onEach { id ->
                _uiState.update { it.copy(clienteId = id) }
                if (id != null) {
                    carregarFichaAtiva(id)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun carregarFichaAtiva(clienteId: Long) {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            repository.buscarFichaAtiva(clienteId)
                .collect { ficha ->
                    if (ficha != null) {
                        _uiState.update { it.copy(fichaAtiva = ficha, isFichaVazia = false) }
                        carregarDivisoes(ficha.cdFicha)
                    } else {
                        _uiState.update { it.copy(fichaAtiva = null, isFichaVazia = true, isLoading = false) }
                    }
                }
        }
    }

    private fun carregarDivisoes(fichaId: Long) {
        viewModelScope.launch {
            repository.listarDias(fichaId)
                .collect { lista ->
                    _uiState.update { it.copy(divisoes = lista) }
                    if (lista.isNotEmpty() && _uiState.value.divisaoSelecionada == null) {
                        selecionarDivisao(lista.first())
                    } else if (lista.isEmpty()) {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    fun selecionarDivisao(divisao: TreinoFichaDiaEntity) {
        _uiState.update { it.copy(divisaoSelecionada = divisao, isLoading = true) }
        
        viewModelScope.launch {
            repository.listarExerciciosPlanejados(divisao.cdFichaDia)
                .collect { exercicios ->
                    _uiState.update { it.copy(exercicios = exercicios, isLoading = false) }
                }
        }
    }

    fun criarDadosExemplo() {
        val clienteId = _uiState.value.clienteId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.criarDadosExemploSeNecessario(clienteId)
            // carregarFichaAtiva será disparado pelo Flow se houver mudanças, 
            // mas como buscarFichaAtiva retorna um Flow de Ficha?, 
            // o collect em carregarFichaAtiva cuidará disso.
        }
    }
}
