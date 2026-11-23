package com.puntoventa.app.models

data class Cuenta(
    val id: Int,
    val negocioId: Int,
    val nombre: String,
    val tipo: String,
    val saldoActual: Double
)