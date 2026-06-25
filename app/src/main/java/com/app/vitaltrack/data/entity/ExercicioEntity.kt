package com.app.vitaltrack.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "tb_DT_exercicios"
)
data class ExercicioEntity(
    @PrimaryKey
    @ColumnInfo(name = "CD_EXERCICIO")
    val cdExercicio: Long,

    @ColumnInfo(name = "NM_CAL")
    val nmCal: Double?,

    @ColumnInfo(name = "CD_PERIODO")
    val cdPeriodo: Int?,

    @ColumnInfo(name = "CD_FASE")
    val cdFase: Int?,

    @ColumnInfo(name = "CD_CLIENTE")
    val cdCliente: Long,

    @ColumnInfo(name = "CD_TP_EXERCICIO")
    val cdTpExercicio: Int?,

    @ColumnInfo(name = "DT_DIA")
    val dtDia: Date?
)
