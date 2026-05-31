package com.app.vitaltrack.repository

import com.app.vitaltrack.data.dao.*
import com.app.vitaltrack.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealRepository(
    private val mealDao: MealDao,
    private val refeicaoSalvaDao: RefeicaoSalvaDao
) {
    
    fun getMostUsedFoods() = mealDao.getMostUsedFoods()
    
    fun getFavorites(refeicaoTipoId: Int?) = mealDao.getFavorites(refeicaoTipoId)

    fun listarRefeicoesSalvas() = refeicaoSalvaDao.listarRefeicoesSalvasComItens()

    suspend fun buscarRefeicaoSalvaComItens(id: Long) = withContext(Dispatchers.IO) {
        refeicaoSalvaDao.buscarRefeicaoSalvaComItens(id)
    }

    suspend fun salvarRefeicaoCompleta(refeicao: RefeicaoSalvaEntity, itens: List<RefeicaoSalvaItemEntity>) = withContext(Dispatchers.IO) {
        refeicaoSalvaDao.salvarRefeicaoCompleta(refeicao, itens)
    }

    suspend fun salvarRefeicaoSalva(
        nome: String,
        refeicaoTipoId: Int?,
        itens: List<RefeicaoItemComDescricao>
    ) = withContext(Dispatchers.IO) {
        val totalCal = itens.sumOf { ((it.nmQnt ?: 0.0) / (it.nmQntBase?.toDouble() ?: 1.0)) * (it.nmCal ?: 0.0) }
        val totalProt = itens.sumOf { ((it.nmQnt ?: 0.0) / (it.nmQntBase?.toDouble() ?: 1.0)) * (it.nmProt ?: 0.0) }
        val totalCarb = itens.sumOf { ((it.nmQnt ?: 0.0) / (it.nmQntBase?.toDouble() ?: 1.0)) * (it.nmCarb ?: 0.0) }
        val totalGord = itens.sumOf { ((it.nmQnt ?: 0.0) / (it.nmQntBase?.toDouble() ?: 1.0)) * (it.nmGord ?: 0.0) }

        val refeicao = RefeicaoSalvaEntity(
            dsRefeicaoSalva = nome,
            cdRefeicaoTp = refeicaoTipoId,
            nmCalTotal = totalCal,
            nmProtTotal = totalProt,
            nmCarbTotal = totalCarb,
            nmGordTotal = totalGord
        )

        val itensSalvos = itens.map {
            RefeicaoSalvaItemEntity(
                cdRefeicaoSalva = 0,
                cdAlimento = it.cdAlimento,
                dsAlimento = it.dsAlimento ?: "",
                nmQtd = it.nmQnt ?: 0.0,
                dsUnidade = it.dsUnidade ?: "g",
                nmCal = ((it.nmQnt ?: 0.0) / (it.nmQntBase?.toDouble() ?: 1.0)) * (it.nmCal ?: 0.0),
                nmProt = ((it.nmQnt ?: 0.0) / (it.nmQntBase?.toDouble() ?: 1.0)) * (it.nmProt ?: 0.0),
                nmCarb = ((it.nmQnt ?: 0.0) / (it.nmQntBase?.toDouble() ?: 1.0)) * (it.nmCarb ?: 0.0),
                nmGord = ((it.nmQnt ?: 0.0) / (it.nmQntBase?.toDouble() ?: 1.0)) * (it.nmGord ?: 0.0)
            )
        }
        refeicaoSalvaDao.salvarRefeicaoCompleta(refeicao, itensSalvos)
    }

    suspend fun excluirRefeicaoSalva(id: Long) = withContext(Dispatchers.IO) {
        refeicaoSalvaDao.excluirRefeicaoSalva(id)
    }

    suspend fun insertMealItems(items: List<RefeicaoItemEntity>) = withContext(Dispatchers.IO) {
        mealDao.insertMealItems(items)
    }
    
    fun getItemsForMeal(date: String, refeicaoTipoId: Int) = mealDao.getItemsForMeal(date, refeicaoTipoId)

    fun getAlimentosDisponiveis() = mealDao.getAlimentosDisponiveis()

    fun searchAlimentosDisponiveis(query: String) = mealDao.searchAlimentosDisponiveis(query)

    fun getRecentFoods(startDate: String) = mealDao.getRecentFoods(startDate)

    suspend fun saveMealAsFavorite(name: String, refeicaoTipoId: Int?, items: List<RefeicaoItemEntity>) = withContext(Dispatchers.IO) {
        val favoriteId = mealDao.insertFavorite(
            RefeicaoFavoritaEntity(dsFavorita = name, cdRefeicaoTp = refeicaoTipoId)
        )
        val favoriteItems = items.map {
            RefeicaoFavoritaItemEntity(
                cdFavorita = favoriteId,
                cdAlimento = it.cdAlimento,
                nmQnt = it.nmQnt ?: 0.0
            )
        }
        mealDao.insertFavoriteItems(favoriteItems)
    }

    suspend fun importFavorite(favoritaId: Long, date: String, clienteId: Long, refeicaoTipoId: Int) = withContext(Dispatchers.IO) {
        val items = mealDao.getFavoriteItemsSync(favoritaId)
        val mealItems = items.map {
            val existing = mealDao.getSpecificMealItemSync(date, refeicaoTipoId, it.cdAlimento, clienteId)
            RefeicaoItemEntity(
                dtConsumo = date,
                cdAlimento = it.cdAlimento,
                cdCliente = clienteId,
                cdFase = 1,
                cdRefeicaoTp = refeicaoTipoId,
                nmQnt = (existing?.nmQnt ?: 0.0) + it.nmQnt
            )
        }
        mealDao.insertMealItems(mealItems)
    }

    suspend fun copyPreviousMeal(clienteId: Long, refeicaoTipoId: Int, currentDate: String) = withContext(Dispatchers.IO) {
        val lastDate = mealDao.getLastMealDateSync(clienteId, refeicaoTipoId, currentDate)
        if (lastDate != null) {
            val items = mealDao.getMealItemsByDateSync(clienteId, refeicaoTipoId, lastDate)
            val newItems = items.map { item ->
                val existing = mealDao.getSpecificMealItemSync(currentDate, refeicaoTipoId, item.cdAlimento, clienteId)
                item.copy(
                    dtConsumo = currentDate,
                    nmQnt = (existing?.nmQnt ?: 0.0) + (item.nmQnt ?: 0.0)
                )
            }
            mealDao.insertMealItems(newItems)
            true
        } else {
            false
        }
    }

    suspend fun addFoodToMeal(date: String, clienteId: Long, refeicaoTipoId: Int, alimentoId: Long, quantity: Double, unit: String) = withContext(Dispatchers.IO) {
        val existing = mealDao.getSpecificMealItemSync(date, refeicaoTipoId, alimentoId, clienteId)
        val newQuantity = (existing?.nmQnt ?: 0.0) + quantity
        
        val item = RefeicaoItemEntity(
            dtConsumo = date,
            cdAlimento = alimentoId,
            cdCliente = clienteId,
            cdFase = 1,
            cdRefeicaoTp = refeicaoTipoId,
            nmQnt = newQuantity,
            dsUnidade = unit
        )
        mealDao.insertMealItems(listOf(item))
    }

    suspend fun updateFoodInMeal(date: String, clienteId: Long, refeicaoTipoId: Int, alimentoId: Long, quantity: Double, unit: String) = withContext(Dispatchers.IO) {
        val item = RefeicaoItemEntity(
            dtConsumo = date,
            cdAlimento = alimentoId,
            cdCliente = clienteId,
            cdFase = 1,
            cdRefeicaoTp = refeicaoTipoId,
            nmQnt = quantity,
            dsUnidade = unit
        )
        mealDao.insertMealItems(listOf(item))
    }

    suspend fun deleteItem(date: String, refeicaoTipoId: Int, alimentoId: Long) = withContext(Dispatchers.IO) {
        mealDao.deleteItem(date, refeicaoTipoId, alimentoId)
    }

    suspend fun getAlimentoById(id: Long) = withContext(Dispatchers.IO) {
        mealDao.getAlimentoByIdSync(id)
    }
}
