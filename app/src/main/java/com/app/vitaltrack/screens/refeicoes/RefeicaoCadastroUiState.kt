package com.app.vitaltrack.screens.refeicoes

import com.app.vitaltrack.data.dao.AlimentoDisponivel
import com.app.vitaltrack.data.dao.MostUsedFood
import com.app.vitaltrack.data.dao.RecentFood
import com.app.vitaltrack.data.dao.RefeicaoItemComDescricao
import com.app.vitaltrack.data.entity.AlimentoEntity
import com.app.vitaltrack.data.entity.RefeicaoFavoritaEntity

data class RefeicaoSalvaUi(
    val id: Long,
    val nome: String,
    val calorias: Double,
    val proteinas: Double,
    val carboidratos: Double,
    val gorduras: Double,
    val quantidadeItens: Int = 0
)

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
    val refeicoesSalvas: List<RefeicaoSalvaUi> = emptyList(),
    val refeicoesFavoritas: List<RefeicaoFavoritaEntity> = emptyList(), // Mantendo favoritas anteriores
    val searchResults: List<AlimentoEntity> = emptyList(),
    val isLoading: Boolean = false,
    val alimentoParaRemover: RefeicaoItemComDescricao? = null,
    val alimentoParaEditar: RefeicaoItemComDescricao? = null,
    val alimentoDisponivelSelecionado: AlimentoDisponivel? = null,
    val alimentoRecentSelecionado: RecentFood? = null,
    val alimentoMaisConsumidoSelecionado: MostUsedFood? = null,
    val refeicaoSalvaSelecionadaId: Long? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
) {
    // Aliases para manter compatibilidade com a versão atual da tela
    val currentItems get() = alimentosSelecionados
    val availableFoods get() = alimentosDisponiveis
    val mostUsed get() = maisConsumidos
    val favorites get() = refeicoesFavoritas
}
