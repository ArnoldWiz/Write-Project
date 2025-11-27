package com.chear.planit.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString(separator = "||") ?: ""
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        return if (data.isNullOrEmpty()) {
            emptyList()
        } else {
            data.split("||")
        }
    }

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
}
