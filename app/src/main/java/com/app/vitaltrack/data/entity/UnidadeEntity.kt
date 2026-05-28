package com.app.vitaltrack.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_DT_unidades")
data class UnidadeEntity(
    @PrimaryKey
    @ColumnInfo(name = "CD_UNIDADE")
    val cdUnidade: Long,
    
    @ColumnInfo(name = "DS_UNIDADE")
    val dsUnidade: String?
)
