package com.app.vitaltrack.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "tb_DT_clientes"
)
data class ClienteEntity(
    @PrimaryKey
    @ColumnInfo(name = "CD_CLIENTE")
    val cdCliente: Long,

    @ColumnInfo(name = "DS_NOME")
    val dsNome: String,

    @ColumnInfo(name = "CD_SEXO")
    val cdSexo: String? = null,

    @ColumnInfo(name = "DT_NASCIMENTO")
    val dtNascimento: Date? = null,

    @ColumnInfo(name = "NM_ALTURA")
    val nmAltura: Double? = null
)
