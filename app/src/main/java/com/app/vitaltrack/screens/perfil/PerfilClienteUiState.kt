package com.app.vitaltrack.screens.perfil

import com.app.vitaltrack.data.entity.ClienteEntity

data class PerfilClienteUiState(
    val clientes: List<ClienteEntity> = emptyList(),
    val clienteAtivoId: Long? = null,
    val metaCalorias: Double = 2000.0,
    val isLoading: Boolean = false,
    val erro: String? = null
)
