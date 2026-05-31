package com.app.vitaltrack.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tb_DT_refeicoes_salvas_itens",
    foreignKeys = [
        ForeignKey(
            entity = RefeicaoSalvaEntity::class,
            parentColumns = ["CD_REFEICAO_SALVA"],
            childColumns = ["CD_REFEICAO_SALVA"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RefeicaoSalvaItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CD_ITEM_REFEICAO_SALVA")
    val cdItemRefeicaoSalva: Long = 0,

    @ColumnInfo(name = "CD_REFEICAO_SALVA")
    val cdRefeicaoSalva: Long,

    @ColumnInfo(name = "CD_ALIMENTO")
    val cdAlimento: Long,

    @ColumnInfo(name = "DS_ALIMENTO")
    val dsAlimento: String,

    @ColumnInfo(name = "NM_QTD")
    val nmQtd: Double,

    @ColumnInfo(name = "DS_UNIDADE")
    val dsUnidade: String,

    @ColumnInfo(name = "NM_CAL")
    val nmCal: Double,

    @ColumnInfo(name = "NM_PROT")
    val nmProt: Double,

    @ColumnInfo(name = "NM_CARB")
    val nmCarb: Double,

    @ColumnInfo(name = "NM_GORD")
    val nmGord: Double
)
