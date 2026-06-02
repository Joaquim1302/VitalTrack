package com.app.vitaltrack.screens.perfil

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.vitaltrack.database.AppDatabase
import com.app.vitaltrack.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PerfilClienteViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val clienteDao = db.clienteDao()
    private val userPreferencesRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(PerfilClienteUiState())
    val uiState: StateFlow<PerfilClienteUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
        loadClientes()
    }

    private fun observePreferences() {
        userPreferencesRepository.userPreferencesFlow
            .onEach { prefs ->
                _uiState.update { it.copy(
                    clienteAtivoId = prefs.clienteAtivoId,
                    metaCalorias = prefs.metaCalorias
                ) }
                
                // Se não houver cliente ativo e já tivermos carregado a lista
                if (prefs.clienteAtivoId == null && _uiState.value.clientes.isNotEmpty()) {
                    val first = _uiState.value.clientes[0]
                    userPreferencesRepository.updateClienteAtivo(first.cdCliente, first.dsNome)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadClientes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lista = clienteDao.listar()
                _uiState.update { it.copy(clientes = lista) }
                
                // Se o cliente ativo ainda for null, tenta setar o primeiro
                val prefs = userPreferencesRepository.userPreferencesFlow.first()
                if (prefs.clienteAtivoId == null && lista.isNotEmpty()) {
                    userPreferencesRepository.updateClienteAtivo(lista[0].cdCliente, lista[0].dsNome)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = "Erro ao carregar clientes: ${e.message}") }
            }
        }
    }

    fun selecionarCliente(id: Long, nome: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateClienteAtivo(id, nome)
        }
    }

    fun atualizarMeta(meta: Double) {
        viewModelScope.launch {
            userPreferencesRepository.updateMetaCalorica(meta)
        }
    }
}
