package com.chear.planit.data

import androidx.room.TypeConverter

class Converters {
    // Convierte una lista de Strings a un solo String separado por "||"
    @TypeConverter
    fun fromList(list: List<String>?): String {
        return list?.joinToString(separator = "||") ?: ""
    }

    // Convierte un String separado por "||" de vuelta a una lista de Strings
    @TypeConverter
    fun toList(data: String?): List<String> {
        return if (data.isNullOrEmpty()) {
            emptyList()
        } else {
            data.split("||")
        }
    }
}
