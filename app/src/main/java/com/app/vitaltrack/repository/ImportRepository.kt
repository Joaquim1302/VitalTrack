package com.app.vitaltrack.repository

import com.app.vitaltrack.data.dao.ImportDao
import com.app.vitaltrack.data.entity.AlimentoEntity
import com.app.vitaltrack.data.entity.RefeicaoItemEntity
import com.app.vitaltrack.data.entity.RefeicaoTipoEntity
import com.app.vitaltrack.data.entity.UnidadeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImportRepository(private val importDao: ImportDao) {
    suspend fun importAlimentos(alimentos: List<AlimentoEntity>) = withContext(Dispatchers.IO) {
        importDao.insertAlimentos(alimentos)
    }
    
    suspend fun importRefeicoesTipos(tipos: List<RefeicaoTipoEntity>) = withContext(Dispatchers.IO) {
        importDao.insertRefeicoesTipos(tipos)
    }
    
    suspend fun importRefeicoesItens(itens: List<RefeicaoItemEntity>) = withContext(Dispatchers.IO) {
        importDao.insertRefeicoesItens(itens)
    }
    
    suspend fun importUnidades(unidades: List<UnidadeEntity>) = withContext(Dispatchers.IO) {
        importDao.insertUnidades(unidades)
    }
}
