package com.app.vitaltrack.screens.treinos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.vitaltrack.data.entity.treinos.TreinoSerieEntity
import com.app.vitaltrack.database.AppDatabase
import com.app.vitaltrack.repository.UserPreferencesRepository
import com.app.vitaltrack.repository.treinos.TreinoAcademiaRepository
import com.app.vitaltrack.repository.treinos.TreinoSessaoResult
import com.app.vitaltrack.data.entity.treinos.TreinoSessaoEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class TreinoExecucaoEvent {
    object DescansoConcluido : TreinoExecucaoEvent()
}

class TreinoExecucaoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TreinoAcademiaRepository
    private val userPrefs: UserPreferencesRepository

    private val _uiState = MutableStateFlow(TreinoExecucaoUiState())
    val uiState: StateFlow<TreinoExecucaoUiState> = _uiState.asStateFlow()

    private val _events = Channel<TreinoExecucaoEvent>()
    val events = _events.receiveAsFlow()

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var timerJob: Job? = null
    private var restTimerJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TreinoAcademiaRepository(db.treinoAcademiaDao())
        userPrefs = UserPreferencesRepository(application)
        
        userPrefs.userPreferencesFlow
            .map { it.clienteAtivoId }
            .distinctUntilChanged()
            .onEach { id -> _uiState.update { it.copy(cdCliente = id) } }
            .launchIn(viewModelScope)
    }

    fun init(cdSessao: Long, cdFichaDia: Long = 0) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            if (cdSessao != 0L) {
                loadSessaoExistente(cdSessao)
            } else if (cdFichaDia != 0L) {
                garantirSessao(cdFichaDia)
            }
        }
    }

    private suspend fun loadSessaoExistente(cdSessao: Long) {
        val sessao = repository.dao.buscarSessaoPorId(cdSessao)
        if (sessao != null) {
            val dia = repository.dao.buscarDia(sessao.cdFichaDia)
            val exerciciosPlanejados = repository.dao.listarExerciciosPlanejadosSync(sessao.cdFichaDia)
            val seriesReaisSessaoAtual = repository.buscarSeriesDaSessao(cdSessao)

            // Busca histórico da última sessão concluída
            val ultimaSessaoConcluida = repository.buscarUltimaSessaoConcluida(
                sessao.cdCliente, 
                sessao.cdFichaDia, 
                sessao.cdTreinoSessao
            )
            val seriesHistorico = ultimaSessaoConcluida?.let { 
                repository.buscarSeriesDaSessao(it.cdTreinoSessao) 
            } ?: emptyList()

            val exerciciosExecucao: List<TreinoExercicioExecucao> = exerciciosPlanejados.map { planejado ->
                val seriesParaExercicioAtual = seriesReaisSessaoAtual.filter { it.cdFichaExercicio == planejado.cdFichaExercicio }
                val seriesParaExercicioHistorico = seriesHistorico.filter { it.cdFichaExercicio == planejado.cdFichaExercicio }

                val seriesUi = (1..planejado.nrSeriesPlanejadas).map { index ->
                    val realAtual = seriesParaExercicioAtual.find { it.nrSerie == index }
                    val realHistorico = seriesParaExercicioHistorico.find { it.nrSerie == index }
                    
                    // Prioridade de sugestão/valor:
                    // 1. Valor já salvo na sessão atual (retomada)
                    // 2. Valor realizado na última sessão concluída
                    // 3. Valor planejado na ficha (fallback)
                    
                    val cargaValor = realAtual?.nmCarga 
                        ?: realHistorico?.nmCarga
                        ?: planejado.nmCargaRecomendada
                    
                    val cargaStr = cargaValor?.let { 
                        if ((it % 1f) == 0f) it.toInt().toString() else it.toString() 
                    } ?: ""

                    val repsValor = realAtual?.nrRepeticoes 
                        ?: realHistorico?.nrRepeticoes 
                        ?: planejado.nrRepeticoesPlanejadas

                    TreinoSerieUiModel(
                        cdSerie = realAtual?.cdSerie ?: 0L,
                        nrSerie = index,
                        carga = cargaStr,
                        repeticoes = repsValor?.toString() ?: "",
                        concluida = realAtual?.stConcluida ?: false,
                        sugeridoDoTreinoAnterior = realAtual == null && realHistorico != null
                    )
                }
                TreinoExercicioExecucao(planejado, seriesUi)
            }

            _uiState.update { it.copy(
                isLoading = false,
                sessao = sessao,
                dia = dia,
                exerciciosExecucao = exerciciosExecucao,
                horarioInicioFormatado = timeFormat.format(sessao.dtInicio),
                totalSeries = exerciciosExecucao.sumOf { it.series.size },
                seriesConcluidas = exerciciosExecucao.sumOf { it.series.count { s -> s.concluida } }
            ) }
            startTimer(sessao.dtInicio)
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Sessão não encontrada.") }
        }
    }

    private suspend fun garantirSessao(cdFichaDia: Long) {
        val clienteId = _uiState.value.cdCliente ?: return
        val result = repository.garantirSessaoEmAndamento(clienteId, cdFichaDia)
        when (result) {
            is TreinoSessaoResult.SessaoCriada -> loadSessaoExistente(result.sessao.cdTreinoSessao)
            is TreinoSessaoResult.SessaoRetomada -> loadSessaoExistente(result.sessao.cdTreinoSessao)
            else -> _uiState.update { it.copy(isLoading = false, errorMessage = "Erro ao iniciar sessão.") }
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

    fun updateCarga(cdFichaExercicio: Long, nrSerie: Int, novaCarga: String) {
        _uiState.update { state ->
            val novosExercicios = state.exerciciosExecucao.map { ex ->
                if (ex.exercicio.cdFichaExercicio == cdFichaExercicio) {
                    val novasSeries = ex.series.map { s ->
                        if (s.nrSerie == nrSerie) s.copy(carga = novaCarga) else s
                    }
                    ex.copy(series = novasSeries)
                } else ex
            }
            state.copy(exerciciosExecucao = novosExercicios)
        }
    }

    fun updateRepeticoes(cdFichaExercicio: Long, nrSerie: Int, novasReps: String) {
        _uiState.update { state ->
            val novosExercicios = state.exerciciosExecucao.map { ex ->
                if (ex.exercicio.cdFichaExercicio == cdFichaExercicio) {
                    val novasSeries = ex.series.map { s ->
                        if (s.nrSerie == nrSerie) s.copy(repeticoes = novasReps) else s
                    }
                    ex.copy(series = novasSeries)
                } else ex
            }
            state.copy(exerciciosExecucao = novosExercicios)
        }
    }

    fun ajustarCarga(cdFichaExercicio: Long, nrSerie: Int, delta: Float) {
        val ex = _uiState.value.exerciciosExecucao.find { it.exercicio.cdFichaExercicio == cdFichaExercicio } ?: return
        val serie = ex.series.find { it.nrSerie == nrSerie } ?: return
        val atual = serie.carga.replace(',', '.').toFloatOrNull() ?: 0f
        val novo = (atual + delta).coerceAtLeast(0f)
        val novoStr = if (novo % 1f == 0f) novo.toInt().toString() else String.format(Locale.getDefault(), "%.1f", novo).replace('.', ',')
        updateCarga(cdFichaExercicio, nrSerie, novoStr)
    }

    fun ajustarRepeticoes(cdFichaExercicio: Long, nrSerie: Int, delta: Int) {
        val ex = _uiState.value.exerciciosExecucao.find { it.exercicio.cdFichaExercicio == cdFichaExercicio } ?: return
        val serie = ex.series.find { it.nrSerie == nrSerie } ?: return
        val atual = serie.repeticoes.toIntOrNull() ?: 0
        val novo = (atual + delta).coerceAtLeast(0)
        updateRepeticoes(cdFichaExercicio, nrSerie, novo.toString())
    }

    fun concluirSerie(cdFichaExercicio: Long, nrSerie: Int) {
        val sessao = _uiState.value.sessao ?: return
        val exercicioExec = _uiState.value.exerciciosExecucao.find { it.exercicio.cdFichaExercicio == cdFichaExercicio } ?: return
        val serieUi = exercicioExec.series.find { it.nrSerie == nrSerie } ?: return

        viewModelScope.launch {
            setSaving(cdFichaExercicio, nrSerie, true)

            val serieEntity = TreinoSerieEntity(
                cdSerie = serieUi.cdSerie,
                cdTreinoSessao = sessao.cdTreinoSessao,
                cdFichaExercicio = cdFichaExercicio,
                nrSerie = nrSerie,
                nmCarga = serieUi.carga.replace(',', '.').toFloatOrNull(),
                nrRepeticoes = serieUi.repeticoes.toIntOrNull(),
                stConcluida = true
            )
            val novoId = repository.registrarSerieRealizada(serieEntity)
            
            _uiState.update { state ->
                val novosExercicios = state.exerciciosExecucao.map { ex ->
                    if (ex.exercicio.cdFichaExercicio == cdFichaExercicio) {
                        val novasSeries = ex.series.map { s ->
                            when {
                                s.nrSerie == nrSerie -> s.copy(cdSerie = novoId, concluida = true, isSaving = false)
                                s.nrSerie > nrSerie && !s.concluida -> s.copy(carga = serieUi.carga)
                                else -> s
                            }
                        }
                        ex.copy(series = novasSeries)
                    } else ex
                }
                state.copy(
                    exerciciosExecucao = novosExercicios,
                    seriesConcluidas = novosExercicios.sumOf { it.series.count { s -> s.concluida } }
                )
            }

            // Iniciar cronômetro de descanso (Fase 5)
            val restSeconds = exercicioExec.exercicio.nrDescansoSegundos ?: 60
            startRestTimer(
                seconds = restSeconds,
                exerciseName = exercicioExec.exercicio.dsObs ?: "Exercício",
                serieNumber = nrSerie
            )
        }
    }

    private fun startRestTimer(seconds: Int, exerciseName: String?, serieNumber: Int?) {
        restTimerJob?.cancel()
        _uiState.update { it.copy(
            isRestTimerVisible = true,
            isRestTimerRunning = true,
            restRemainingSeconds = seconds,
            restTotalSeconds = seconds,
            currentRestExerciseName = exerciseName,
            currentRestSerieNumber = serieNumber,
            restFinished = false
        ) }

        restTimerJob = viewModelScope.launch {
            while (_uiState.value.restRemainingSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(
                    restRemainingSeconds = (it.restRemainingSeconds - 1).coerceAtZero()
                ) }
            }
            finishRestTimer()
        }
    }

    private fun Int.coerceAtZero() = if (this < 0) 0 else this

    fun skipRestTimer() {
        restTimerJob?.cancel()
        _uiState.update { it.copy(
            isRestTimerVisible = false,
            isRestTimerRunning = false,
            restRemainingSeconds = 0
        ) }
    }

    fun addThirtySecondsToRestTimer() {
        _uiState.update { it.copy(
            restRemainingSeconds = it.restRemainingSeconds + 30
        ) }
    }

    private fun finishRestTimer() {
        _uiState.update { it.copy(
            isRestTimerRunning = false,
            restRemainingSeconds = 0,
            restFinished = true
        ) }
        viewModelScope.launch {
            _events.send(TreinoExecucaoEvent.DescansoConcluido)
        }
    }

    fun hideRestTimer() {
        _uiState.update { it.copy(isRestTimerVisible = false, restFinished = false) }
    }

    private fun setSaving(cdFichaExercicio: Long, nrSerie: Int, saving: Boolean) {
        _uiState.update { state ->
            val novos = state.exerciciosExecucao.map { ex ->
                if (ex.exercicio.cdFichaExercicio == cdFichaExercicio) {
                    ex.copy(series = ex.series.map { s -> if (s.nrSerie == nrSerie) s.copy(isSaving = saving) else s })
                } else ex
            }
            state.copy(exerciciosExecucao = novos)
        }
    }

    fun onConcluirClick() {
        _uiState.update { it.copy(showConcluirDialog = true) }
    }

    fun dismissConcluirDialog() {
        _uiState.update { it.copy(showConcluirDialog = false) }
    }

    fun onCancelarClick() {
        _uiState.update { it.copy(showCancelarDialog = true) }
    }

    fun dismissCancelarDialog() {
        _uiState.update { it.copy(showCancelarDialog = false) }
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
