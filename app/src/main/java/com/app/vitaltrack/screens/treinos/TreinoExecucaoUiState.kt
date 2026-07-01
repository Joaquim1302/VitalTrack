package com.app.vitaltrack.screens.treinos

import com.app.vitaltrack.data.entity.treinos.TreinoFichaDiaEntity
import com.app.vitaltrack.data.entity.treinos.TreinoFichaExercicioEntity
import com.app.vitaltrack.data.entity.treinos.TreinoSessaoEntity

data class TreinoSerieUiModel(
    val cdSerie: Long = 0,
    val nrSerie: Int,
    val carga: String = "",
    val repeticoes: String = "",
    val concluida: Boolean = false,
    val isSaving: Boolean = false
)

data class TreinoExercicioExecucao(
    val exercicio: TreinoFichaExercicioEntity,
    val series: List<TreinoSerieUiModel> = emptyList()
)

data class TreinoExecucaoUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cdCliente: Long? = null,
    val sessao: TreinoSessaoEntity? = null,
    val dia: TreinoFichaDiaEntity? = null,
    val exerciciosExecucao: List<TreinoExercicioExecucao> = emptyList(),
    val horarioInicioFormatado: String = "",
    val duracaoFormatada: String = "00:00",
    val treinoConcluido: Boolean = false,
    val showConcluirDialog: Boolean = false,
    val showCancelarDialog: Boolean = false,
    val totalSeries: Int = 0,
    val seriesConcluidas: Int = 0
) {
    val progresso: Float get() = if (totalSeries > 0) seriesConcluidas.toFloat() / totalSeries else 0f
}
