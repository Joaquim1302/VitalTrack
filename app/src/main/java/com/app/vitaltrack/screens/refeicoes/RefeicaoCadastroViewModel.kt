package com.app.vitaltrack.screens.refeicoes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.vitaltrack.data.dao.RefeicaoItemComDescricao
import com.app.vitaltrack.data.entity.RefeicaoItemEntity
import com.app.vitaltrack.database.AppDatabase
import com.app.vitaltrack.model.Meal
import com.app.vitaltrack.repository.MealRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class RefeicaoCadastroViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MealRepository
    private val _uiState = MutableStateFlow(RefeicaoCadastroUiState())
    val uiState: StateFlow<RefeicaoCadastroUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MealRepository(db.mealDao())
        
        repository.getMostUsedFoods().onEach { list ->
            _uiState.update { it.copy(maisConsumidos = list) }
        }.launchIn(viewModelScope)

        _searchQuery
            .debounce(300)
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.getAlimentosDisponiveis()
                } else {
                    repository.searchAlimentosDisponiveis(query)
                }
            }
            .onEach { list ->
                _uiState.update { it.copy(alimentosDisponiveis = list) }
            }
            .launchIn(viewModelScope)
    }

    fun init(date: String, typeId: Int) {
        val meal = Meal.defaultMeals.find { it.id.toInt() == typeId }
        val mealName = meal?.name ?: "Adicionar Alimentos"
        val mealEmoji = meal?.emoji ?: ""
        _uiState.update { it.copy(date = date, typeId = typeId, mealName = mealName, mealEmoji = mealEmoji) }
        
        repository.getItemsForMeal(date, typeId).onEach { items ->
            _uiState.update { it.copy(alimentosSelecionados = items) }
        }.launchIn(viewModelScope)

        repository.getFavorites(typeId).onEach { list ->
            _uiState.update { it.copy(refeicoesSalvas = list) }
        }.launchIn(viewModelScope)
    }

    fun selecionarAba(aba: AbaAdicionarAlimento) {
        _uiState.update {
            it.copy(abaSelecionada = aba)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addItem(alimentoId: Long, quantity: Double) {
        if (quantity <= 0) {
            _uiState.update { it.copy(errorMessage = "A quantidade deve ser maior que zero.") }
            return
        }

        viewModelScope.launch {
            repository.addFoodToMeal(
                _uiState.value.date,
                1, // Mock clienteId
                _uiState.value.typeId,
                alimentoId,
                quantity
            )
            _uiState.update { it.copy(successMessage = "Alimento adicionado à refeição.") }
        }
    }

    fun deleteItem(alimentoId: Long) {
        viewModelScope.launch {
            repository.deleteItem(_uiState.value.date, _uiState.value.typeId, alimentoId)
        }
    }

    fun solicitarRemocaoAlimento(item: RefeicaoItemComDescricao) {
        _uiState.update { it.copy(alimentoParaRemover = item) }
    }

    fun cancelarRemocaoAlimento() {
        _uiState.update { it.copy(alimentoParaRemover = null) }
    }

    fun confirmarRemocaoAlimento() {
        val item = _uiState.value.alimentoParaRemover ?: return
        viewModelScope.launch {
            repository.deleteItem(_uiState.value.date, _uiState.value.typeId, item.cdAlimento)
            _uiState.update { it.copy(alimentoParaRemover = null, successMessage = "Alimento removido.") }
        }
    }

    fun saveAsFavorite(name: String) {
        viewModelScope.launch {
            val itemsToSave = _uiState.value.alimentosSelecionados.map {
                RefeicaoItemEntity(
                    dtConsumo = it.dtConsumo,
                    cdAlimento = it.cdAlimento,
                    cdCliente = it.cdCliente,
                    cdFase = it.cdFase,
                    cdRefeicaoTp = it.cdRefeicaoTp,
                    nmQnt = it.nmQnt
                )
            }
            repository.saveMealAsFavorite(name, _uiState.value.typeId, itemsToSave)
            _uiState.update { it.copy(successMessage = "Refeição favorita salva.") }
        }
    }

    fun importFavorite(favoritaId: Long) {
        viewModelScope.launch {
            repository.importFavorite(favoritaId, _uiState.value.date, 1, _uiState.value.typeId)
            _uiState.update { it.copy(successMessage = "Refeição favorita importada.") }
        }
    }

    fun copyPreviousMeal() {
        viewModelScope.launch {
            val success = repository.copyPreviousMeal(1, _uiState.value.typeId, _uiState.value.date)
            if (success) {
                _uiState.update { it.copy(successMessage = "Refeição anterior copiada.") }
            } else {
                _uiState.update { it.copy(errorMessage = "Nenhuma refeição anterior encontrada.") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
