package com.app.vitaltrack.data.dao

import androidx.room.*
import com.app.vitaltrack.data.entity.ExercicioEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ExercicioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirExercicio(exercicio: ExercicioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirExercicios(exercicios: List<ExercicioEntity>)

    @Update
    suspend fun atualizarExercicio(exercicio: ExercicioEntity)

    @Delete
    suspend fun excluirExercicio(exercicio: ExercicioEntity)

    @Query("SELECT * FROM tb_DT_exercicios WHERE CD_CLIENTE = :cdCliente ORDER BY DT_DIA DESC")
    fun listarExerciciosPorCliente(cdCliente: Long): Flow<List<ExercicioEntity>>

    @Query("SELECT * FROM tb_DT_exercicios WHERE CD_CLIENTE = :cdCliente AND DT_DIA = :dtDia")
    fun listarExerciciosPorClienteEDia(cdCliente: Long, dtDia: Date): Flow<List<ExercicioEntity>>

    @Query("SELECT * FROM tb_DT_exercicios")
    suspend fun getAllExercicios(): List<ExercicioEntity>
}
