package com.app.vitaltrack.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_DT_refeicoes_tipos")
data class RefeicaoTipoEntity(
    @PrimaryKey
    @ColumnInfo(name = "CD_REFEICAO_TP")
    val cdRefeicaoTp: Long,
    
    @ColumnInfo(name = "DS_REFEICAO_TP")
    val dsRefeicaoTp: String?
)
