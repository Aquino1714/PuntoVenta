package com.puntoventa.app.repositories

import android.content.ContentValues
import android.database.Cursor
import com.puntoventa.app.database.DatabaseHelper
import com.puntoventa.app.models.Venta
import com.puntoventa.app.models.VentaDetalle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemVentaInput(
    val productoId: Int,
    val varianteId: Int? = null,
    val cantidad: Int,
    val precioUnitario: Double
)

class VentaRepository(private val databaseHelper: DatabaseHelper) {

    fun calcularSubtotal(cantidad: Int, precioUnitario: Double): Double {
        if (cantidad <= 0 || precioUnitario < 0.0) return 0.0
        return cantidad * precioUnitario
    }

    fun calcularTotal(items: List<ItemVentaInput>): Double {
        return items.sumOf { calcularSubtotal(it.cantidad, it.precioUnitario) }
    }

    fun obtenerPorNegocio(negocioId: Int): List<Venta> {
        if (negocioId <= 0) return emptyList()

        val sql = """
            SELECT venta_id, venta_negocio_id, venta_cliente_id, venta_fecha,
                   venta_total, venta_metodo_pago, venta_monto_pagado
            FROM ventas
            WHERE venta_negocio_id = ?
            ORDER BY venta_id DESC
        """

        val ventas = mutableListOf<Venta>()
        databaseHelper.readableDatabase
            .rawQuery(sql, arrayOf(negocioId.toString()))
            .use { cursor ->
                while (cursor.moveToNext()) {
                    ventas.add(mapearVenta(cursor))
                }
            }
        return ventas
    }

    fun obtenerDetallesPorVenta(ventaId: Int): List<VentaDetalle> {
        if (ventaId <= 0) return emptyList()

        val sql = """
            SELECT venta_det_id, venta_det_venta_id, venta_det_producto_id,
                   venta_det_variante_id, venta_det_cantidad,
                   venta_det_precio_unitario, venta_det_subtotal
            FROM venta_detalle
            WHERE venta_det_venta_id = ?
            ORDER BY venta_det_id ASC
        """

        val detalles = mutableListOf<VentaDetalle>()
        databaseHelper.readableDatabase
            .rawQuery(sql, arrayOf(ventaId.toString()))
            .use { cursor ->
                while (cursor.moveToNext()) {
                    detalles.add(mapearDetalle(cursor))
                }
            }
        return detalles
    }

    fun registrarVenta(
        negocioId: Int,
        clienteId: Int?,
        metodoPago: String,
        montoPagado: Double,
        items: List<ItemVentaInput>,
        fecha: String = fechaActualIso()
    ): ResultadoVenta {
        validarVenta(negocioId, metodoPago, montoPagado, items)?.let {
            return ResultadoVenta.Error(it)
        }

        val total = calcularTotal(items)
        val db = databaseHelper.writableDatabase

        return try {
            db.beginTransaction()

            val valoresVenta = ContentValues()
            valoresVenta.put("venta_negocio_id", negocioId)
            valoresVenta.putIdOpcional("venta_cliente_id", clienteId)
            valoresVenta.put("venta_fecha", fecha.trim().ifEmpty { fechaActualIso() })
            valoresVenta.put("venta_total", total)
            valoresVenta.put("venta_metodo_pago", metodoPago.trim())
            valoresVenta.put("venta_monto_pagado", montoPagado)

            val ventaId = db.insert("ventas", null, valoresVenta)
            if (ventaId == -1L) {
                throw Exception("No se pudo guardar la venta.")
            }

            // Si falla algún detalle se lanza la excepción y la transacción se revierte completa
            for (item in items) {
                val valoresDetalle = ContentValues()
                valoresDetalle.put("venta_det_venta_id", ventaId)
                valoresDetalle.put("venta_det_producto_id", item.productoId)
                valoresDetalle.putIdOpcional("venta_det_variante_id", item.varianteId)
                valoresDetalle.put("venta_det_cantidad", item.cantidad)
                valoresDetalle.put("venta_det_precio_unitario", item.precioUnitario)
                valoresDetalle.put(
                    "venta_det_subtotal",
                    calcularSubtotal(item.cantidad, item.precioUnitario)
                )

                if (db.insert("venta_detalle", null, valoresDetalle) == -1L) {
                    throw Exception("No se pudo guardar el detalle de la venta.")
                }
            }

            db.setTransactionSuccessful()
            ResultadoVenta.Exitoso(
                ventaId = ventaId.toInt(),
                total = total,
                mensaje = "Venta registrada correctamente."
            )
        } catch (e: Exception) {
            ResultadoVenta.Error(e.message ?: "No se pudo registrar la venta.")
        } finally {
            db.endTransaction()
        }
    }

    // Regresa el mensaje de error o null si todo está bien
    private fun validarVenta(
        negocioId: Int,
        metodoPago: String,
        montoPagado: Double,
        items: List<ItemVentaInput>
    ): String? {
        if (negocioId <= 0) return "No se identificó el negocio."
        if (metodoPago.isBlank()) return "Selecciona un método de pago."
        if (items.isEmpty()) return "Agrega al menos un producto a la venta."
        if (items.any { it.cantidad <= 0 || it.precioUnitario < 0.0 }) {
            return "Revisa las cantidades y precios de los productos."
        }
        if (montoPagado < 0.0) return "El monto pagado no puede ser negativo."
        return null
    }

    private fun mapearVenta(c: Cursor): Venta {
        return Venta(
            id = c.getInt(0),
            negocioId = c.getInt(1),
            clienteId = if (c.isNull(2)) null else c.getInt(2),
            fecha = c.getString(3),
            total = c.getDouble(4),
            metodoPago = c.getString(5),
            montoPagado = c.getDouble(6)
        )
    }

    private fun mapearDetalle(c: Cursor): VentaDetalle {
        return VentaDetalle(
            id = c.getInt(0),
            ventaId = c.getInt(1),
            productoId = c.getInt(2),
            varianteId = if (c.isNull(3)) null else c.getInt(3),
            cantidad = c.getInt(4),
            precioUnitario = c.getDouble(5),
            subtotal = c.getDouble(6)
        )
    }

    // Si el id viene null o es 0/negativo se guarda NULL en la base
    private fun ContentValues.putIdOpcional(columna: String, id: Int?) {
        if (id == null || id <= 0) putNull(columna) else put(columna, id)
    }

    private fun fechaActualIso(): String {
        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formato.format(Date())
    }
}

sealed class ResultadoVenta {
    data class Exitoso(
        val ventaId: Int,
        val total: Double,
        val mensaje: String
    ) : ResultadoVenta()

    data class Error(val mensaje: String) : ResultadoVenta()
}