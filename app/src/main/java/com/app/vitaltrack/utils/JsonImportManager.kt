package com.app.vitaltrack.utils

import android.util.Log
import com.app.vitaltrack.data.entity.*
import com.app.vitaltrack.repository.ImportRepository
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JsonImportManager(private val repository: ImportRepository) {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    suspend fun importFromJson(jsonString: String): String {
        val root = JSONObject(jsonString)
        
        // 1. Processar tb_DT_clientes
        val importedClientIds = mutableSetOf<Long>()
        if (root.has("tb_DT_clientes")) {
            val clientesArray = root.getJSONArray("tb_DT_clientes")
            val clientes = mutableListOf<ClienteEntity>()
            for (i in 0 until clientesArray.length()) {
                val obj = clientesArray.getJSONObject(i)
                val id = obj.getLong("CD_CLIENTE")
                importedClientIds.add(id)
                
                val dtNascimento: Date? = when {
                    obj.isNull("DT_NASCIMENTO") -> null
                    obj.get("DT_NASCIMENTO") is Long -> Date(obj.getLong("DT_NASCIMENTO"))
                    obj.get("DT_NASCIMENTO") is String -> {
                        val dateStr = obj.getString("DT_NASCIMENTO")
                        try {
                            if (dateStr.contains("T")) isoFormat.parse(dateStr)
                            else SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
                        } catch (e: Exception) {
                            Log.e("JsonImportManager", "Erro ao converter data: $dateStr", e)
                            null
                        }
                    }
                    else -> null
                }

                clientes.add(ClienteEntity(
                    cdCliente = id,
                    dsNome = obj.getString("DS_NOME"),
                    cdSexo = if (obj.isNull("CD_SEXO")) null else obj.getString("CD_SEXO"),
                    dtNascimento = dtNascimento,
                    nmAltura = if (obj.isNull("NM_ALTURA")) null else obj.getDouble("NM_ALTURA")
                ))
            }
            repository.importClientes(clientes)
        }

        // 2. Processar tb_DT_unidades
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

        // 3. Processar tb_DT_refeicoes_tipos
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

        // 4. Processar tb_DT_alimentos
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

        // 5. Processar tb_DT_refeicoes_itens
        if (root.has("tb_DT_refeicoes_itens")) {
            val itensArray = root.getJSONArray("tb_DT_refeicoes_itens")
            val itens = mutableListOf<RefeicaoItemEntity>()
            val missingClientIds = mutableSetOf<Long>()
            
            for (i in 0 until itensArray.length()) {
                val obj = itensArray.getJSONObject(i)
                val clientId = obj.getLong("CD_CLIENTE")
                
                if (!importedClientIds.contains(clientId) && clientId != 1L) {
                    missingClientIds.add(clientId)
                }

                itens.add(RefeicaoItemEntity(
                    dtConsumo = obj.getString("DT_CONSUMO"),
                    cdAlimento = obj.getLong("CD_ALIMENTO"),
                    cdCliente = clientId,
                    cdFase = if (obj.isNull("CD_FASE")) null else obj.getInt("CD_FASE"),
                    cdRefeicaoTp = obj.getInt("CD_REFEICAO_TP"),
                    nmQnt = if (obj.isNull("NM_QNT")) null else obj.getDouble("NM_QNT"),
                    dsUnidade = obj.optString("DS_UNIDADE", "g"),
                    cdRefeicaoItemApp = obj.optInt("CD_REFEICAO_ITEM_APP", 0)
                ))
            }
            
            if (missingClientIds.isNotEmpty()) {
                val dummyClients = missingClientIds.map { 
                    ClienteEntity(cdCliente = it, dsNome = "Usuário $it")
                }
                repository.importClientes(dummyClients)
            }
            
            repository.importClientes(listOf(ClienteEntity(cdCliente = 1L, dsNome = "Joaquim")))

            repository.importRefeicoesItens(itens)
        }

        return "Importação concluída com sucesso."
    }
}
