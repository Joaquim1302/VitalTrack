package com.app.vitaltrack.repository.treinos

import com.app.vitaltrack.data.markdown.MarkdownAssetReader
import com.app.vitaltrack.data.markdown.MarkdownTreino
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
    private val _importedTreinos = MutableStateFlow<List<MarkdownTreino>>(emptyList())
    val importedTreinos: StateFlow<List<MarkdownTreino>> = _importedTreinos.asStateFlow()

    private val _activeMarkdownTreino = MutableStateFlow<MarkdownTreino?>(null)
    val activeMarkdownTreino: StateFlow<MarkdownTreino?> = _activeMarkdownTreino.asStateFlow()

    suspend fun carregarTreinosDoMarkdown(): MarkdownTreinoParseResult = withContext(Dispatchers.IO) {
        val content = assetReader.readTreinamentoFile()
        if (content == null) {
            MarkdownTreinoParseResult.Error("Não foi possível encontrar o arquivo de treinamento.")
        } else {
            val result = parser.parse(content)
            if (result is MarkdownTreinoParseResult.Success) {
                _importedTreinos.value = result.treinos
            }
            result
        }
    }

    suspend fun carregarTreinosDeUri(context: Context, uri: Uri): MarkdownTreinoParseResult = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (content == null) {
                MarkdownTreinoParseResult.Error("Não foi possível ler o conteúdo do arquivo.")
            } else {
                val result = parser.parse(content)
                if (result is MarkdownTreinoParseResult.Success) {
                    _importedTreinos.value = result.treinos
                }
                result
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MarkdownTreinoParseResult.Error("Erro ao ler o arquivo: ${e.message}")
        }
    }

    fun getTreinoByNome(nome: String): MarkdownTreino? {
        return _importedTreinos.value.find { it.nome == nome }
    }

    fun setActiveTreino(treino: MarkdownTreino) {
        _activeMarkdownTreino.value = treino
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
