package com.app.vitaltrack.screens.treinos

import com.app.vitaltrack.data.markdown.TreinoMarkdownImportado
import com.app.vitaltrack.data.markdown.TreinoDiaMarkdownImportado

data class TreinoMarkdownImportUiState(
    val isLoading: Boolean = false,
    val fichaImportada: TreinoMarkdownImportado? = null,
    val treinoSelecionadoIndex: Int = 0,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
) {
    val treinos: List<TreinoDiaMarkdownImportado> get() = fichaImportada?.dias ?: emptyList()
    val treinoSelecionado: TreinoDiaMarkdownImportado? get() = treinos.getOrNull(treinoSelecionadoIndex)
}
