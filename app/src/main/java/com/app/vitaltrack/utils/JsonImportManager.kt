package com.app.vitaltrack.utils

import com.app.vitaltrack.data.entity.AlimentoEntity
import com.app.vitaltrack.data.entity.RefeicaoItemEntity
import com.app.vitaltrack.data.entity.RefeicaoTipoEntity
import com.app.vitaltrack.data.entity.UnidadeEntity
import com.app.vitaltrack.repository.ImportRepository
import org.json.JSONObject

class JsonImportManager(private val repository: ImportRepository) {

    suspend fun importFromJson(jsonString: String): String {
        val root = JSONObject(jsonString)
        
        // Processar tb_DT_unidades
        if (root.has("tb_DT_unidades")) {
            val unidadesArray = root.getJSONArray("tb_DT_unidades")
            val unidades = mutableListOf<UnidadeEntity>()
            for (i in 0 until unidadesArray.length()) {
                val obj = unidadesArray.getJSONObject(i)
                unidades.add(UnidadeEntity(
                    cdUnidade = obj.getLong("CD_UNIDADE"),
                    dsUnidade = obj.optString("DS_UNIDADE")
                ))
            }
            repository.importUnidades(unidades)
        }

        // Processar tb_DT_refeicoes_tipos
        if (root.has("tb_DT_refeicoes_tipos")) {
            val tiposArray = root.getJSONArray("tb_DT_refeicoes_tipos")
            val tipos = mutableListOf<RefeicaoTipoEntity>()
            for (i in 0 until tiposArray.length()) {
                val obj = tiposArray.getJSONObject(i)
                tipos.add(RefeicaoTipoEntity(
                    cdRefeicaoTp = obj.getLong("CD_REFEICAO_TP"),
                    dsRefeicaoTp = obj.optString("DS_REFEICAO_TP")
                ))
            }
            repository.importRefeicoesTipos(tipos)
        }

        // Processar tb_DT_alimentos
        if (root.has("tb_DT_alimentos")) {
            val alimentosArray = root.getJSONArray("tb_DT_alimentos")
            val alimentos = mutableListOf<AlimentoEntity>()
            for (i in 0 until alimentosArray.length()) {
                val obj = alimentosArray.getJSONObject(i)
                alimentos.add(AlimentoEntity(
                    cdAlimento = obj.getLong("CD_ALIMENTO"),
                    dsAlimento = obj.optString("DS_ALIMENTO"),
                    cdUnidade = obj.optInt("CD_UNIDADE"),
                    nmQntBase = obj.optLong("NM_QNT_BASE"),
                    nmCal = obj.optDouble("NM_CAL"),
                    nmProt = obj.optDouble("NM_PROT"),
                    nmCarb = obj.optDouble("NM_CARB"),
                    nmGord = obj.optDouble("NM_GORD")
                ))
            }
            repository.importAlimentos(alimentos)
        }

        // Processar tb_DT_refeicoes_itens
        if (root.has("tb_DT_refeicoes_itens")) {
            val itensArray = root.getJSONArray("tb_DT_refeicoes_itens")
            val itens = mutableListOf<RefeicaoItemEntity>()
            for (i in 0 until itensArray.length()) {
                val obj = itensArray.getJSONObject(i)
                itens.add(RefeicaoItemEntity(
                    dtConsumo = obj.getString("DT_CONSUMO"),
                    cdAlimento = obj.getLong("CD_ALIMENTO"),
                    cdCliente = obj.getLong("CD_CLIENTE"),
                    cdFase = obj.optInt("CD_FASE"),
                    cdRefeicaoTp = obj.getInt("CD_REFEICAO_TP"),
                    nmQnt = obj.optDouble("NM_QNT")
                ))
            }
            repository.importRefeicoesItens(itens)
        }

        return "Importação concluída com sucesso."
    }
}
