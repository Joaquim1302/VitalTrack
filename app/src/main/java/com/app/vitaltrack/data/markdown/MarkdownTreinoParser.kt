package com.app.vitaltrack.data.markdown

class MarkdownTreinoParser {

    companion object {
        private const val DEFAULT_ERROR_MESSAGE = "Não foi possível ler o plano de treino.\nVerifique se o arquivo Markdown possui as colunas Exercício, Séries, Repetições, Carga e Intervalo."
    }

    fun parse(content: String): MarkdownTreinoParseResult {
        if (content.isBlank()) return MarkdownTreinoParseResult.Error(DEFAULT_ERROR_MESSAGE)

        val treinos = mutableListOf<TreinoDiaMarkdownImportado>()
        val lines = content.lines()

        var currentTreinoNome = ""
        var currentGrupoMuscular: String? = null
        var currentExercicios = mutableListOf<TreinoExercicioMarkdownImportado>()
        var foundTitle = false

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()

            if (line.startsWith("#")) {
                // Se já tínhamos um treino sendo processado, salva ele
                if (foundTitle && currentExercicios.isNotEmpty()) {
                    treinos.add(TreinoDiaMarkdownImportado(currentTreinoNome, currentGrupoMuscular, currentExercicios.toList()))
                    currentExercicios = mutableListOf()
                }

                // Processa novo título
                val titleContent = line.removePrefix("#").trim().removePrefix("#").trim() // Suporta # ou ##
                if (titleContent.contains("—")) {
                    val parts = titleContent.split("—", limit = 2)
                    currentTreinoNome = parts[0].trim()
                    currentGrupoMuscular = parts[1].trim()
                } else if (titleContent.contains("-")) {
                    val parts = titleContent.split("-", limit = 2)
                    currentTreinoNome = parts[0].trim()
                    currentGrupoMuscular = parts[1].trim()
                } else {
                    currentTreinoNome = titleContent
                    currentGrupoMuscular = null
                }
                foundTitle = true
                i++
                continue
            }

            if (line.startsWith("|") && line.contains("Exercício", ignoreCase = true)) {
                // Cabeçalho da tabela encontrado
                // Pula o cabeçalho e a linha separadora
                i += 2
                var ordem = 1
                while (i < lines.size && lines[i].trim().startsWith("|")) {
                    val rowLine = lines[i].trim()
                    if (rowLine.contains("---")) { // Pula linha separadora se o i+2 não foi suficiente
                        i++
                        continue
                    }
                    val cells = rowLine.split("|")
                        .map { it.trim() }
                        .filterIndexed { index, _ -> index > 0 } // Remove o primeiro elemento vazio por causa do split em "|..."

                    if (cells.size >= 5) {
                        val nome = cells[0].replace("**", "").trim()
                        if (nome.isNotEmpty()) {
                            val series = cells[1].toIntOrNull()
                            val repeticoes = cells[2]
                            val cargaRaw = cells[3]
                            val carga = if (cargaRaw == "—" || cargaRaw == "-" || cargaRaw.isEmpty()) null else cargaRaw
                            val intervalo = cells[4].toIntOrNull() ?: 60

                            currentExercicios.add(
                                TreinoExercicioMarkdownImportado(
                                    ordem = ordem++,
                                    nome = nome,
                                    series = series,
                                    repeticoes = repeticoes,
                                    carga = carga,
                                    intervaloSegundos = intervalo
                                )
                            )
                        }
                    }
                    i++
                }
                continue
            }

            i++
        }

        // Adiciona o último treino processado
        if (foundTitle && currentExercicios.isNotEmpty()) {
            treinos.add(TreinoDiaMarkdownImportado(currentTreinoNome, currentGrupoMuscular, currentExercicios.toList()))
        }

        return if (treinos.isEmpty()) {
            MarkdownTreinoParseResult.Error(DEFAULT_ERROR_MESSAGE)
        } else {
            val fichaImportada = TreinoMarkdownImportado(
                dsFicha = "Plano de Treino Importado",
                dias = treinos
            )
            MarkdownTreinoParseResult.Success(fichaImportada)
        }
    }
}
