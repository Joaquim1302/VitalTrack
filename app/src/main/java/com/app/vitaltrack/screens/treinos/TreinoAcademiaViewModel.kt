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
import com.app.vitaltrack.repository.treinos.TreinoMarkdownRepository
import com.app.vitaltrack.repository.treinos.TreinoSessaoResult
import com.app.vitaltrack.data.markdown.TreinoMarkdownMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class TreinoAcademiaEvent {
    data class NavegarParaExecucao(val cdSessao: Long) : TreinoAcademiaEvent()
    data class MostrarErro(val mensagem: String) : TreinoAcademiaEvent()
}

data class TreinoAcademiaUiState(
    val clienteId: Long? = null,
    val fichaAtiva: TreinoFichaEntity? = null,
    val divisoes: List<TreinoFichaDiaEntity> = emptyList(),
    val divisaoSelecionada: TreinoFichaDiaEntity? = null,
    val exercicios: List<TreinoFichaExercicioEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isFichaVazia: Boolean = false,
    val isMarkdownMode: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class TreinoAcademiaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TreinoAcademiaRepository
    private val userPrefs: UserPreferencesRepository
    private val markdownRepo: TreinoMarkdownRepository

    private val _uiState = MutableStateFlow(TreinoAcademiaUiState())
    val uiState: StateFlow<TreinoAcademiaUiState> = _uiState.asStateFlow()

    private val _events = Channel<TreinoAcademiaEvent>()
    val events = _events.receiveAsFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TreinoAcademiaRepository(db.treinoAcademiaDao())
        userPrefs = UserPreferencesRepository(application)
        markdownRepo = TreinoMarkdownRepository.getInstance(application)

        observeClienteAtivo()
        observeMarkdownTreino()
    }

    private fun observeClienteAtivo() {
        userPrefs.userPreferencesFlow
            .map { it.clienteAtivoId }
            .distinctUntilChanged()
            .onEach { id ->
                _uiState.update { it.copy(clienteId = id) }
                if (id != null && !_uiState.value.isMarkdownMode) {
                    carregarFichaAtiva(id)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeMarkdownTreino() {
        markdownRepo.activeMarkdownTreino
            .onEach { active ->
                if (active != null) {
                    _uiState.update { it.copy(isMarkdownMode = true) }
                    carregarTreinosMarkdown()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun carregarTreinosMarkdown() {
        val imported = markdownRepo.importedTreinos.value
        if (imported.isNotEmpty()) {
            val divisoes = imported.mapIndexed { index, t -> 
                TreinoMarkdownMapper.toFichaDiaEntity(t, index)
            }
            _uiState.update { it.copy(
                divisoes = divisoes,
                isFichaVazia = false,
                isLoading = false
            ) }
            // Seleciona o primeiro se nenhum selecionado
            if (_uiState.value.divisaoSelecionada == null) {
                selecionarDivisao(divisoes.first())
            }
        }
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
            if (_uiState.value.isMarkdownMode) {
                val markdownTreino = markdownRepo.getTreinoByNome(divisao.dsDia)
                if (markdownTreino != null) {
                    val exercicios = markdownTreino.exercicios.mapIndexed { index, e ->
                        TreinoMarkdownMapper.toFichaExercicioEntity(e, divisao.cdFichaDia, index)
                    }
                    _uiState.update { it.copy(exercicios = exercicios, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                repository.listarExerciciosPlanejados(divisao.cdFichaDia)
                    .collect { exercicios ->
                        _uiState.update { it.copy(exercicios = exercicios, isLoading = false) }
                    }
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

    fun iniciarTreino(cdFichaDia: Long) {
        val clienteId = _uiState.value.clienteId ?: return
        
        if (_uiState.value.isMarkdownMode) {
            // No modo Markdown, usamos um ID especial para a execução
            viewModelScope.launch {
                _events.send(TreinoAcademiaEvent.NavegarParaExecucao(MARKDOWN_SESSION_ID))
            }
            return
        }

        viewModelScope.launch {
            val result = repository.iniciarSessaoTreino(clienteId, cdFichaDia)
            when (result) {
                is TreinoSessaoResult.SessaoCriada -> {
                    _events.send(TreinoAcademiaEvent.NavegarParaExecucao(result.sessao.cdTreinoSessao))
                }
                is TreinoSessaoResult.SessaoRetomada -> {
                    _events.send(TreinoAcademiaEvent.NavegarParaExecucao(result.sessao.cdTreinoSessao))
                }
                is TreinoSessaoResult.SessaoEmAndamentoDeOutroTreino -> {
                    _events.send(TreinoAcademiaEvent.MostrarErro("Já existe um treino de ${result.sessao.dsObs ?: "outro tipo"} em andamento."))
                    // TODO: Futuramente oferecer opção de cancelar a anterior e iniciar esta
                }
                is TreinoSessaoResult.Erro -> {
                    _events.send(TreinoAcademiaEvent.MostrarErro(result.mensagem))
                }
            }
        }
    }

    companion object {
        const val MARKDOWN_SESSION_ID = -1L
    }
}
