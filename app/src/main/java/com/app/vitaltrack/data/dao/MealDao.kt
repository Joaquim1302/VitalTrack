package com.app.vitaltrack.data.dao

import androidx.room.*
import com.app.vitaltrack.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("""
        SELECT 
            a.CD_ALIMENTO,
            a.DS_ALIMENTO,
            a.CD_UNIDADE,
            u.DS_UNIDADE,
            COUNT(*) AS TOTAL_USOS
        FROM tb_DT_refeicoes_itens r
        INNER JOIN tb_DT_alimentos a ON a.CD_ALIMENTO = r.CD_ALIMENTO
        LEFT JOIN tb_DT_unidades u ON u.CD_UNIDADE = a.CD_UNIDADE
        GROUP BY a.CD_ALIMENTO, a.DS_ALIMENTO, a.CD_UNIDADE, u.DS_UNIDADE
        ORDER BY TOTAL_USOS DESC
        LIMIT 10
    """)
    fun getMostUsedFoods(): Flow<List<MostUsedFood>>

    @Insert
    fun insertFavorite(favorite: RefeicaoFavoritaEntity): Long

    @Insert
    fun insertFavoriteItems(items: List<RefeicaoFavoritaItemEntity>): LongArray

    @Query("SELECT * FROM tb_DT_refeicoes_favoritas WHERE CD_REFEICAO_TP = :refeicaoTipoId OR :refeicaoTipoId IS NULL ORDER BY DT_CRIACAO DESC")
    fun getFavorites(refeicaoTipoId: Int?): Flow<List<RefeicaoFavoritaEntity>>

    @Query("SELECT * FROM tb_DT_refeicoes_favoritas_itens WHERE CD_FAVORITA = :favoritaId")
    fun getFavoriteItemsSync(favoritaId: Long): List<RefeicaoFavoritaItemEntity>

    @Query("""
        SELECT MAX(DT_CONSUMO)
        FROM tb_DT_refeicoes_itens
        WHERE CD_CLIENTE = :clienteId
        AND CD_REFEICAO_TP = :refeicaoTipoId
        AND DT_CONSUMO < :dataAtual
    """)
    fun getLastMealDateSync(clienteId: Long, refeicaoTipoId: Int, dataAtual: String): String?

    @Query("""
        SELECT *
        FROM tb_DT_refeicoes_itens
        WHERE CD_CLIENTE = :clienteId
        AND CD_REFEICAO_TP = :refeicaoTipoId
        AND DT_CONSUMO = :dataAnterior
    """)
    fun getMealItemsByDateSync(clienteId: Long, refeicaoTipoId: Int, dataAnterior: String): List<RefeicaoItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMealItems(items: List<RefeicaoItemEntity>): LongArray

    @Query("""
        SELECT 
            r.DT_CONSUMO,
            r.CD_ALIMENTO,
            a.DS_ALIMENTO,
            r.CD_CLIENTE,
            r.CD_FASE,
            r.CD_REFEICAO_TP,
            r.NM_QNT,
            u.DS_UNIDADE
        FROM tb_DT_refeicoes_itens r
        INNER JOIN tb_DT_alimentos a ON a.CD_ALIMENTO = r.CD_ALIMENTO
        LEFT JOIN tb_DT_unidades u ON u.CD_UNIDADE = a.CD_UNIDADE
        WHERE r.DT_CONSUMO = :date AND r.CD_REFEICAO_TP = :refeicaoTipoId
    """)
    fun getItemsForMeal(date: String, refeicaoTipoId: Int): Flow<List<RefeicaoItemComDescricao>>

    @Query("SELECT * FROM tb_DT_refeicoes_itens WHERE DT_CONSUMO = :date AND CD_REFEICAO_TP = :refeicaoTipoId AND CD_ALIMENTO = :alimentoId AND CD_CLIENTE = :clienteId LIMIT 1")
    fun getSpecificMealItemSync(date: String, refeicaoTipoId: Int, alimentoId: Long, clienteId: Long): RefeicaoItemEntity?
    
    @Query("DELETE FROM tb_DT_refeicoes_itens WHERE DT_CONSUMO = :date AND CD_REFEICAO_TP = :refeicaoTipoId AND CD_ALIMENTO = :alimentoId")
    fun deleteItem(date: String, refeicaoTipoId: Int, alimentoId: Long): Int

    @Query("SELECT * FROM tb_DT_alimentos WHERE CD_ALIMENTO = :id")
    fun getAlimentoByIdSync(id: Long): AlimentoEntity?

    @Query("""
        SELECT 
            a.CD_ALIMENTO,
            a.DS_ALIMENTO,
            a.CD_UNIDADE,
            u.DS_UNIDADE,
            a.NM_QNT_BASE,
            a.NM_CAL,
            a.NM_PROT,
            a.NM_CARB,
            a.NM_GORD
        FROM tb_DT_alimentos a
        LEFT JOIN tb_DT_unidades u ON u.CD_UNIDADE = a.CD_UNIDADE
        ORDER BY a.DS_ALIMENTO ASC
    """)
    fun getAlimentosDisponiveis(): Flow<List<AlimentoDisponivel>>

    @Query("""
        SELECT 
            a.CD_ALIMENTO,
            a.DS_ALIMENTO,
            a.CD_UNIDADE,
            u.DS_UNIDADE,
            a.NM_QNT_BASE,
            a.NM_CAL,
            a.NM_PROT,
            a.NM_CARB,
            a.NM_GORD
        FROM tb_DT_alimentos a
        LEFT JOIN tb_DT_unidades u ON u.CD_UNIDADE = a.CD_UNIDADE
        WHERE a.DS_ALIMENTO LIKE '%' || :query || '%'
        ORDER BY a.DS_ALIMENTO ASC
    """)
    fun searchAlimentosDisponiveis(query: String): Flow<List<AlimentoDisponivel>>
}

data class MostUsedFood(
    @ColumnInfo(name = "CD_ALIMENTO") val cdAlimento: Long,
    @ColumnInfo(name = "DS_ALIMENTO") val dsAlimento: String?,
    @ColumnInfo(name = "CD_UNIDADE") val cdUnidade: Int?,
    @ColumnInfo(name = "DS_UNIDADE") val dsUnidade: String?,
    @ColumnInfo(name = "TOTAL_USOS") val totalUsos: Int
)

data class AlimentoDisponivel(
    @ColumnInfo(name = "CD_ALIMENTO") val cdAlimento: Long,
    @ColumnInfo(name = "DS_ALIMENTO") val dsAlimento: String?,
    @ColumnInfo(name = "CD_UNIDADE") val cdUnidade: Int?,
    @ColumnInfo(name = "DS_UNIDADE") val dsUnidade: String?,
    @ColumnInfo(name = "NM_QNT_BASE") val nmQntBase: Long?,
    @ColumnInfo(name = "NM_CAL") val nmCal: Double?,
    @ColumnInfo(name = "NM_PROT") val nmProt: Double?,
    @ColumnInfo(name = "NM_CARB") val nmCarb: Double?,
    @ColumnInfo(name = "NM_GORD") val nmGord: Double?
)

data class RefeicaoItemComDescricao(
    @ColumnInfo(name = "DT_CONSUMO") val dtConsumo: String,
    @ColumnInfo(name = "CD_ALIMENTO") val cdAlimento: Long,
    @ColumnInfo(name = "DS_ALIMENTO") val dsAlimento: String?,
    @ColumnInfo(name = "CD_CLIENTE") val cdCliente: Long,
    @ColumnInfo(name = "CD_FASE") val cdFase: Int?,
    @ColumnInfo(name = "CD_REFEICAO_TP") val cdRefeicaoTp: Int,
    @ColumnInfo(name = "NM_QNT") val nmQnt: Double?,
    @ColumnInfo(name = "DS_UNIDADE") val dsUnidade: String?
)
