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
import com.app.vitaltrack.data.markdown.MarkdownTreinoParseResult
import com.app.vitaltrack.data.markdown.TreinoMarkdownExportService
import com.app.vitaltrack.data.gamification.GamificationRepository
import com.app.vitaltrack.data.gamification.GamificationEvent
import android.net.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class TreinoAcademiaEvent {
    data class NavegarParaExecucao(val cdSessao: Long) : TreinoAcademiaEvent()
    object NavegarParaImportacaoMarkdown : TreinoAcademiaEvent()
    data class MostrarErro(val mensagem: String) : TreinoAcademiaEvent()
    data class IniciarExportacaoMarkdown(val defaultFileName: String) : TreinoAcademiaEvent()
    data class MostrarMensagemSucesso(val mensagem: String) : TreinoAcademiaEvent()
    data class MostrarDialogoConflito(val sessao: com.app.vitaltrack.data.entity.treinos.TreinoSessaoEntity) : TreinoAcademiaEvent()
    data class NotificarGamificacao(val result: com.app.vitaltrack.data.gamification.GamificationResult) : TreinoAcademiaEvent()
}

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
    private val markdownRepository: TreinoMarkdownRepository
    private val markdownExportService: TreinoMarkdownExportService
    private val gamificationRepository: GamificationRepository
    private val userPrefs: UserPreferencesRepository

    private val _uiState = MutableStateFlow(TreinoAcademiaUiState())
    val uiState: StateFlow<TreinoAcademiaUiState> = _uiState.asStateFlow()

    private val _events = Channel<TreinoAcademiaEvent>()
    val events = _events.receiveAsFlow()

    private var markdownTemporario: String? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TreinoAcademiaRepository(db.treinoAcademiaDao())
        markdownRepository = TreinoMarkdownRepository.getInstance(application)
        markdownExportService = TreinoMarkdownExportService(repository)
        gamificationRepository = GamificationRepository(application)
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
                        _uiState.update { it.copy(fichaAtiva = null, isFichaVazia = true, isLoading = false, divisoes = emptyList(), exercicios = emptyList(), divisaoSelecionada = null) }
                    }
                }
        }
    }

    private fun carregarDivisoes(fichaId: Long) {
        viewModelScope.launch {
            repository.listarDias(fichaId)
                .collect { lista ->
                    _uiState.update { it.copy(divisoes = lista) }
                    // Se a divisão selecionada não está na lista ou é nula, seleciona a primeira
                    val currentSelection = _uiState.value.divisaoSelecionada
                    if (lista.isNotEmpty()) {
                        if (currentSelection == null || !lista.any { it.cdFichaDia == currentSelection.cdFichaDia }) {
                            selecionarDivisao(lista.first())
                        } else {
                            // Atualiza a seleção atual para garantir que os dados estejam frescos
                            val updatedSelection = lista.find { it.cdFichaDia == currentSelection.cdFichaDia }
                            if (updatedSelection != null) selecionarDivisao(updatedSelection)
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, divisaoSelecionada = null, exercicios = emptyList()) }
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
        }
    }

    fun iniciarTreino(cdFichaDia: Long, ignorarConflito: Boolean = false) {
        val clientId = _uiState.value.clienteId ?: return

        viewModelScope.launch {
            val result = if (ignorarConflito) {
                repository.forcarNovaSessao(clientId, cdFichaDia)
            } else {
                repository.iniciarSessaoTreino(clientId, cdFichaDia)
            }

            when (result) {
                is TreinoSessaoResult.SessaoCriada -> {
                    val gamificationResult = gamificationRepository.registerEvent(
                        GamificationEvent.WorkoutStarted(
                            clientId = clientId,
                            cdTreinoSessao = result.sessao.cdTreinoSessao,
                            date = LocalDate.now().toString()
                        )
                    )
                    _events.send(TreinoAcademiaEvent.NotificarGamificacao(gamificationResult))
                    _events.send(TreinoAcademiaEvent.NavegarParaExecucao(result.sessao.cdTreinoSessao))
                }
                is TreinoSessaoResult.SessaoRetomada -> {
                    _events.send(TreinoAcademiaEvent.NavegarParaExecucao(result.sessao.cdTreinoSessao))
                }
                is TreinoSessaoResult.SessaoEmAndamentoDeOutroTreino -> {
                    // Mantido por compatibilidade, mas o fluxo normal cairá em SessaoConflito
                    _events.send(TreinoAcademiaEvent.MostrarErro("Já existe um treino em andamento."))
                }
                is TreinoSessaoResult.SessaoConflito -> {
                    _events.send(TreinoAcademiaEvent.MostrarDialogoConflito(result.sessao))
                }
                is TreinoSessaoResult.Erro -> {
                    _events.send(TreinoAcademiaEvent.MostrarErro(result.mensagem))
                }
            }
        }
    }

    fun processarMarkdown(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = markdownRepository.carregarTreinosDeUri(getApplication(), uri)
            _uiState.update { it.copy(isLoading = false) }
            
            if (result is MarkdownTreinoParseResult.Success) {
                _events.send(TreinoAcademiaEvent.NavegarParaImportacaoMarkdown)
            } else if (result is MarkdownTreinoParseResult.Error) {
                _events.send(TreinoAcademiaEvent.MostrarErro(result.message))
            }
        }
    }

    fun prepararExportacaoMarkdown() {
        val clienteId = _uiState.value.clienteId ?: return
        val ficha = _uiState.value.fichaAtiva ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val markdown = markdownExportService.gerarMarkdownFichaAtiva(clienteId)
            _uiState.update { it.copy(isLoading = false) }
            
            if (markdown != null) {
                markdownTemporario = markdown
                val fileName = "treino_${ficha.dsFicha.lowercase().replace(" ", "_")}.md"
                _events.send(TreinoAcademiaEvent.IniciarExportacaoMarkdown(fileName))
            } else {
                _events.send(TreinoAcademiaEvent.MostrarErro("Não foi possível gerar o arquivo de exportação."))
            }
        }
    }

    fun salvarMarkdownNoUri(uri: Uri) {
        val content = markdownTemporario ?: return
        viewModelScope.launch {
            try {
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(content.toByteArray())
                }
                _events.send(TreinoAcademiaEvent.MostrarMensagemSucesso("Ficha exportada com sucesso!"))
            } catch (e: Exception) {
                _events.send(TreinoAcademiaEvent.MostrarErro("Erro ao salvar arquivo: ${e.message}"))
            } finally {
                markdownTemporario = null
            }
        }
    }
}
