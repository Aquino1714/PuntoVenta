package com.puntoventa.app.models

data class VentaDetalle(
    val id: Int,
    val ventaId: Int,
    val productoId: Int,
    val varianteId: Int?,      // solo si el producto tiene variantes (talla, color, etc.)
    val cantidad: Int,
    val precioUnitario: Double, // precio al momento de la venta
    val subtotal: Double        // cantidad * precioUnitario
)