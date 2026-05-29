package com.app.vitaltrack.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "tb_DT_refeicoes_favoritas_itens",
    primaryKeys = ["CD_FAVORITA", "CD_ALIMENTO"]
)
data class RefeicaoFavoritaItemEntity(
    @ColumnInfo(name = "CD_FAVORITA")
    val cdFavorita: Long,
    
    @ColumnInfo(name = "CD_ALIMENTO")
    val cdAlimento: Long,
    
    @ColumnInfo(name = "NM_QNT")
    val nmQnt: Double
)
