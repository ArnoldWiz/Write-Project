package com.chear.planit.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // --- Attachment List ---
    @TypeConverter
    fun fromAttachmentList(list: List<Attachment>?): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toAttachmentList(data: String?): List<Attachment> {
        if (data.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<Attachment>>() {}.type
        return gson.fromJson(data, type)
    }

    // --- Long List (para las fechas adicionales) ---
    @TypeConverter
    fun fromLongList(list: List<Long>?): String {
        return list?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toLongList(data: String?): List<Long> {
        return if (data.isNullOrEmpty()) {
            emptyList()
        } else {
            data.split(",").mapNotNull { it.toLongOrNull() }
        }
    }
    
    // --- LEGACY: String List (para compatibilidad si se requiere, o se puede remover si migramos todo) ---
    // Dejamos estos por si acaso la migración falla en algún punto, pero idealmente ya no se usarán para attachmentUris
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString(separator = "||") ?: ""
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        // Detectamos si es un JSON (empieza con '[') o el formato antiguo
        if (data.isNullOrEmpty()) return emptyList()
        if (data.trim().startsWith("[")) {
             // Es JSON, pero estamos pidiendo List<String>, esto puede ser tricky.
             // Si la entidad espera List<String>, Room usará este converter.
             // Pero hemos cambiado la entidad a List<Attachment>.
             // Así que este converter es solo para columnas viejas que sean List<String>.
             return emptyList() 
        }
        return data.split("||")
    }
}
