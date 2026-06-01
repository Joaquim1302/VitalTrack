package com.app.vitaltrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.app.vitaltrack.data.entity.*

@Dao
interface ImportDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAlimentos(alimentos: List<AlimentoEntity>): LongArray

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRefeicoesTipos(tipos: List<RefeicaoTipoEntity>): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRefeicoesItens(itens: List<RefeicaoItemEntity>): LongArray

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUnidades(unidades: List<UnidadeEntity>): LongArray

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertClientes(clientes: List<ClienteEntity>): LongArray
}
