package com.app.vitaltrack.utils

import java.text.Normalizer

/**
 * Normaliza uma string para busca, removendo acentos, 
 * convertendo para minúsculas e removendo espaços extras.
 */
fun String.normalizeSearch(): String {
    return Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .lowercase()
        .trim()
}
