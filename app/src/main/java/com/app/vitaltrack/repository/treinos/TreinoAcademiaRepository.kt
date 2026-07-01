package com.app.vitaltrack.repository.treinos

import com.app.vitaltrack.data.dao.treinos.TreinoAcademiaDao
import com.app.vitaltrack.data.entity.treinos.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

class TreinoAcademiaRepository(private val dao: TreinoAcademiaDao) {

    fun buscarFichaAtiva(cdCliente: Long): Flow<TreinoFichaEntity?> = dao.buscarFichaAtiva(cdCliente)

    suspend fun criarFichaDeTreino(ficha: TreinoFichaEntity): Long {
        if (ficha.stAtiva) {
            dao.desativarFichasAnteriores(ficha.cdCliente)
        }
        return dao.inserirFicha(ficha)
    }

    suspend fun criarDivisaoDaFicha(dia: TreinoFichaDiaEntity): Long = dao.inserirDia(dia)

    fun listarDias(cdFicha: Long): Flow<List<TreinoFichaDiaEntity>> = dao.listarDias(cdFicha)

    suspend fun adicionarExercicioPlanejado(exercicio: TreinoFichaExercicioEntity): Long = 
        dao.inserirExercicioPlanejado(exercicio)

    fun listarExerciciosPlanejados(cdFichaDia: Long): Flow<List<TreinoFichaExercicioEntity>> = 
        dao.listarExerciciosPlanejados(cdFichaDia)

    suspend fun criarSessaoDeTreino(sessao: TreinoSessaoEntity): Long = dao.inserirSessao(sessao)

    suspend fun buscarSessaoEmAndamento(cdCliente: Long): TreinoSessaoEntity? = 
        dao.buscarSessaoEmAndamento(cdCliente)

    suspend fun concluirSessaoDeTreino(sessao: TreinoSessaoEntity) {
        val concluida = sessao.copy(
            stStatus = TreinoSessaoEntity.STATUS_CONCLUIDO,
            dtFim = Date()
        )
        dao.atualizarSessao(concluida)
    }

    suspend fun registrarSerieRealizada(serie: TreinoSerieEntity): Long = dao.inserirSerie(serie)

    fun listarSeriesDaSessao(cdTreinoSessao: Long): Flow<List<TreinoSerieEntity>> = 
        dao.listarSeriesPorSessao(cdTreinoSessao)

    suspend fun criarRegistroDeImportacao(importacao: TreinoImportacaoEntity): Long = 
        dao.inserirImportacao(importacao)

    suspend fun atualizarStatusDaImportacao(cdImportacao: Long, status: String) {
        val importacao = dao.buscarImportacao(cdImportacao)
        if (importacao != null) {
            dao.atualizarImportacao(importacao.copy(stStatus = status))
        }
    }

    suspend fun criarDadosExemploSeNecessario(cdCliente: Long) {
        val count = dao.contarFichasDoCliente(cdCliente)
        if (count == 0) {
            val fichaId = dao.inserirFicha(TreinoFichaEntity(
                cdCliente = cdCliente,
                dsFicha = "Hipertrofia A/B/C",
                dtInicio = Date(),
                stAtiva = true
            ))

            // Treino A
            val diaA = dao.inserirDia(TreinoFichaDiaEntity(
                cdFicha = fichaId,
                dsDia = "Treino A",
                nrOrdem = 1,
                dsGrupoMuscular = "Peito e Tríceps"
            ))
            dao.inserirExercicioPlanejado(TreinoFichaExercicioEntity(cdFichaDia = diaA, cdExercicio = 1, nrOrdem = 1, nrSeriesPlanejadas = 4, nrRepeticoesPlanejadas = 10, nrDescansoSegundos = 90, dsObs = "Supino reto"))
            dao.inserirExercicioPlanejado(TreinoFichaExercicioEntity(cdFichaDia = diaA, cdExercicio = 2, nrOrdem = 2, nrSeriesPlanejadas = 3, nrRepeticoesPlanejadas = 12, nrDescansoSegundos = 75, dsObs = "Supino inclinado"))
            dao.inserirExercicioPlanejado(TreinoFichaExercicioEntity(cdFichaDia = diaA, cdExercicio = 3, nrOrdem = 3, nrSeriesPlanejadas = 3, nrRepeticoesPlanejadas = 12, nrDescansoSegundos = 60, dsObs = "Tríceps pulley"))

            // Treino B
            val diaB = dao.inserirDia(TreinoFichaDiaEntity(
                cdFicha = fichaId,
                dsDia = "Treino B",
                nrOrdem = 2,
                dsGrupoMuscular = "Costas e Bíceps"
            ))
            dao.inserirExercicioPlanejado(TreinoFichaExercicioEntity(cdFichaDia = diaB, cdExercicio = 4, nrOrdem = 1, nrSeriesPlanejadas = 4, nrRepeticoesPlanejadas = 10, nrDescansoSegundos = 90, dsObs = "Puxada frontal"))
            dao.inserirExercicioPlanejado(TreinoFichaExercicioEntity(cdFichaDia = diaB, cdExercicio = 5, nrOrdem = 2, nrSeriesPlanejadas = 3, nrRepeticoesPlanejadas = 12, nrDescansoSegundos = 75, dsObs = "Remada baixa"))
            dao.inserirExercicioPlanejado(TreinoFichaExercicioEntity(cdFichaDia = diaB, cdExercicio = 6, nrOrdem = 3, nrSeriesPlanejadas = 3, nrRepeticoesPlanejadas = 12, nrDescansoSegundos = 60, dsObs = "Rosca direta"))

            // Treino C
            val diaC = dao.inserirDia(TreinoFichaDiaEntity(
                cdFicha = fichaId,
                dsDia = "Treino C",
                nrOrdem = 3,
                dsGrupoMuscular = "Pernas"
            ))
            dao.inserirExercicioPlanejado(TreinoFichaExercicioEntity(cdFichaDia = diaC, cdExercicio = 7, nrOrdem = 1, nrSeriesPlanejadas = 4, nrRepeticoesPlanejadas = 10, nrDescansoSegundos = 90, dsObs = "Leg press"))
            dao.inserirExercicioPlanejado(TreinoFichaExercicioEntity(cdFichaDia = diaC, cdExercicio = 8, nrOrdem = 2, nrSeriesPlanejadas = 3, nrRepeticoesPlanejadas = 12, nrDescansoSegundos = 60, dsObs = "Cadeira extensora"))
            dao.inserirExercicioPlanejado(TreinoFichaExercicioEntity(cdFichaDia = diaC, cdExercicio = 9, nrOrdem = 3, nrSeriesPlanejadas = 3, nrRepeticoesPlanejadas = 12, nrDescansoSegundos = 60, dsObs = "Mesa flexora"))
        }
    }
}
