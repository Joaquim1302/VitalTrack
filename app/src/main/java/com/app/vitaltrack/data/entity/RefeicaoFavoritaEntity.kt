package com.app.vitaltrack.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_DT_refeicoes_favoritas")
data class RefeicaoFavoritaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CD_FAVORITA")
    val cdFavorita: Long = 0,
    
    @ColumnInfo(name = "DS_FAVORITA")
    val dsFavorita: String,
    
    @ColumnInfo(name = "CD_REFEICAO_TP")
    val cdRefeicaoTp: Int?,
    
    @ColumnInfo(name = "DT_CRIACAO")
    val dtCriacao: Long = System.currentTimeMillis()
)
