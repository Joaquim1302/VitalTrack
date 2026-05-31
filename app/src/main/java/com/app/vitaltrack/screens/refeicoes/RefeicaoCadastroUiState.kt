package com.app.vitaltrack.screens.refeicoes

import com.app.vitaltrack.data.dao.AlimentoDisponivel
import com.app.vitaltrack.data.dao.MostUsedFood
import com.app.vitaltrack.data.dao.RecentFood
import com.app.vitaltrack.data.dao.RefeicaoItemComDescricao
import com.app.vitaltrack.data.entity.AlimentoEntity
import com.app.vitaltrack.data.entity.RefeicaoFavoritaEntity

data class RefeicaoCadastroUiState(
    val abaSelecionada: AbaAdicionarAlimento = AbaAdicionarAlimento.SELECIONADOS,
    val date: String = "",
    val typeId: Int = 0,
    val mealName: String = "",
    val mealEmoji: String = "",
    val alimentosSelecionados: List<RefeicaoItemComDescricao> = emptyList(),
    val alimentosDisponiveis: List<AlimentoDisponivel> = emptyList(),
    val consumidosRecentemente: List<RecentFood> = emptyList(),
    val maisConsumidos: List<MostUsedFood> = emptyList(),
    val refeicoesSalvas: List<RefeicaoFavoritaEntity> = emptyList(),
    val searchResults: List<AlimentoEntity> = emptyList(),
    val isLoading: Boolean = false,
    val alimentoParaRemover: RefeicaoItemComDescricao? = null,
    val alimentoParaEditar: RefeicaoItemComDescricao? = null,
    val alimentoDisponivelSelecionado: AlimentoDisponivel? = null,
    val alimentoRecentSelecionado: RecentFood? = null,
    val alimentoMaisConsumidoSelecionado: MostUsedFood? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
) {
    // Aliases para manter compatibilidade com a versão atual da tela sem grandes refatorações imediatas
    val currentItems get() = alimentosSelecionados
    val availableFoods get() = alimentosDisponiveis
    val mostUsed get() = maisConsumidos
    val favorites get() = refeicoesSalvas
}
