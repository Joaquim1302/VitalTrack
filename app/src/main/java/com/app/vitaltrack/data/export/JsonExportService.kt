package com.app.vitaltrack.data.export

import com.app.vitaltrack.data.dao.MealDao
import com.app.vitaltrack.database.AppDatabase
import com.app.vitaltrack.utils.JsonDateUtils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JsonExportService(
    private val db: AppDatabase
) {
    private val mealDao = db.mealDao()
    private val exercicioDao = db.exercicioDao()
    private val exercicioTipoDao = db.exercicioTipoDao()
    private val pesagemDao = db.pesagemDao()

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    suspend fun generateExportJson(): String = withContext(Dispatchers.IO) {
        val items = mealDao.exportarRefeicoes()
        
        val clientes = mealDao.getAllClientes().map { 
            ExportClienteDto(
                CD_CLIENTE = it.cdCliente,
                DS_NOME = it.dsNome,
                CD_SEXO = it.cdSexo,
                DT_NASCIMENTO = JsonDateUtils.dateToJsonDate(it.dtNascimento),
                NM_ALTURA = it.nmAltura
            )
        }
        
        val alimentos = mealDao.getAllAlimentos().map { 
            ExportAlimentoDto(
                CD_ALIMENTO = it.cdAlimento,
                DS_ALIMENTO = it.dsAlimento,
                CD_UNIDADE = it.cdUnidade,
                NM_QNT_BASE = it.nmQntBase,
                NM_CAL = it.nmCal,
                NM_PROT = it.nmProt,
                NM_CARB = it.nmCarb,
                NM_GORD = it.nmGord
            )
        }
        
        val tipos = mealDao.getAllRefeicaoTipos().map { 
            ExportRefeicaoTipoDto(
                CD_REFEICAO_TP = it.cdRefeicaoTp,
                DS_REFEICAO_TP = it.dsRefeicaoTp
            )
        }
        
        val unidades = mealDao.getAllUnidades().map { 
            ExportUnidadeDto(
                CD_UNIDADE = it.cdUnidade,
                DS_UNIDADE = it.dsUnidade
            )
        }

        val pesagens = pesagemDao.getAllPesagens().map {
            ExportPesagemDto(
                CD_CLIENTE = it.cdCliente,
                CD_FASE = it.cdFase,
                DT_PESAGEM = JsonDateUtils.dateToJsonDate(it.dtPesagem) ?: "",
                NM_PESO = it.nmPeso,
                NM_PERCENT_GORD = it.nmPercentGord,
                HR_PESAGEM = JsonDateUtils.dateTimeToJsonDateTime(it.hrPesagem) ?: ""
            )
        }

        val exercicios = exercicioDao.getAllExercicios().map {
            ExportExercicioDto(
                CD_EXERCICIO = it.cdExercicio,
                NM_CAL = it.nmCal,
                CD_PERIODO = it.cdPeriodo,
                CD_FASE = it.cdFase,
                CD_CLIENTE = it.cdCliente,
                CD_TP_EXERCICIO = it.cdTpExercicio,
                DT_DIA = JsonDateUtils.dateToJsonDate(it.dtDia)
            )
        }

        val exercicioTipos = exercicioTipoDao.getAllTipos().map {
            ExportExercicioTipoDto(
                CD_TP_EXERCICIO = it.cdTpExercicio,
                DS_TP_EXERCICIO = it.dsTpExercicio,
                BL_TP_EXERCICIO = it.blTpExercicio
            )
        }

        val exportData = ExportRefeicoesDto(
            tb_DT_refeicoes_itens = items,
            tb_DT_clientes = clientes,
            tb_DT_alimentos = alimentos,
            tb_DT_refeicoes_tipos = tipos,
            tb_DT_unidades = unidades,
            tb_DT_pesagens = pesagens,
            tb_DT_exercicios = exercicios,
            tb_DT_exercicios_tipos = exercicioTipos
        )
        json.encodeToString(exportData)
    }
}
