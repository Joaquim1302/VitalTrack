package com.app.vitaltrack.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "tb_DT_exercicios_tipos"
)
data class ExercicioTipoEntity(
    @PrimaryKey
    @ColumnInfo(name = "CD_TP_EXERCICIO")
    val cdTpExercicio: Int,

    @ColumnInfo(name = "DS_TP_EXERCICIO")
    val dsTpExercicio: String?,

    @ColumnInfo(name = "BL_TP_EXERCICIO")
    val blTpExercicio: Boolean?
)
