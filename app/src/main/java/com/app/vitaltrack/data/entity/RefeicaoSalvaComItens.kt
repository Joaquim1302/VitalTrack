package com.app.vitaltrack.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RefeicaoSalvaComItens(
    @Embedded val refeicao: RefeicaoSalvaEntity,
    @Relation(
        parentColumn = "CD_REFEICAO_SALVA",
        entityColumn = "CD_REFEICAO_SALVA"
    )
    val itens: List<RefeicaoSalvaItemEntity>
)
