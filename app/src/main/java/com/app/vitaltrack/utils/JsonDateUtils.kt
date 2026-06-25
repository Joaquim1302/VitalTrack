package com.app.vitaltrack.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilitário centralizado para conversão de datas nos arquivos JSON de importação e exportação.
 * Padroniza o uso de ISO (yyyy-MM-dd para datas e yyyy-MM-dd'T'HH:mm:ss para data/hora).
 */
object JsonDateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    // --- Conversões para Date? ---

    fun dateToJsonDate(date: Date?): String? {
        return date?.let { dateFormat.format(it) }
    }

    fun jsonDateToDate(value: String?): Date? {
        if (value.isNullOrBlank()) return null
        return try {
            dateFormat.parse(value)
        } catch (e: Exception) {
            Log.e("JsonDateUtils", "Erro ao parsear Data (yyyy-MM-dd): $value")
            null
        }
    }

    fun dateTimeToJsonDateTime(date: Date?): String? {
        return date?.let { dateTimeFormat.format(it) }
    }

    fun jsonDateTimeToDate(value: String?): Date? {
        if (value.isNullOrBlank()) return null
        return try {
            // Tenta formato completo primeiro
            if (value.contains("T")) {
                dateTimeFormat.parse(value)
            } else {
                dateFormat.parse(value)
            }
        } catch (e: Exception) {
            Log.e("JsonDateUtils", "Erro ao parsear Data/Hora: $value", e)
            null
        }
    }

    // --- Conversões para Long? (Timestamp) ---

    fun timestampToJsonDate(timestamp: Long?): String? {
        return timestamp?.let { dateToJsonDate(Date(it)) }
    }

    fun jsonDateToTimestamp(value: String?): Long? {
        return jsonDateToDate(value)?.time
    }

    fun timestampToJsonDateTime(timestamp: Long?): String? {
        return timestamp?.let { dateTimeToJsonDateTime(Date(it)) }
    }

    fun jsonDateTimeToTimestamp(value: String?): Long? {
        return jsonDateTimeToDate(value)?.time
    }
}
