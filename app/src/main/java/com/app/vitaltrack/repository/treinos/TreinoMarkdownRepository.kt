package com.app.vitaltrack.repository.treinos

import com.app.vitaltrack.data.markdown.MarkdownAssetReader
import com.app.vitaltrack.data.markdown.TreinoMarkdownImportado
import com.app.vitaltrack.data.markdown.MarkdownTreinoParseResult
import com.app.vitaltrack.data.markdown.MarkdownTreinoParser
import kotlinx.coroutines.Dispatchers
import android.net.Uri
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class TreinoMarkdownRepository(
    private val assetReader: MarkdownAssetReader,
    private val parser: MarkdownTreinoParser = MarkdownTreinoParser()
) {
    private val _importedResult = MutableStateFlow<TreinoMarkdownImportado?>(null)
    val importedResult: StateFlow<TreinoMarkdownImportado?> = _importedResult.asStateFlow()

    suspend fun carregarTreinosDoMarkdown(): MarkdownTreinoParseResult = withContext(Dispatchers.IO) {
        val content = assetReader.readTreinamentoFile()
        if (content == null) {
            MarkdownTreinoParseResult.Error("Não foi possível encontrar o arquivo de treinamento.")
        } else {
            val result = parser.parse(content)
            if (result is MarkdownTreinoParseResult.Success) {
                _importedResult.value = result.resultado
            }
            result
        }
    }

    suspend fun carregarTreinosDeUri(context: Context, uri: Uri): MarkdownTreinoParseResult = withContext(Dispatchers.IO) {
        try {
            // Log para debug (aparecerá no logcat se necessário)
            println("VitalTrack: Tentando ler URI: $uri")
            
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (content == null || content.isBlank()) {
                MarkdownTreinoParseResult.Error("O arquivo está vazio ou não pôde ser lido.")
            } else {
                val result = parser.parse(content)
                if (result is MarkdownTreinoParseResult.Success) {
                    _importedResult.value = result.resultado
                }
                result
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            MarkdownTreinoParseResult.Error("Erro de Permissão: O sistema impediu o acesso ao arquivo. Tente mover o arquivo para a pasta principal de Downloads.")
        } catch (e: Exception) {
            e.printStackTrace()
            MarkdownTreinoParseResult.Error("Erro ao ler o arquivo: ${e.message}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TreinoMarkdownRepository? = null

        fun getInstance(context: Context): TreinoMarkdownRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = TreinoMarkdownRepository(MarkdownAssetReader(context))
                INSTANCE = instance
                instance
            }
        }
    }
}
