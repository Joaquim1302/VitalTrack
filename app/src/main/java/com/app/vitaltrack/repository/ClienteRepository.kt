package com.app.vitaltrack.repository

import com.app.vitaltrack.data.dao.ClienteDao
import com.app.vitaltrack.data.entity.ClienteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClienteRepository(private val clienteDao: ClienteDao) {

    suspend fun listar() = withContext(Dispatchers.IO) {
        clienteDao.listar()
    }

    suspend fun obter(id: Long) = withContext(Dispatchers.IO) {
        clienteDao.obter(id)
    }

    suspend fun inserir(cliente: ClienteEntity) = withContext(Dispatchers.IO) {
        clienteDao.inserir(cliente)
    }

    suspend fun atualizar(cliente: ClienteEntity) = withContext(Dispatchers.IO) {
        clienteDao.atualizar(cliente)
    }

    suspend fun excluir(cliente: ClienteEntity) = withContext(Dispatchers.IO) {
        clienteDao.excluir(cliente)
    }
}
