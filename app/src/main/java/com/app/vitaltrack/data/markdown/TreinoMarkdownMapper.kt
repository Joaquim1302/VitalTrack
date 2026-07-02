package com.app.vitaltrack.data.markdown

import com.app.vitaltrack.data.entity.treinos.TreinoFichaDiaEntity
import com.app.vitaltrack.data.entity.treinos.TreinoFichaExercicioEntity
import java.util.Date

object TreinoMarkdownMapper {

    /**
     * Converte um treino do Markdown para a entidade de Dia da Ficha.
     * Usamos IDs negativos temporários para indicar que são dados em memória/Markdown.
     */
    fun toFichaDiaEntity(markdownTreino: MarkdownTreino, index: Int): TreinoFichaDiaEntity {
        return TreinoFichaDiaEntity(
            cdFichaDia = -(index + 1).toLong(), // ID negativo para Markdown
            cdFicha = -1L,
            dsDia = markdownTreino.nome,
            nrOrdem = index + 1,
            dsGrupoMuscular = markdownTreino.grupoMuscular
        )
    }

    /**
     * Converte um exercício do Markdown para a entidade de Exercício da Ficha.
     */
    fun toFichaExercicioEntity(
        markdownExercicio: MarkdownTreinoExercicio, 
        diaId: Long, 
        globalIndex: Int
    ): TreinoFichaExercicioEntity {
        // Tentamos converter repetições para Int, se falhar usamos 0 e guardamos no dsObs
        val repsInt = markdownExercicio.repeticoes.toIntOrNull() ?: 0
        
        return TreinoFichaExercicioEntity(
            cdFichaExercicio = -(globalIndex + 1).toLong(), // ID negativo
            cdFichaDia = diaId,
            cdExercicio = 0L, // TODO: Mapear com IDs reais se necessário
            nrOrdem = markdownExercicio.ordem,
            nrSeriesPlanejadas = markdownExercicio.series ?: 1,
            nrRepeticoesPlanejadas = repsInt,
            nmCargaRecomendada = markdownExercicio.carga?.replace(",", ".")?.toFloatOrNull(),
            nrDescansoSegundos = markdownExercicio.intervaloSegundos ?: 60,
            dsObs = if (repsInt == 0) markdownExercicio.repeticoes else markdownExercicio.nome
        )
    }
}
