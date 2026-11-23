package com.puntoventa.app.models

data class MovimientoFinanciero(
    val id: Int = 0,
    val negocioId: Int,
    val cuentaId: Int?,
    val categoriaGastoId: Int?,
    val tipo: String,
    val subtipo: String?,
    val concepto: String?,
    val monto: Double,
    val metodoPago: String?,
    val fecha: String
)