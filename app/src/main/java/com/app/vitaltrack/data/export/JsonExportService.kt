package com.app.vitaltrack.data.export

import android.content.Context
import com.app.vitaltrack.data.dao.MealDao
import kotlinx.serialization.json.Json

class JsonExportService(
    private val context: Context,
    private val mealDao: MealDao
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
}
