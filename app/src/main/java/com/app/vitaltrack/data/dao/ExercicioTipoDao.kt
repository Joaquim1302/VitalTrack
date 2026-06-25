package com.app.vitaltrack.data.dao

import androidx.room.*
import com.app.vitaltrack.data.entity.ExercicioTipoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExercicioTipoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTipo(tipo: ExercicioTipoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTipos(tipos: List<ExercicioTipoEntity>)

    @Query("SELECT * FROM tb_DT_exercicios_tipos ORDER BY DS_TP_EXERCICIO")
    fun listarTipos(): Flow<List<ExercicioTipoEntity>>

    @Query("SELECT * FROM tb_DT_exercicios_tipos WHERE BL_TP_EXERCICIO = 1 ORDER BY DS_TP_EXERCICIO")
    fun listarTiposAtivos(): Flow<List<ExercicioTipoEntity>>

    @Query("SELECT * FROM tb_DT_exercicios_tipos")
    suspend fun getAllTipos(): List<ExercicioTipoEntity>
}
