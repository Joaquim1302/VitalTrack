package com.app.vitaltrack.data.dao

import androidx.room.*
import com.app.vitaltrack.data.entity.ClienteEntity

@Dao
interface ClienteDao {

    @Query("""
        SELECT *
        FROM tb_DT_clientes
        ORDER BY DS_NOME
    """)
    fun listar(): List<ClienteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserir(cliente: ClienteEntity): Long

    @Update
    fun atualizar(cliente: ClienteEntity): Int

    @Delete
    fun excluir(cliente: ClienteEntity): Int

    @Query("""
        SELECT *
        FROM tb_DT_clientes
        WHERE CD_CLIENTE = :id
        LIMIT 1
    """)
    fun obter(id: Long): ClienteEntity?
}
