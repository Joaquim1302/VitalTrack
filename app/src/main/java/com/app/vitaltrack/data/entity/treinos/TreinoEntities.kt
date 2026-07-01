package com.app.vitaltrack.data.entity.treinos

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tb_DT_treinos_fichas")
data class TreinoFichaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CD_FICHA")
    val cdFicha: Long = 0,
    @ColumnInfo(name = "CD_CLIENTE")
    val cdCliente: Long,
    @ColumnInfo(name = "DS_FICHA")
    val dsFicha: String,
    @ColumnInfo(name = "DT_INICIO")
    val dtInicio: Date,
    @ColumnInfo(name = "DT_FIM")
    val dtFim: Date? = null,
    @ColumnInfo(name = "ST_ATIVA")
    val stAtiva: Boolean,
    @ColumnInfo(name = "DS_OBS")
    val dsObs: String? = null
)

@Entity(tableName = "tb_DT_treinos_fichas_dias")
data class TreinoFichaDiaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CD_FICHA_DIA")
    val cdFichaDia: Long = 0,
    @ColumnInfo(name = "CD_FICHA")
    val cdFicha: Long,
    @ColumnInfo(name = "DS_DIA")
    val dsDia: String,
    @ColumnInfo(name = "NR_ORDEM")
    val nrOrdem: Int,
    @ColumnInfo(name = "DS_GRUPO_MUSCULAR")
    val dsGrupoMuscular: String? = null
)

@Entity(tableName = "tb_DT_treinos_fichas_exercicios")
data class TreinoFichaExercicioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CD_FICHA_EXERCICIO")
    val cdFichaExercicio: Long = 0,
    @ColumnInfo(name = "CD_FICHA_DIA")
    val cdFichaDia: Long,
    @ColumnInfo(name = "CD_EXERCICIO")
    val cdExercicio: Long, // TODO: Integração com tabela de exercícios geral
    @ColumnInfo(name = "NR_ORDEM")
    val nrOrdem: Int,
    @ColumnInfo(name = "NR_SERIES_PLANEJADAS")
    val nrSeriesPlanejadas: Int,
    @ColumnInfo(name = "NR_REPETICOES_PLANEJADAS")
    val nrRepeticoesPlanejadas: Int,
    @ColumnInfo(name = "NM_CARGA_RECOMENDADA")
    val nmCargaRecomendada: Float? = null,
    @ColumnInfo(name = "NR_DESCANSO_SEGUNDOS")
    val nrDescansoSegundos: Int? = null,
    @ColumnInfo(name = "DS_OBS")
    val dsObs: String? = null
)

@Entity(tableName = "tb_DT_treinos_sessoes")
data class TreinoSessaoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CD_TREINO_SESSAO")
    val cdTreinoSessao: Long = 0,
    @ColumnInfo(name = "CD_CLIENTE")
    val cdCliente: Long,
    @ColumnInfo(name = "CD_FICHA_DIA")
    val cdFichaDia: Long,
    @ColumnInfo(name = "DT_INICIO")
    val dtInicio: Date,
    @ColumnInfo(name = "DT_FIM")
    val dtFim: Date? = null,
    @ColumnInfo(name = "ST_STATUS")
    val stStatus: String, // EM_ANDAMENTO, CONCLUIDO, CANCELADO
    @ColumnInfo(name = "DS_ORIGEM")
    val dsOrigem: String, // MANUAL, IMPORTADO, APP
    @ColumnInfo(name = "DS_OBS")
    val dsObs: String? = null
) {
    companion object {
        const val STATUS_EM_ANDAMENTO = "EM_ANDAMENTO"
        const val STATUS_CONCLUIDO = "CONCLUIDO"
        const val STATUS_CANCELADO = "CANCELADO"

        const val ORIGEM_MANUAL = "MANUAL"
        const val ORIGEM_IMPORTADO = "IMPORTADO"
        const val ORIGEM_APP = "APP"
    }
}

@Entity(tableName = "tb_DT_treinos_series")
data class TreinoSerieEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CD_SERIE")
    val cdSerie: Long = 0,
    @ColumnInfo(name = "CD_TREINO_SESSAO")
    val cdTreinoSessao: Long,
    @ColumnInfo(name = "CD_FICHA_EXERCICIO")
    val cdFichaExercicio: Long,
    @ColumnInfo(name = "NR_SERIE")
    val nrSerie: Int,
    @ColumnInfo(name = "NM_CARGA")
    val nmCarga: Float? = null,
    @ColumnInfo(name = "NR_REPETICOES")
    val nrRepeticoes: Int? = null,
    @ColumnInfo(name = "ST_CONCLUIDA")
    val stConcluida: Boolean,
    @ColumnInfo(name = "NM_ESFORCO")
    val nmEsforco: Float? = null,
    @ColumnInfo(name = "DS_OBS")
    val dsObs: String? = null
)

@Entity(tableName = "tb_DT_treinos_importacoes")
data class TreinoImportacaoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CD_IMPORTACAO")
    val cdImportacao: Long = 0,
    @ColumnInfo(name = "CD_CLIENTE")
    val cdCliente: Long,
    @ColumnInfo(name = "DT_IMPORTACAO")
    val dtImportacao: Date,
    @ColumnInfo(name = "DS_ORIGEM")
    val dsOrigem: String, // FOTO, PDF, TEXTO
    @ColumnInfo(name = "DS_TEXTO_EXTRAIDO")
    val dsTextoExtraido: String? = null,
    @ColumnInfo(name = "ST_STATUS")
    val stStatus: String, // PENDENTE, REVISADA, IMPORTADA, ERRO
    @ColumnInfo(name = "DS_OBS")
    val dsObs: String? = null
) {
    companion object {
        const val ORIGEM_FOTO = "FOTO"
        const val ORIGEM_PDF = "PDF"
        const val ORIGEM_TEXTO = "TEXTO"

        const val STATUS_PENDENTE = "PENDENTE"
        const val STATUS_REVISADA = "REVISADA"
        const val STATUS_IMPORTADA = "IMPORTADA"
        const val STATUS_ERRO = "ERRO"
    }
}
