package com.app.vitaltrack.data.markdown

data class TreinoMarkdownImportado(
    val dsFicha: String,
    val dias: List<TreinoDiaMarkdownImportado>
)

data class TreinoDiaMarkdownImportado(
    val dsDia: String,
    val dsGrupoMuscular: String?,
    val exercicios: List<TreinoExercicioMarkdownImportado>
)

data class TreinoExercicioMarkdownImportado(
    val nome: String,
    val series: Int?,
    val repeticoes: String?,
    val carga: String?,
    val intervaloSegundos: Int?,
    val ordem: Int
)

sealed class MarkdownTreinoParseResult {
    data class Success(val resultado: TreinoMarkdownImportado) : MarkdownTreinoParseResult() {
        val treinos: List<TreinoDiaMarkdownImportado> get() = resultado.dias
    }
    data class Error(val message: String) : MarkdownTreinoParseResult()
}
