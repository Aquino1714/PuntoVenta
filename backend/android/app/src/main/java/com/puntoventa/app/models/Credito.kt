package com.puntoventa.app.models

data class Credito(
    val id: Int,
    val ventaId: Int,
    val clienteId: Int,
    val montoTotal: Double,
    val plazo: String,
    val fechaInicio: String,
    val fechaVencimiento: String,
    val estado: String = "vigente",
    val nombreCliente: String? = null,
    val montoAbonado: Double = 0.0
) {

    val saldoPendiente: Double
        get() = maxOf(montoTotal - montoAbonado, 0.0)
}