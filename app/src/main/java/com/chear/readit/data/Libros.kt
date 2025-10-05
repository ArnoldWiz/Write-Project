package com.chear.readit.data

data class Libro(
    val titulo: String,
    val autor: String,
    val estado: String, // "Por leer" = 0, "En proceso" = 1, "Terminado" = 2
)