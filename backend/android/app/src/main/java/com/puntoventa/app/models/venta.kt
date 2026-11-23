package com.puntoventa.app.models

data class Venta(
    val id: Int,
    val negocioId: Int,
    val clienteId: Int?,   // null cuando es venta a público general
    val fecha: String,
    val total: Double,
    val metodoPago: String,
    val montoPagado: Double
) {

    // Lo que todavía falta por cobrar. Si pagaron de más, no se muestra negativo
    val saldoPendiente: Double
        get() = maxOf(total - montoPagado, 0.0)
}