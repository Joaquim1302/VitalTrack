package com.app.vitaltrack.data.markdown

import com.app.vitaltrack.data.entity.treinos.TreinoFichaDiaEntity
import com.app.vitaltrack.data.entity.treinos.TreinoFichaExercicioEntity

object TreinoMarkdownMapper {

    fun toFichaDiaEntity(markdownTreino: TreinoDiaMarkdownImportado, index: Int, fichaId: Long): TreinoFichaDiaEntity {
        return TreinoFichaDiaEntity(
            cdFicha = fichaId,
            dsDia = markdownTreino.dsDia,
            nrOrdem = index + 1,
            dsGrupoMuscular = markdownTreino.dsGrupoMuscular
        )
    }

    fun toFichaExercicioEntity(
        markdownExercicio: TreinoExercicioMarkdownImportado, 
        diaId: Long
    ): TreinoFichaExercicioEntity {
        val repsInt = markdownExercicio.repeticoes?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 10
        
        return TreinoFichaExercicioEntity(
            cdFichaDia = diaId,
            cdExercicio = 0L, 
            nrOrdem = markdownExercicio.ordem,
            nrSeriesPlanejadas = markdownExercicio.series ?: 3,
            nrRepeticoesPlanejadas = repsInt,
            nmCargaRecomendada = markdownExercicio.carga?.replace(",", ".")?.replace(Regex("[^0-9.]"), "")?.toFloatOrNull(),
            nrDescansoSegundos = markdownExercicio.intervaloSegundos ?: 60,
            dsObs = markdownExercicio.nome
        )
    }
}
