package com.app.vitaltrack.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_DT_alimentos")
data class AlimentoEntity(
    @PrimaryKey
    @ColumnInfo(name = "CD_ALIMENTO")
    val cdAlimento: Long,
    
    @ColumnInfo(name = "DS_ALIMENTO")
    val dsAlimento: String?,
    
    @ColumnInfo(name = "CD_UNIDADE")
    val cdUnidade: Int?,
    
    @ColumnInfo(name = "NM_QNT_BASE")
    val nmQntBase: Long?,
    
    @ColumnInfo(name = "NM_CAL")
    val nmCal: Double?,
    
    @ColumnInfo(name = "NM_PROT")
    val nmProt: Double?,
    
    @ColumnInfo(name = "NM_CARB")
    val nmCarb: Double?,
    
    @ColumnInfo(name = "NM_GORD")
    val nmGord: Double?
)
