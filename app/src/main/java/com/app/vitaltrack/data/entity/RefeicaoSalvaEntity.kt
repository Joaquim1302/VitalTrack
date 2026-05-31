package com.app.vitaltrack.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_DT_refeicoes_salvas")
data class RefeicaoSalvaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CD_REFEICAO_SALVA")
    val cdRefeicaoSalva: Long = 0,

    @ColumnInfo(name = "DS_REFEICAO_SALVA")
    val dsRefeicaoSalva: String,

    @ColumnInfo(name = "CD_REFEICAO_TP")
    val cdRefeicaoTp: Int?,

    @ColumnInfo(name = "NM_CAL_TOTAL")
    val nmCalTotal: Double,

    @ColumnInfo(name = "NM_PROT_TOTAL")
    val nmProtTotal: Double,

    @ColumnInfo(name = "NM_CARB_TOTAL")
    val nmCarbTotal: Double,

    @ColumnInfo(name = "NM_GORD_TOTAL")
    val nmGordTotal: Double,

    @ColumnInfo(name = "DT_CRIACAO")
    val dtCriacao: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "DT_ATUALIZACAO")
    val dtAtualizacao: Long = System.currentTimeMillis()
)
