package com.puntoventa.app.models

data class PagoCredito(
    val id: Int,
    val creditoId: Int,
    val monto: Double,
    val metodoPago: String,
    val referencia: String?,
    val fecha: String
)