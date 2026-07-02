package com.app.vitaltrack.data.markdown

import android.content.Context
import java.io.IOException

class MarkdownAssetReader(private val context: Context) {

    fun readTreinamentoFile(): String? {
        return try {
            context.assets.open("treinamento.md").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
