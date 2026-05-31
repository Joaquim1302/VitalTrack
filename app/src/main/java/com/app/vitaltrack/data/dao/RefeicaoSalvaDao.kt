package com.app.vitaltrack.data.dao

import androidx.room.*
import com.app.vitaltrack.data.entity.RefeicaoSalvaComItens
import com.app.vitaltrack.data.entity.RefeicaoSalvaEntity
import com.app.vitaltrack.data.entity.RefeicaoSalvaItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RefeicaoSalvaDao {

    @Insert
    abstract fun inserirRefeicaoSalva(refeicao: RefeicaoSalvaEntity): Long

    @Insert
    abstract fun inserirItens(itens: List<RefeicaoSalvaItemEntity>): LongArray

    @Query("SELECT * FROM tb_DT_refeicoes_salvas ORDER BY DT_ATUALIZACAO DESC, DT_CRIACAO DESC")
    abstract fun listarRefeicoesSalvas(): Flow<List<RefeicaoSalvaEntity>>

    @Transaction
    @Query("SELECT * FROM tb_DT_refeicoes_salvas ORDER BY DT_ATUALIZACAO DESC, DT_CRIACAO DESC")
    abstract fun listarRefeicoesSalvasComItens(): Flow<List<RefeicaoSalvaComItens>>

    @Transaction
    @Query("SELECT * FROM tb_DT_refeicoes_salvas WHERE CD_REFEICAO_SALVA = :cdRefeicaoSalva")
    abstract fun buscarRefeicaoSalvaComItens(cdRefeicaoSalva: Long): RefeicaoSalvaComItens?

    @Query("DELETE FROM tb_DT_refeicoes_salvas WHERE CD_REFEICAO_SALVA = :cdRefeicaoSalva")
    abstract fun excluirRefeicaoSalva(cdRefeicaoSalva: Long): Int

    @Transaction
    open fun salvarRefeicaoCompleta(
        refeicao: RefeicaoSalvaEntity,
        itens: List<RefeicaoSalvaItemEntity>
    ): Long {
        val id = inserirRefeicaoSalva(refeicao)
        val itensComId = itens.map { it.copy(cdRefeicaoSalva = id) }
        inserirItens(itensComId)
        return id
    }
}
