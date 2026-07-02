package com.app.vitaltrack.screens.treinos

import com.app.vitaltrack.data.markdown.MarkdownTreino

data class TreinoMarkdownImportUiState(
    val isLoading: Boolean = false,
    val treinos: List<MarkdownTreino> = emptyList(),
    val treinoSelecionadoIndex: Int = 0,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
) {
    val treinoSelecionado: MarkdownTreino? get() = treinos.getOrNull(treinoSelecionadoIndex)
}
