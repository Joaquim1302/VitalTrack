package com.app.vitaltrack.data.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportRefeicaoItemDto(
    val DT_CONSUMO: String,
    val CD_REFEICAO_TP: Int,
    val CD_ALIMENTO: Long,
    val CD_CLIENTE: Long,
    val NM_QNT: Double?,
    val NM_CAL: Double?,
    val NM_PROT: Double?,
    val NM_CARB: Double?,
    val NM_GORD: Double?
)

@Serializable
data class ExportRefeicoesDto(
    val tb_DT_refeicoes_itens: List<ExportRefeicaoItemDto>
)
