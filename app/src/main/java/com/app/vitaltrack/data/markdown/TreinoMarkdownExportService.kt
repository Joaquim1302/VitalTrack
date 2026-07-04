package com.app.vitaltrack.data.markdown

import com.app.vitaltrack.repository.treinos.TreinoAcademiaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TreinoMarkdownExportService(
    private val repository: TreinoAcademiaRepository
) {

    /**
     * Gera o conteúdo Markdown da ficha ativa para um cliente.
     */
    suspend fun gerarMarkdownFichaAtiva(cdCliente: Long): String? = withContext(Dispatchers.IO) {
        val ficha = repository.buscarFichaAtivaSync(cdCliente) ?: return@withContext null
        val dias = repository.listarDiasSync(ficha.cdFicha)
        
        val sb = StringBuilder()
        
        // H1: Nome da Ficha
        sb.append("# ${ficha.dsFicha}\n\n")
        
        for (dia in dias) {
            // H2: Treino e Grupo Muscular
            val header = if (!dia.dsGrupoMuscular.isNullOrBlank()) {
                "## ${dia.dsDia} — ${dia.dsGrupoMuscular}"
            } else {
                "## ${dia.dsDia}"
            }
            sb.append("$header\n\n")
            
            // Tabela de Exercícios
            sb.append("| **Exercício** | **Séries** | **Repetições** | **Carga** | **Intervalo** |\n")
            sb.append("| :--- | :--- | :--- | :--- | :--- |\n")
            
            val exercicios = repository.dao.listarExerciciosPlanejadosSync(dia.cdFichaDia)
            for (ex in exercicios) {
                val nome = ex.dsObs ?: "Exercício"
                val series = ex.nrSeriesPlanejadas
                val reps = ex.nrRepeticoesPlanejadas
                val carga = ex.nmCargaRecomendada?.let { 
                    if (it % 1f == 0f) it.toInt().toString() else it.toString() 
                } ?: "—"
                val descanso = ex.nrDescansoSegundos ?: 60
                
                sb.append("| $nome | $series | $reps | $carga | $descanso |\n")
            }
            sb.append("\n")
        }
        
        return@withContext sb.toString()
    }
}
