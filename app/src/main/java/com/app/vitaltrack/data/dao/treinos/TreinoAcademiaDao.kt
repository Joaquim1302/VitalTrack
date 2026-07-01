package com.app.vitaltrack.data.dao.treinos

import androidx.room.*
import com.app.vitaltrack.data.entity.treinos.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TreinoAcademiaDao {

    // Fichas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirFicha(ficha: TreinoFichaEntity): Long

    @Update
    suspend fun atualizarFicha(ficha: TreinoFichaEntity)

    @Query("SELECT * FROM tb_DT_treinos_fichas WHERE CD_CLIENTE = :cdCliente AND ST_ATIVA = 1 LIMIT 1")
    fun buscarFichaAtiva(cdCliente: Long): Flow<TreinoFichaEntity?>

    @Query("SELECT * FROM tb_DT_treinos_fichas WHERE CD_CLIENTE = :cdCliente ORDER BY DT_INICIO DESC")
    fun listarFichas(cdCliente: Long): Flow<List<TreinoFichaEntity>>

    @Query("UPDATE tb_DT_treinos_fichas SET ST_ATIVA = 0 WHERE CD_CLIENTE = :cdCliente")
    suspend fun desativarFichasAnteriores(cdCliente: Long)

    @Query("SELECT COUNT(*) FROM tb_DT_treinos_fichas WHERE CD_CLIENTE = :cdCliente")
    suspend fun contarFichasDoCliente(cdCliente: Long): Int

    // Dias/Divisões
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirDia(dia: TreinoFichaDiaEntity): Long

    @Query("SELECT * FROM tb_DT_treinos_fichas_dias WHERE CD_FICHA = :cdFicha ORDER BY NR_ORDEM")
    fun listarDias(cdFicha: Long): Flow<List<TreinoFichaDiaEntity>>

    @Query("SELECT * FROM tb_DT_treinos_fichas_dias WHERE CD_FICHA_DIA = :cdFichaDia")
    suspend fun buscarDia(cdFichaDia: Long): TreinoFichaDiaEntity?

    // Exercícios Planejados
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirExercicioPlanejado(exercicio: TreinoFichaExercicioEntity): Long

    @Query("SELECT * FROM tb_DT_treinos_fichas_exercicios WHERE CD_FICHA_DIA = :cdFichaDia ORDER BY NR_ORDEM")
    fun listarExerciciosPlanejados(cdFichaDia: Long): Flow<List<TreinoFichaExercicioEntity>>

    @Query("SELECT * FROM tb_DT_treinos_fichas_exercicios WHERE CD_FICHA_EXERCICIO = :cdFichaExercicio")
    suspend fun buscarExercicioPlanejado(cdFichaExercicio: Long): TreinoFichaExercicioEntity?

    // Sessões
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirSessao(sessao: TreinoSessaoEntity): Long

    @Update
    suspend fun atualizarSessao(sessao: TreinoSessaoEntity)

    @Query("SELECT * FROM tb_DT_treinos_sessoes WHERE CD_CLIENTE = :cdCliente AND ST_STATUS = 'EM_ANDAMENTO' LIMIT 1")
    suspend fun buscarSessaoEmAndamento(cdCliente: Long): TreinoSessaoEntity?

    @Query("SELECT * FROM tb_DT_treinos_sessoes WHERE CD_CLIENTE = :cdCliente AND CD_FICHA_DIA = :cdFichaDia AND ST_STATUS = 'EM_ANDAMENTO' LIMIT 1")
    suspend fun buscarSessaoEmAndamentoPorDia(cdCliente: Long, cdFichaDia: Long): TreinoSessaoEntity?

    @Query("SELECT * FROM tb_DT_treinos_sessoes WHERE CD_TREINO_SESSAO = :cdSessao")
    suspend fun buscarSessaoPorId(cdSessao: Long): TreinoSessaoEntity?

    @Query("SELECT * FROM tb_DT_treinos_sessoes WHERE CD_CLIENTE = :cdCliente ORDER BY DT_INICIO DESC")
    fun listarSessoes(cdCliente: Long): Flow<List<TreinoSessaoEntity>>

    @Query("SELECT * FROM tb_DT_treinos_sessoes WHERE DT_INICIO >= :inicio AND DT_INICIO <= :fim ORDER BY DT_INICIO DESC")
    fun listarSessoesPorPeriodo(inicio: Long, fim: Long): Flow<List<TreinoSessaoEntity>>

    // Séries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirSerie(serie: TreinoSerieEntity): Long

    @Update
    suspend fun atualizarSerie(serie: TreinoSerieEntity)

    @Query("SELECT * FROM tb_DT_treinos_series WHERE CD_TREINO_SESSAO = :cdTreinoSessao ORDER BY CD_FICHA_EXERCICIO, NR_SERIE")
    fun listarSeriesPorSessao(cdTreinoSessao: Long): Flow<List<TreinoSerieEntity>>

    @Query("SELECT * FROM tb_DT_treinos_series WHERE CD_TREINO_SESSAO = :cdTreinoSessao AND CD_FICHA_EXERCICIO = :cdFichaExercicio ORDER BY NR_SERIE")
    fun listarSeriesPorExercicio(cdTreinoSessao: Long, cdFichaExercicio: Long): Flow<List<TreinoSerieEntity>>

    // Importações
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirImportacao(importacao: TreinoImportacaoEntity): Long

    @Update
    suspend fun atualizarImportacao(importacao: TreinoImportacaoEntity)

    @Query("SELECT * FROM tb_DT_treinos_importacoes WHERE CD_CLIENTE = :cdCliente ORDER BY DT_IMPORTACAO DESC")
    fun listarImportacoes(cdCliente: Long): Flow<List<TreinoImportacaoEntity>>

    @Query("SELECT * FROM tb_DT_treinos_importacoes WHERE CD_IMPORTACAO = :cdImportacao")
    suspend fun buscarImportacao(cdImportacao: Long): TreinoImportacaoEntity?
}
