package com.app.vitaltrack.screens.refeicoes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.vitaltrack.data.dao.AlimentoDisponivel
import com.app.vitaltrack.data.dao.MostUsedFood
import com.app.vitaltrack.data.dao.RecentFood
import com.app.vitaltrack.data.dao.RefeicaoItemComDescricao
import com.app.vitaltrack.data.entity.RefeicaoItemEntity
import com.app.vitaltrack.database.AppDatabase
import com.app.vitaltrack.model.Meal
import com.app.vitaltrack.repository.MealRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

        val thirtyDaysAgo = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)
        repository.getRecentFoods(thirtyDaysAgo).onEach { list ->
            _uiState.update { it.copy(consumidosRecentemente = list) }
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

    fun isAlimentoSelecionado(alimentoId: Long): Boolean {
        return _uiState.value.alimentosSelecionados.any { it.cdAlimento == alimentoId }
    }

    fun selecionarAlimentoParaAdicionar(alimento: AlimentoDisponivel) {
        _uiState.update { it.copy(alimentoDisponivelSelecionado = alimento) }
    }

    fun selecionarAlimentoRecentParaAdicionar(alimento: RecentFood) {
        _uiState.update { it.copy(alimentoRecentSelecionado = alimento) }
    }

    fun selecionarAlimentoMaisConsumidoParaAdicionar(food: MostUsedFood) {
        _uiState.update { it.copy(alimentoMaisConsumidoSelecionado = food) }
    }

    fun cancelarAdicaoAlimento() {
        _uiState.update { 
            it.copy(
                alimentoDisponivelSelecionado = null, 
                alimentoRecentSelecionado = null,
                alimentoMaisConsumidoSelecionado = null
            ) 
        }
    }

    fun selecionarAlimentoParaEditar(item: RefeicaoItemComDescricao) {
        _uiState.update { it.copy(alimentoParaEditar = item) }
    }

    fun cancelarEdicaoAlimento() {
        _uiState.update { it.copy(alimentoParaEditar = null) }
    }

    fun addItem(alimentoId: Long, quantity: Double, unit: String) {
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
                quantity,
                unit
            )
            _uiState.update { 
                it.copy(
                    alimentoDisponivelSelecionado = null, 
                    alimentoRecentSelecionado = null,
                    alimentoMaisConsumidoSelecionado = null,
                    successMessage = "Alimento adicionado à refeição."
                ) 
            }
        }
    }

    fun updateItem(alimentoId: Long, quantity: Double, unit: String) {
        if (quantity <= 0) {
            _uiState.update { it.copy(errorMessage = "A quantidade deve ser maior que zero.") }
            return
        }

        viewModelScope.launch {
            repository.updateFoodInMeal(
                _uiState.value.date,
                1, // Mock clienteId
                _uiState.value.typeId,
                alimentoId,
                quantity,
                unit
            )
            _uiState.update { it.copy(alimentoParaEditar = null, successMessage = "Quantidade atualizada.") }
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
                    nmQnt = it.nmQnt,
                    dsUnidade = it.dsUnidade
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
