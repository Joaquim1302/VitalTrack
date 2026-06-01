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
        val clientes = mealDao.getAllClientes().map { 
            ExportClienteDto(
                CD_CLIENTE = it.cdCliente,
                DS_NOME = it.dsNome,
                CD_SEXO = it.cdSexo,
                DT_NASCIMENTO = it.dtNascimento?.time,
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

        val exportData = ExportRefeicoesDto(
            tb_DT_refeicoes_itens = items,
            tb_DT_clientes = clientes,
            tb_DT_alimentos = alimentos,
            tb_DT_refeicoes_tipos = tipos,
            tb_DT_unidades = unidades
        )
        json.encodeToString(exportData)
    }
}
