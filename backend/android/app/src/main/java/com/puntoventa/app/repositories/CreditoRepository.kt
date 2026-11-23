package com.puntoventa.app.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.puntoventa.app.database.DatabaseHelper
import com.puntoventa.app.models.Credito
import com.puntoventa.app.models.PagoCredito
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreditoRepository(private val databaseHelper: DatabaseHelper) {

    fun calcularFechaVencimiento(fechaInicio: String, plazo: String): String {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val fechaBase = try {
            formato.parse(fechaInicio.take(10)) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        val calendario = Calendar.getInstance()
        calendario.time = fechaBase

        when (plazo.trim().lowercase()) {
            "1_mes", "1 mes", "mensual" -> calendario.add(Calendar.MONTH, 1)
            else -> calendario.add(Calendar.DAY_OF_YEAR, 15)
        }

        return formato.format(calendario.time)
    }

    fun crearCredito(
        ventaId: Int,
        clienteId: Int,
        montoTotal: Double,
        plazo: String,
        fechaInicio: String = fechaActualIso()
    ): ResultadoCredito {
        if (ventaId <= 0) {
            return ResultadoCredito.Error("La venta asociada no es válida.")
        }
        if (clienteId <= 0) {
            return ResultadoCredito.Error("Selecciona un cliente para registrar el crédito.")
        }
        if (montoTotal <= 0.0) {
            return ResultadoCredito.Error("El monto del crédito debe ser mayor a cero.")
        }
        if (plazo.isBlank()) {
            return ResultadoCredito.Error("Selecciona el plazo del crédito.")
        }

        val valores = ContentValues()
        valores.put("cred_venta_id", ventaId)
        valores.put("cred_cliente_id", clienteId)
        valores.put("cred_monto_total", montoTotal)
        valores.put("cred_plazo", plazo.trim())
        valores.put("cred_fecha_inicio", fechaInicio.trim().ifEmpty { fechaActualIso() })
        valores.put("cred_fecha_vencimiento", calcularFechaVencimiento(fechaInicio, plazo))
        valores.put("cred_estado", "vigente")

        return try {
            val id = databaseHelper.writableDatabase.insert("creditos", null, valores)
            if (id == -1L) {
                ResultadoCredito.Error("No se pudo registrar el crédito.")
            } else {
                ResultadoCredito.Exitoso("Crédito registrado correctamente.")
            }
        } catch (e: Exception) {
            ResultadoCredito.Error(e.message ?: "No se pudo registrar el crédito.")
        }
    }

    fun obtenerCuentasPorCobrar(negocioId: Int, soloPendientes: Boolean = true): List<Credito> {
        if (negocioId <= 0) return emptyList()

        val filtroEstado = if (soloPendientes) "AND c.cred_estado != 'pagado'" else ""

        val sql = """
            SELECT
                c.cred_id,
                c.cred_venta_id,
                c.cred_cliente_id,
                c.cred_monto_total,
                c.cred_plazo,
                c.cred_fecha_inicio,
                c.cred_fecha_vencimiento,
                c.cred_estado,
                TRIM(
                    COALESCE(cl.cliente_nombre, '') || ' ' ||
                    COALESCE(cl.cliente_apellido_paterno, '') || ' ' ||
                    COALESCE(cl.cliente_apellido_materno, '')
                ) AS nombre_cliente,
                COALESCE((
                    SELECT SUM(p.pago_cred_monto)
                    FROM pagos_credito p
                    WHERE p.pago_cred_credito_id = c.cred_id
                ), 0.0) AS monto_abonado
            FROM creditos c
            INNER JOIN ventas v ON v.venta_id = c.cred_venta_id
            LEFT JOIN clientes cl ON cl.cliente_id = c.cred_cliente_id
            WHERE v.venta_negocio_id = ? $filtroEstado
            ORDER BY c.cred_fecha_vencimiento ASC, c.cred_id DESC
        """

        val creditos = mutableListOf<Credito>()
        databaseHelper.readableDatabase
            .rawQuery(sql, arrayOf(negocioId.toString()))
            .use { cursor ->
                while (cursor.moveToNext()) {
                    creditos.add(mapearCredito(cursor))
                }
            }
        return creditos
    }

    fun obtenerPagosPorCredito(creditoId: Int): List<PagoCredito> {
        if (creditoId <= 0) return emptyList()

        val sql = """
            SELECT pago_cred_id, pago_cred_credito_id, pago_cred_monto,
                   pago_cred_metodo_pago, pago_cred_referencia, pago_cred_fecha
            FROM pagos_credito
            WHERE pago_cred_credito_id = ?
            ORDER BY pago_cred_id DESC
        """

        val pagos = mutableListOf<PagoCredito>()
        databaseHelper.readableDatabase
            .rawQuery(sql, arrayOf(creditoId.toString()))
            .use { cursor ->
                while (cursor.moveToNext()) {
                    pagos.add(mapearPago(cursor))
                }
            }
        return pagos
    }

    fun registrarAbono(
        creditoId: Int,
        monto: Double,
        metodoPago: String,
        referencia: String? = null,
        fecha: String = fechaActualIso()
    ): ResultadoCredito {
        if (creditoId <= 0) {
            return ResultadoCredito.Error("Selecciona un crédito válido.")
        }
        if (monto <= 0.0) {
            return ResultadoCredito.Error("El abono debe ser mayor a cero.")
        }
        if (metodoPago.isBlank()) {
            return ResultadoCredito.Error("Selecciona un método de pago para el abono.")
        }

        val db = databaseHelper.writableDatabase

        return try {
            db.beginTransaction()

            val montoTotal = obtenerMontoTotal(db, creditoId)
            val abonadoPrevio = obtenerTotalAbonado(db, creditoId)

            if (monto > montoTotal - abonadoPrevio + 0.0001) {
                throw Exception("El abono supera el saldo pendiente.")
            }

            val referenciaLimpia = referencia?.trim()

            val valoresPago = ContentValues()
            valoresPago.put("pago_cred_credito_id", creditoId)
            valoresPago.put("pago_cred_monto", monto)
            valoresPago.put("pago_cred_metodo_pago", metodoPago.trim())
            if (referenciaLimpia.isNullOrEmpty()) {
                valoresPago.putNull("pago_cred_referencia")
            } else {
                valoresPago.put("pago_cred_referencia", referenciaLimpia)
            }
            valoresPago.put("pago_cred_fecha", fecha.trim().ifEmpty { fechaActualIso() })

            if (db.insert("pagos_credito", null, valoresPago) == -1L) {
                throw Exception("No se pudo registrar el abono.")
            }

            if (abonadoPrevio + monto >= montoTotal - 0.0001) {
                val valoresCredito = ContentValues()
                valoresCredito.put("cred_estado", "pagado")
                db.update("creditos", valoresCredito, "cred_id = ?", arrayOf(creditoId.toString()))
            }

            db.setTransactionSuccessful()
            ResultadoCredito.Exitoso("Abono registrado correctamente.")
        } catch (e: Exception) {
            ResultadoCredito.Error(e.message ?: "No se pudo registrar el abono.")
        } finally {
            db.endTransaction()
        }
    }

    private fun obtenerMontoTotal(db: SQLiteDatabase, creditoId: Int): Double {
        return db.rawQuery(
            "SELECT cred_monto_total FROM creditos WHERE cred_id = ?",
            arrayOf(creditoId.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                throw Exception("No se encontró el crédito.")
            }
            cursor.getDouble(0)
        }
    }

    private fun obtenerTotalAbonado(db: SQLiteDatabase, creditoId: Int): Double {
        return db.rawQuery(
            "SELECT COALESCE(SUM(pago_cred_monto), 0.0) FROM pagos_credito WHERE pago_cred_credito_id = ?",
            arrayOf(creditoId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        }
    }

    private fun mapearCredito(c: Cursor): Credito {
        return Credito(
            id = c.getInt(0),
            ventaId = c.getInt(1),
            clienteId = c.getInt(2),
            montoTotal = c.getDouble(3),
            plazo = c.getString(4),
            fechaInicio = c.getString(5),
            fechaVencimiento = c.getString(6),
            estado = c.getString(7),
            nombreCliente = c.getString(8),
            montoAbonado = c.getDouble(9)
        )
    }

    private fun mapearPago(c: Cursor): PagoCredito {
        return PagoCredito(
            id = c.getInt(0),
            creditoId = c.getInt(1),
            monto = c.getDouble(2),
            metodoPago = c.getString(3),
            referencia = c.getString(4),
            fecha = c.getString(5)
        )
    }

    private fun fechaActualIso(): String {
        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formato.format(Date())
    }
}

sealed class ResultadoCredito {
    data class Exitoso(val mensaje: String) : ResultadoCredito()
    data class Error(val mensaje: String) : ResultadoCredito()
}