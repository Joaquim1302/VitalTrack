package com.app.vitaltrack.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "tb_DT_refeicoes_itens",
    primaryKeys = ["DT_CONSUMO", "CD_ALIMENTO", "CD_CLIENTE", "CD_REFEICAO_TP"]
)
data class RefeicaoItemEntity(
    @ColumnInfo(name = "DT_CONSUMO")
    val dtConsumo: String, // Usaremos String para simplificar ou Long (timestamp)
    
    @ColumnInfo(name = "CD_ALIMENTO")
    val cdAlimento: Long,
    
    @ColumnInfo(name = "CD_CLIENTE")
    val cdCliente: Long,
    
    @ColumnInfo(name = "CD_FASE")
    val cdFase: Int?,
    
    @ColumnInfo(name = "CD_REFEICAO_TP")
    val cdRefeicaoTp: Int,
    
    @ColumnInfo(name = "NM_QNT")
    val nmQnt: Double?,

    @ColumnInfo(name = "DS_UNIDADE")
    val dsUnidade: String? = "g"
)
