package com.app.vitaltrack.screens.treinos

import com.app.vitaltrack.data.entity.treinos.TreinoFichaDiaEntity
import com.app.vitaltrack.data.entity.treinos.TreinoFichaExercicioEntity
import com.app.vitaltrack.data.entity.treinos.TreinoSessaoEntity

data class TreinoExecucaoUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cdCliente: Long? = null,
    val sessao: TreinoSessaoEntity? = null,
    val dia: TreinoFichaDiaEntity? = null,
    val exercicios: List<TreinoFichaExercicioEntity> = emptyList(),
    val horarioInicioFormatado: String = "",
    val duracaoFormatada: String = "00:00",
    val treinoConcluido: Boolean = false,
    val showConcluirDialog: Boolean = false,
    val showCancelarDialog: Boolean = false
)
