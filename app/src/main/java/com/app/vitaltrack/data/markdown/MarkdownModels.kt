package com.app.vitaltrack.data.markdown

data class MarkdownTreino(
    val nome: String,
    val grupoMuscular: String?,
    val exercicios: List<MarkdownTreinoExercicio>
)

data class MarkdownTreinoExercicio(
    val ordem: Int,
    val nome: String,
    val series: Int?,
    val repeticoes: String,
    val carga: String?,
    val intervaloSegundos: Int?
)

sealed class MarkdownTreinoParseResult {
    data class Success(val treinos: List<MarkdownTreino>) : MarkdownTreinoParseResult()
    data class Error(val message: String) : MarkdownTreinoParseResult()
}
