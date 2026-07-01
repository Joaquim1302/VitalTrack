package com.app.vitaltrack.screens.treinos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.vitaltrack.database.AppDatabase
import com.app.vitaltrack.repository.UserPreferencesRepository
import com.app.vitaltrack.repository.treinos.TreinoAcademiaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TreinoExecucaoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TreinoAcademiaRepository
    private val userPrefs: UserPreferencesRepository

    private val _uiState = MutableStateFlow(TreinoExecucaoUiState())
    val uiState: StateFlow<TreinoExecucaoUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var timerJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TreinoAcademiaRepository(db.treinoAcademiaDao())
        userPrefs = UserPreferencesRepository(application)
    }

    fun init(cdSessao: Long) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val sessao = repository.dao.buscarSessaoPorId(cdSessao)
            if (sessao != null) {
                val dia = repository.dao.buscarDia(sessao.cdFichaDia)
                val exerciciosFlow = repository.listarExerciciosPlanejados(sessao.cdFichaDia)
                
                exerciciosFlow.collect { exercicios ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        sessao = sessao,
                        dia = dia,
                        exercicios = exercicios,
                        horarioInicioFormatado = timeFormat.format(sessao.dtInicio)
                    ) }
                    startTimer(sessao.dtInicio)
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sessão não encontrada.") }
            }
        }
    }

    private fun startTimer(inicio: Date) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val agora = Date()
                val diff = agora.time - inicio.time
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff / (1000 * 60)) % 60
                val seconds = (diff / 1000) % 60
                
                val format = if (hours > 0) {
                    String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                }
                
                _uiState.update { it.copy(duracaoFormatada = format) }
                delay(1000)
            }
        }
    }

    fun onConcluirClick() {
        _uiState.update { it.copy(showConcluirDialog = true) }
    }

    fun dismissConcluirDialog() {
        _uiState.update { it.copy(showConcluirDialog = false) }
    }

    fun confirmarConclusao() {
        val cdSessao = _uiState.value.sessao?.cdTreinoSessao ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConcluirDialog = false) }
            val result = repository.concluirSessaoTreino(cdSessao)
            if (result != null) {
                timerJob?.cancel()
                _uiState.update { it.copy(isLoading = false, treinoConcluido = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Erro ao concluir treino.") }
            }
        }
    }

    fun onCancelarClick() {
        _uiState.update { it.copy(showCancelarDialog = true) }
    }

    fun dismissCancelarDialog() {
        _uiState.update { it.copy(showCancelarDialog = false) }
    }

    fun confirmarCancelamento() {
        val cdSessao = _uiState.value.sessao?.cdTreinoSessao ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showCancelarDialog = false) }
            val result = repository.cancelarSessaoTreino(cdSessao, "Cancelado pelo usuário")
            if (result != null) {
                timerJob?.cancel()
                _uiState.update { it.copy(isLoading = false, treinoConcluido = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Erro ao cancelar treino.") }
            }
        }
    }
}
