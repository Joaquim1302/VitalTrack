package com.app.vitaltrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.app.vitaltrack.data.entity.AlimentoEntity
import com.app.vitaltrack.data.entity.RefeicaoItemEntity
import com.app.vitaltrack.data.entity.RefeicaoTipoEntity
import com.app.vitaltrack.data.entity.UnidadeEntity

@Dao
interface ImportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlimentos(alimentos: List<AlimentoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRefeicoesTipos(tipos: List<RefeicaoTipoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRefeicoesItens(itens: List<RefeicaoItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUnidades(unidades: List<UnidadeEntity>)
}
