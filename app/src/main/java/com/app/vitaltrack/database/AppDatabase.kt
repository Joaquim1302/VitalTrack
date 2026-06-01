package com.app.vitaltrack.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.vitaltrack.data.dao.ClienteDao
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
        RefeicaoSalvaItemEntity::class,
        ClienteEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun importDao(): ImportDao
    abstract fun mealDao(): MealDao
    abstract fun refeicaoSalvaDao(): RefeicaoSalvaDao
    abstract fun clienteDao(): ClienteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Criar tb_DT_clientes
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_DT_clientes (
                        CD_CLIENTE INTEGER NOT NULL, 
                        DS_NOME TEXT NOT NULL, 
                        CD_SEXO TEXT, 
                        DT_NASCIMENTO INTEGER, 
                        NM_ALTURA REAL, 
                        PRIMARY KEY(CD_CLIENTE)
                    )
                """)

                // Inserir cliente padrão conforme requisito 8
                db.execSQL("INSERT OR IGNORE INTO tb_DT_clientes (CD_CLIENTE, DS_NOME) VALUES (1, 'Joaquim')")

                // 2. Recriar tb_DT_refeicoes_itens para adicionar Foreign Key
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_DT_refeicoes_itens_new (
                        DT_CONSUMO TEXT NOT NULL, 
                        CD_ALIMENTO INTEGER NOT NULL, 
                        CD_CLIENTE INTEGER NOT NULL, 
                        CD_FASE INTEGER, 
                        CD_REFEICAO_TP INTEGER NOT NULL, 
                        NM_QNT REAL, 
                        DS_UNIDADE TEXT, 
                        PRIMARY KEY(DT_CONSUMO, CD_ALIMENTO, CD_CLIENTE, CD_REFEICAO_TP),
                        FOREIGN KEY(CD_CLIENTE) REFERENCES tb_DT_clientes(CD_CLIENTE) ON UPDATE CASCADE ON DELETE RESTRICT
                    )
                """)
                
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tb_DT_refeicoes_itens_CD_CLIENTE ON tb_DT_refeicoes_itens_new (CD_CLIENTE)")

                db.execSQL("""
                    INSERT INTO tb_DT_refeicoes_itens_new (DT_CONSUMO, CD_ALIMENTO, CD_CLIENTE, CD_FASE, CD_REFEICAO_TP, NM_QNT, DS_UNIDADE)
                    SELECT DT_CONSUMO, CD_ALIMENTO, CD_CLIENTE, CD_FASE, CD_REFEICAO_TP, NM_QNT, DS_UNIDADE FROM tb_DT_refeicoes_itens
                """)

                db.execSQL("DROP TABLE tb_DT_refeicoes_itens")
                db.execSQL("ALTER TABLE tb_DT_refeicoes_itens_new RENAME TO tb_DT_refeicoes_itens")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vitaltrack_database"
                )
                    .addMigrations(MIGRATION_4_5)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL("INSERT OR IGNORE INTO tb_DT_clientes (CD_CLIENTE, DS_NOME) VALUES (1, 'Joaquim')")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
