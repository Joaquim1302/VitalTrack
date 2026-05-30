package com.app.vitaltrack.repository

import com.app.vitaltrack.data.dao.AlimentoDisponivel
import com.app.vitaltrack.data.dao.MealDao
import com.app.vitaltrack.data.dao.RefeicaoItemComDescricao
import com.app.vitaltrack.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealRepository(private val mealDao: MealDao) {
    
    fun getMostUsedFoods() = mealDao.getMostUsedFoods()
    
    fun getFavorites(refeicaoTipoId: Int?) = mealDao.getFavorites(refeicaoTipoId)
    
    fun getItemsForMeal(date: String, refeicaoTipoId: Int) = mealDao.getItemsForMeal(date, refeicaoTipoId)

    fun getAlimentosDisponiveis() = mealDao.getAlimentosDisponiveis()

    fun searchAlimentosDisponiveis(query: String) = mealDao.searchAlimentosDisponiveis(query)

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

    suspend fun addFoodToMeal(date: String, clienteId: Long, refeicaoTipoId: Int, alimentoId: Long, quantity: Double) = withContext(Dispatchers.IO) {
        val existing = mealDao.getSpecificMealItemSync(date, refeicaoTipoId, alimentoId, clienteId)
        val newQuantity = (existing?.nmQnt ?: 0.0) + quantity
        
        val item = RefeicaoItemEntity(
            dtConsumo = date,
            cdAlimento = alimentoId,
            cdCliente = clienteId,
            cdFase = 1,
            cdRefeicaoTp = refeicaoTipoId,
            nmQnt = newQuantity
        )
        mealDao.insertMealItems(listOf(item))
    }

    suspend fun updateFoodInMeal(date: String, clienteId: Long, refeicaoTipoId: Int, alimentoId: Long, quantity: Double) = withContext(Dispatchers.IO) {
        val item = RefeicaoItemEntity(
            dtConsumo = date,
            cdAlimento = alimentoId,
            cdCliente = clienteId,
            cdFase = 1,
            cdRefeicaoTp = refeicaoTipoId,
            nmQnt = quantity
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
