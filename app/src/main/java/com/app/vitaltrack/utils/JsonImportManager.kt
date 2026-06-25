package com.app.vitaltrack.utils

import com.app.vitaltrack.data.entity.*
import com.app.vitaltrack.repository.ImportRepository
import org.json.JSONObject
import java.util.Date

class JsonImportManager(private val repository: ImportRepository) {

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
                
                val dtNascimento: Date? = if (obj.isNull("DT_NASCIMENTO")) {
                    null
                } else {
                    val rawValue = obj.get("DT_NASCIMENTO")
                    if (rawValue is Long) {
                        Date(rawValue)
                    } else {
                        JsonDateUtils.jsonDateToDate(obj.optString("DT_NASCIMENTO"))
                    }
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

        // 5. Processar tb_DT_exercicios_tipos
        if (root.has("tb_DT_exercicios_tipos")) {
            val array = root.getJSONArray("tb_DT_exercicios_tipos")
            val entities = mutableListOf<ExercicioTipoEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                entities.add(ExercicioTipoEntity(
                    cdTpExercicio = obj.getInt("CD_TP_EXERCICIO"),
                    dsTpExercicio = obj.optString("DS_TP_EXERCICIO"),
                    blTpExercicio = if (obj.isNull("BL_TP_EXERCICIO")) null else obj.getBoolean("BL_TP_EXERCICIO")
                ))
            }
            repository.importExercicioTipos(entities)
        }

        // 6. Processar tb_DT_pesagens
        if (root.has("tb_DT_pesagens")) {
            val array = root.getJSONArray("tb_DT_pesagens")
            val entities = mutableListOf<PesagemEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                entities.add(PesagemEntity(
                    cdCliente = obj.getLong("CD_CLIENTE"),
                    cdFase = obj.getInt("CD_FASE"),
                    dtPesagem = JsonDateUtils.jsonDateToDate(obj.getString("DT_PESAGEM")) ?: Date(),
                    nmPeso = if (obj.isNull("NM_PESO")) null else obj.getDouble("NM_PESO"),
                    nmPercentGord = if (obj.isNull("NM_PERCENT_GORD")) null else obj.getDouble("NM_PERCENT_GORD"),
                    hrPesagem = JsonDateUtils.jsonDateTimeToDate(obj.getString("HR_PESAGEM")) ?: Date()
                ))
            }
            repository.importPesagens(entities)
        }

        // 7. Processar tb_DT_exercicios
        if (root.has("tb_DT_exercicios")) {
            val array = root.getJSONArray("tb_DT_exercicios")
            val entities = mutableListOf<ExercicioEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                entities.add(ExercicioEntity(
                    cdExercicio = obj.getLong("CD_EXERCICIO"),
                    nmCal = if (obj.isNull("NM_CAL")) null else obj.getDouble("NM_CAL"),
                    cdPeriodo = if (obj.isNull("CD_PERIODO")) null else obj.getInt("CD_PERIODO"),
                    cdFase = if (obj.isNull("CD_FASE")) null else obj.getInt("CD_FASE"),
                    cdCliente = obj.getLong("CD_CLIENTE"),
                    cdTpExercicio = if (obj.isNull("CD_TP_EXERCICIO")) null else obj.getInt("CD_TP_EXERCICIO"),
                    dtDia = JsonDateUtils.jsonDateToDate(obj.optString("DT_DIA"))
                ))
            }
            repository.importExercicios(entities)
        }

        // 8. Processar tb_DT_refeicoes_itens
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

                val rawDtConsumo = obj.getString("DT_CONSUMO")
                val dtConsumo = if (rawDtConsumo.contains(" ")) {
                    rawDtConsumo.substringBefore(" ")
                } else if (rawDtConsumo.contains("T")) {
                    rawDtConsumo.substringBefore("T")
                } else {
                    rawDtConsumo
                }

                itens.add(RefeicaoItemEntity(
                    dtConsumo = dtConsumo,
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
