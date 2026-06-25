package com.app.vitaltrack.repository

import com.app.vitaltrack.data.dao.ImportDao
import com.app.vitaltrack.data.entity.*
import com.app.vitaltrack.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImportRepository(
    private val importDao: ImportDao,
    private val db: AppDatabase
) {
    suspend fun importAlimentos(alimentos: List<AlimentoEntity>) = withContext(Dispatchers.IO) {
        importDao.insertAlimentos(alimentos)
    }
    
    suspend fun importRefeicoesTipos(tipos: List<RefeicaoTipoEntity>) = withContext(Dispatchers.IO) {
        importDao.insertRefeicoesTipos(tipos)
    }
    
    suspend fun importRefeicoesItens(itens: List<RefeicaoItemEntity>) = withContext(Dispatchers.IO) {
        // Para itens de refeição, desabilitamos FKs para garantir que a carga em massa funcione
        // mesmo se a ordem dos dados no JSON estiver misturada
        db.runInTransaction {
            db.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys = OFF")
            try {
                importDao.insertRefeicoesItens(itens)
            } finally {
                db.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys = ON")
            }
        }
    }
    
    suspend fun importUnidades(unidades: List<UnidadeEntity>) = withContext(Dispatchers.IO) {
        importDao.insertUnidades(unidades)
    }

    suspend fun importClientes(clientes: List<ClienteEntity>) = withContext(Dispatchers.IO) {
        importDao.insertClientes(clientes)
    }

    suspend fun importExercicios(exercicios: List<ExercicioEntity>) = withContext(Dispatchers.IO) {
        db.exercicioDao().inserirExercicios(exercicios)
    }

    suspend fun importExercicioTipos(tipos: List<ExercicioTipoEntity>) = withContext(Dispatchers.IO) {
        db.exercicioTipoDao().inserirTipos(tipos)
    }

    suspend fun importPesagens(pesagens: List<PesagemEntity>) = withContext(Dispatchers.IO) {
        db.pesagemDao().inserirPesagens(pesagens)
    }
}
