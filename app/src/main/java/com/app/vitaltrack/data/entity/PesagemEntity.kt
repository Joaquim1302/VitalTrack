package com.app.vitaltrack.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.Date

@Entity(
    tableName = "tb_DT_pesagens",
    primaryKeys = ["CD_CLIENTE", "CD_FASE", "DT_PESAGEM", "HR_PESAGEM"]
)
data class PesagemEntity(
    @ColumnInfo(name = "CD_CLIENTE")
    val cdCliente: Long,

    @ColumnInfo(name = "CD_FASE")
    val cdFase: Int,

    @ColumnInfo(name = "DT_PESAGEM")
    val dtPesagem: Date,

    @ColumnInfo(name = "NM_PESO")
    val nmPeso: Double?,

    @ColumnInfo(name = "NM_PERCENT_GORD")
    val nmPercentGord: Double?,

    @ColumnInfo(name = "HR_PESAGEM")
    val hrPesagem: Date
)
