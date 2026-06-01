package com.app.vitaltrack.data.export

import com.app.vitaltrack.data.entity.*
import kotlinx.serialization.Serializable

@Serializable
data class ExportRefeicaoItemDto(
    val DT_CONSUMO: String,
    val CD_REFEICAO_TP: Int,
    val CD_ALIMENTO: Long,
    val CD_CLIENTE: Long,
    val CD_REFEICAO_ITEM_APP: Int?,
    val NM_QNT: Double?,
    val NM_CAL: Double?,
    val NM_PROT: Double?,
    val NM_CARB: Double?,
    val NM_GORD: Double?
)

@Serializable
data class ExportRefeicoesDto(
    val tb_DT_refeicoes_itens: List<ExportRefeicaoItemDto> = emptyList(),
    val tb_DT_clientes: List<ExportClienteDto> = emptyList(),
    val tb_DT_alimentos: List<ExportAlimentoDto> = emptyList(),
    val tb_DT_refeicoes_tipos: List<ExportRefeicaoTipoDto> = emptyList(),
    val tb_DT_unidades: List<ExportUnidadeDto> = emptyList()
)

@Serializable
data class ExportClienteDto(
    val CD_CLIENTE: Long,
    val DS_NOME: String,
    val CD_SEXO: String? = null,
    val DT_NASCIMENTO: Long? = null,
    val NM_ALTURA: Double? = null
)

@Serializable
data class ExportAlimentoDto(
    val CD_ALIMENTO: Long,
    val DS_ALIMENTO: String?,
    val CD_UNIDADE: Int?,
    val NM_QNT_BASE: Long?,
    val NM_CAL: Double?,
    val NM_PROT: Double?,
    val NM_CARB: Double?,
    val NM_GORD: Double?
)

@Serializable
data class ExportRefeicaoTipoDto(
    val CD_REFEICAO_TP: Long,
    val DS_REFEICAO_TP: String?
)

@Serializable
data class ExportUnidadeDto(
    val CD_UNIDADE: Long,
    val DS_UNIDADE: String?
)
