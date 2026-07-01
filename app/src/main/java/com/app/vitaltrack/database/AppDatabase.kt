package com.app.vitaltrack.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.vitaltrack.data.dao.*
import com.app.vitaltrack.data.dao.treinos.TreinoAcademiaDao
import com.app.vitaltrack.data.entity.*
import com.app.vitaltrack.data.entity.treinos.*

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
        ClienteEntity::class,
        ExercicioEntity::class,
        ExercicioTipoEntity::class,
        PesagemEntity::class,
        TreinoFichaEntity::class,
        TreinoFichaDiaEntity::class,
        TreinoFichaExercicioEntity::class,
        TreinoSessaoEntity::class,
        TreinoSerieEntity::class,
        TreinoImportacaoEntity::class
    ],
    version = 8,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun importDao(): ImportDao
    abstract fun mealDao(): MealDao
    abstract fun refeicaoSalvaDao(): RefeicaoSalvaDao
    abstract fun clienteDao(): ClienteDao
    abstract fun exercicioDao(): ExercicioDao
    abstract fun exercicioTipoDao(): ExercicioTipoDao
    abstract fun pesagemDao(): PesagemDao
    abstract fun treinoAcademiaDao(): TreinoAcademiaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_DT_treinos_fichas (
                        CD_FICHA INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        CD_CLIENTE INTEGER NOT NULL, 
                        DS_FICHA TEXT NOT NULL, 
                        DT_INICIO INTEGER NOT NULL, 
                        DT_FIM INTEGER, 
                        ST_ATIVA INTEGER NOT NULL, 
                        DS_OBS TEXT
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_DT_treinos_fichas_dias (
                        CD_FICHA_DIA INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        CD_FICHA INTEGER NOT NULL, 
                        DS_DIA TEXT NOT NULL, 
                        NR_ORDEM INTEGER NOT NULL, 
                        DS_GRUPO_MUSCULAR TEXT
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_DT_treinos_fichas_exercicios (
                        CD_FICHA_EXERCICIO INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        CD_FICHA_DIA INTEGER NOT NULL, 
                        CD_EXERCICIO INTEGER NOT NULL, 
                        NR_ORDEM INTEGER NOT NULL, 
                        NR_SERIES_PLANEJADAS INTEGER NOT NULL, 
                        NR_REPETICOES_PLANEJADAS INTEGER NOT NULL, 
                        NM_CARGA_RECOMENDADA REAL, 
                        NR_DESCANSO_SEGUNDOS INTEGER, 
                        DS_OBS TEXT
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_DT_treinos_sessoes (
                        CD_TREINO_SESSAO INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        CD_CLIENTE INTEGER NOT NULL, 
                        CD_FICHA_DIA INTEGER NOT NULL, 
                        DT_INICIO INTEGER NOT NULL, 
                        DT_FIM INTEGER, 
                        ST_STATUS TEXT NOT NULL, 
                        DS_ORIGEM TEXT NOT NULL, 
                        DS_OBS TEXT
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_DT_treinos_series (
                        CD_SERIE INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        CD_TREINO_SESSAO INTEGER NOT NULL, 
                        CD_FICHA_EXERCICIO INTEGER NOT NULL, 
                        NR_SERIE INTEGER NOT NULL, 
                        NM_CARGA REAL, 
                        NR_REPETICOES INTEGER, 
                        ST_CONCLUIDA INTEGER NOT NULL, 
                        NM_ESFORCO REAL, 
                        DS_OBS TEXT
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_DT_treinos_importacoes (
                        CD_IMPORTACAO INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        CD_CLIENTE INTEGER NOT NULL, 
                        DT_IMPORTACAO INTEGER NOT NULL, 
                        DS_ORIGEM TEXT NOT NULL, 
                        DS_TEXTO_EXTRAIDO TEXT, 
                        ST_STATUS TEXT NOT NULL, 
                        DS_OBS TEXT
                    )
                """)
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_DT_exercicios (
                        CD_EXERCICIO INTEGER NOT NULL, 
                        NM_CAL REAL, 
                        CD_PERIODO INTEGER, 
                        CD_FASE INTEGER, 
                        CD_CLIENTE INTEGER NOT NULL, 
                        CD_TP_EXERCICIO INTEGER, 
                        DT_DIA INTEGER, 
                        PRIMARY KEY(CD_EXERCICIO)
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_DT_exercicios_tipos (
                        CD_TP_EXERCICIO INTEGER NOT NULL, 
                        DS_TP_EXERCICIO TEXT, 
                        BL_TP_EXERCICIO INTEGER, 
                        PRIMARY KEY(CD_TP_EXERCICIO)
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tb_DT_pesagens (
                        CD_CLIENTE INTEGER NOT NULL, 
                        CD_FASE INTEGER NOT NULL, 
                        DT_PESAGEM INTEGER NOT NULL, 
                        NM_PESO REAL, 
                        NM_PERCENT_GORD REAL, 
                        HR_PESAGEM INTEGER NOT NULL, 
                        PRIMARY KEY(CD_CLIENTE, CD_FASE, DT_PESAGEM, HR_PESAGEM)
                    )
                """)
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tb_DT_refeicoes_itens ADD COLUMN CD_REFEICAO_ITEM_APP INTEGER DEFAULT 0")
            }
        }

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
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL("INSERT OR IGNORE INTO tb_DT_clientes (CD_CLIENTE, DS_NOME) VALUES (1, 'Joaquim')")
                            
                            // Inserir tipos iniciais de exercicio
                            db.execSQL("INSERT OR IGNORE INTO tb_DT_exercicios_tipos (CD_TP_EXERCICIO, DS_TP_EXERCICIO, BL_TP_EXERCICIO) VALUES (1, 'Caminhada', 1)")
                            db.execSQL("INSERT OR IGNORE INTO tb_DT_exercicios_tipos (CD_TP_EXERCICIO, DS_TP_EXERCICIO, BL_TP_EXERCICIO) VALUES (2, 'Corrida', 1)")
                            db.execSQL("INSERT OR IGNORE INTO tb_DT_exercicios_tipos (CD_TP_EXERCICIO, DS_TP_EXERCICIO, BL_TP_EXERCICIO) VALUES (3, 'Ciclismo', 1)")
                            db.execSQL("INSERT OR IGNORE INTO tb_DT_exercicios_tipos (CD_TP_EXERCICIO, DS_TP_EXERCICIO, BL_TP_EXERCICIO) VALUES (4, 'Musculacao', 1)")
                            db.execSQL("INSERT OR IGNORE INTO tb_DT_exercicios_tipos (CD_TP_EXERCICIO, DS_TP_EXERCICIO, BL_TP_EXERCICIO) VALUES (5, 'Natacao', 1)")
                            db.execSQL("INSERT OR IGNORE INTO tb_DT_exercicios_tipos (CD_TP_EXERCICIO, DS_TP_EXERCICIO, BL_TP_EXERCICIO) VALUES (6, 'Outro', 1)")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
