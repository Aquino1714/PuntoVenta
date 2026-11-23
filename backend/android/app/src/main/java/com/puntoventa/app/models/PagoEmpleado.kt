package com.puntoventa.app.models

data class PagoEmpleado(
    val empleadoId: Int,
    val nombreEmpleado: String,
    val negocioId: Int,
    val cuentaId: Int,
    val monto: Double,
    val periodo: String,
    val fechaPago: String,
    val metodoPago: String = "efectivo"
)