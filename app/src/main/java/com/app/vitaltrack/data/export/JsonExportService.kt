package com.app.vitaltrack.data.export

import android.content.Context
import com.app.vitaltrack.data.dao.MealDao
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JsonExportService(
    private val context: Context,
    private val mealDao: MealDao
) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    suspend fun generateExportJson(): String = withContext(Dispatchers.IO) {
        val items = mealDao.exportarRefeicoes()
        val exportData = ExportRefeicoesDto(tb_DT_refeicoes_itens = items)
        json.encodeToString(exportData)
    }
}
