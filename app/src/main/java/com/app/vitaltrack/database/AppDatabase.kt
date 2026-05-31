package com.app.vitaltrack.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.vitaltrack.data.dao.ImportDao
import com.app.vitaltrack.data.dao.MealDao
import com.app.vitaltrack.data.dao.RefeicaoSalvaDao
import com.app.vitaltrack.data.entity.*

@Database(
    entities = [
        AlimentoEntity::class,
        RefeicaoTipoEntity::class,
        RefeicaoItemEntity::class,
        UnidadeEntity::class,
        RefeicaoFavoritaEntity::class,
        RefeicaoFavoritaItemEntity::class,
        RefeicaoSalvaEntity::class,
        RefeicaoSalvaItemEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun importDao(): ImportDao
    abstract fun mealDao(): MealDao
    abstract fun refeicaoSalvaDao(): RefeicaoSalvaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vitaltrack_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
