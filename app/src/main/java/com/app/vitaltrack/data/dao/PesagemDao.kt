package com.app.vitaltrack.data.dao

import androidx.room.*
import com.app.vitaltrack.data.entity.PesagemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PesagemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirPesagem(pesagem: PesagemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirPesagens(pesagens: List<PesagemEntity>)

    @Update
    suspend fun atualizarPesagem(pesagem: PesagemEntity)

    @Delete
    suspend fun excluirPesagem(pesagem: PesagemEntity)

    @Query("SELECT * FROM tb_DT_pesagens WHERE CD_CLIENTE = :cdCliente ORDER BY DT_PESAGEM DESC, HR_PESAGEM DESC")
    fun listarPesagensPorCliente(cdCliente: Long): Flow<List<PesagemEntity>>

    @Query("SELECT * FROM tb_DT_pesagens WHERE CD_CLIENTE = :cdCliente LIMIT 1")
    suspend fun obterPrimeiraPesagemDoCliente(cdCliente: Long): PesagemEntity?

    @Query("SELECT * FROM tb_DT_pesagens WHERE CD_CLIENTE = :cdCliente ORDER BY DT_PESAGEM DESC, HR_PESAGEM DESC LIMIT 1")
    suspend fun obterUltimaPesagemDoCliente(cdCliente: Long): PesagemEntity?

    @Query("SELECT * FROM tb_DT_pesagens")
    suspend fun getAllPesagens(): List<PesagemEntity>
}
