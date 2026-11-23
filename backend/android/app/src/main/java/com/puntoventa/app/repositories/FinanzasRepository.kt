package com.puntoventa.app.repositories

import android.content.ContentValues
import com.puntoventa.app.database.DatabaseHelper
import com.puntoventa.app.models.CategoriaGasto
import com.puntoventa.app.models.Cuenta
import com.puntoventa.app.models.MovimientoFinanciero
import com.puntoventa.app.models.PagoEmpleado

data class BalanceFinanciero(
    val totalIngresos: Double,
    val totalEgresos: Double,
    val balanceNeto: Double,
    val saldoTotalCuentas: Double
)

class FinanzasRepository(
    private val databaseHelper: DatabaseHelper
) {

    fun obtenerCuentasPorNegocio(negocioId: Int): List<Cuenta> {
        val db = databaseHelper.readableDatabase
        val cuentas = mutableListOf<Cuenta>()

        val cursor = db.rawQuery(
            """
            SELECT
                cuenta_id,
                cuenta_negocio_id,
                cuenta_nombre,
                cuenta_tipo,
                cuenta_saldo_actual
            FROM cuentas
            WHERE cuenta_negocio_id = ?
            ORDER BY cuenta_id ASC
            """.trimIndent(),
            arrayOf(negocioId.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                cuentas.add(
                    Cuenta(
                        id = it.getInt(0),
                        negocioId = it.getInt(1),
                        nombre = it.getString(2),
                        tipo = it.getString(3),
                        saldoActual = it.getDouble(4)
                    )
                )
            }
        }

        return cuentas
    }

    fun crearCuenta(
        negocioId: Int,
        nombre: String,
        tipo: String,
        saldoInicial: Double = 0.0
    ): ResultadoFinanzas {
        if (negocioId <= 0) {
            return ResultadoFinanzas.Error("No se identificó el negocio.")
        }
        if (nombre.isBlank()) {
            return ResultadoFinanzas.Error("El nombre de la cuenta es obligatorio.")
        }
        val tipoLimpio = tipo.trim().lowercase()
        if (tipoLimpio.isBlank()) {
            return ResultadoFinanzas.Error("El tipo de cuenta es obligatorio.")
        }

        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put("cuenta_negocio_id", negocioId)
            put("cuenta_nombre", nombre.trim())
            put("cuenta_tipo", tipoLimpio)
            put("cuenta_saldo_actual", saldoInicial)
        }

        return try {
            val id = db.insert("cuentas", null, values)
            if (id == -1L) {
                throw Exception("No se pudo registrar la cuenta.")
            }
            ResultadoFinanzas.Exitoso("Cuenta registrada correctamente.")
        } catch (e: Exception) {
            ResultadoFinanzas.Error(e.message ?: "No se pudo registrar la cuenta.")
        }
    }

    fun obtenerCategoriasGasto(negocioId: Int): List<CategoriaGasto> {
        val db = databaseHelper.readableDatabase
        val categorias = mutableListOf<CategoriaGasto>()

        val cursor = db.rawQuery(
            """
            SELECT
                cat_gasto_id,
                cat_gasto_negocio_id,
                cat_gasto_nombre
            FROM categorias_gasto
            WHERE cat_gasto_negocio_id = ?
            ORDER BY cat_gasto_nombre ASC
            """.trimIndent(),
            arrayOf(negocioId.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                categorias.add(
                    CategoriaGasto(
                        id = it.getInt(0),
                        negocioId = it.getInt(1),
                        nombre = it.getString(2)
                    )
                )
            }
        }

        return categorias
    }

    fun crearCategoriaGasto(
        negocioId: Int,
        nombre: String
    ): ResultadoFinanzas {
        if (negocioId <= 0) {
            return ResultadoFinanzas.Error("No se identificó el negocio.")
        }
        if (nombre.isBlank()) {
            return ResultadoFinanzas.Error("El nombre de la categoría es obligatorio.")
        }

        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put("cat_gasto_negocio_id", negocioId)
            put("cat_gasto_nombre", nombre.trim())
        }

        return try {
            val id = db.insert("categorias_gasto", null, values)
            if (id == -1L) {
                throw Exception("No se pudo registrar la categoría de gasto.")
            }
            ResultadoFinanzas.Exitoso("Categoría de gasto registrada correctamente.")
        } catch (e: Exception) {
            ResultadoFinanzas.Error(
                e.message ?: "No se pudo registrar la categoría de gasto."
            )
        }
    }

    fun obtenerMovimientosPorNegocio(
        negocioId: Int,
        tipoFiltro: String? = null
    ): List<MovimientoFinanciero> {
        val db = databaseHelper.readableDatabase
        val movimientos = mutableListOf<MovimientoFinanciero>()

        val tieneFiltroTipo = !tipoFiltro.isNullOrBlank()
        val filtroSql = if (tieneFiltroTipo) {
            "AND mov_fin_tipo = ?"
        } else {
            ""
        }
        val args = if (tieneFiltroTipo) {
            arrayOf(negocioId.toString(), tipoFiltro!!.trim().lowercase())
        } else {
            arrayOf(negocioId.toString())
        }

        val cursor = db.rawQuery(
            """
            SELECT
                mov_fin_id,
                mov_fin_negocio_id,
                mov_fin_cuenta_id,
                mov_fin_categoria_gasto_id,
                mov_fin_tipo,
                mov_fin_subtipo,
                mov_fin_concepto,
                mov_fin_monto,
                mov_fin_metodo_pago,
                mov_fin_fecha
            FROM movimientos_financieros
            WHERE mov_fin_negocio_id = ?
            $filtroSql
            ORDER BY mov_fin_id DESC
            """.trimIndent(),
            args
        )

        cursor.use {
            while (it.moveToNext()) {
                movimientos.add(
                    MovimientoFinanciero(
                        id = it.getInt(0),
                        negocioId = it.getInt(1),
                        cuentaId = if (it.isNull(2)) null else it.getInt(2),
                        categoriaGastoId = if (it.isNull(3)) null else it.getInt(3),
                        tipo = it.getString(4),
                        subtipo = if (it.isNull(5)) null else it.getString(5),
                        concepto = if (it.isNull(6)) null else it.getString(6),
                        monto = it.getDouble(7),
                        metodoPago = if (it.isNull(8)) null else it.getString(8),
                        fecha = it.getString(9)
                    )
                )
            }
        }

        return movimientos
    }

    fun registrarMovimiento(
        movimiento: MovimientoFinanciero
    ): ResultadoFinanzas {
        if (movimiento.negocioId <= 0) {
            return ResultadoFinanzas.Error("No se identificó el negocio.")
        }
        if (movimiento.monto <= 0.0) {
            return ResultadoFinanzas.Error("El monto debe ser mayor a cero.")
        }
        if (movimiento.fecha.isBlank()) {
            return ResultadoFinanzas.Error("La fecha del movimiento es obligatoria.")
        }

        val tipoNormalizado = movimiento.tipo.trim().lowercase()
        if (tipoNormalizado != "ingreso" && tipoNormalizado != "egreso") {
            return ResultadoFinanzas.Error(
                "El tipo de movimiento debe ser ingreso o egreso."
            )
        }

        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put("mov_fin_negocio_id", movimiento.negocioId)
            if (movimiento.cuentaId != null) {
                put("mov_fin_cuenta_id", movimiento.cuentaId)
            } else {
                putNull("mov_fin_cuenta_id")
            }
            if (movimiento.categoriaGastoId != null) {
                put("mov_fin_categoria_gasto_id", movimiento.categoriaGastoId)
            } else {
                putNull("mov_fin_categoria_gasto_id")
            }
            put("mov_fin_tipo", tipoNormalizado)
            if (movimiento.subtipo.isNullOrBlank()) {
                putNull("mov_fin_subtipo")
            } else {
                put("mov_fin_subtipo", movimiento.subtipo.trim())
            }
            if (movimiento.concepto.isNullOrBlank()) {
                putNull("mov_fin_concepto")
            } else {
                put("mov_fin_concepto", movimiento.concepto.trim())
            }
            put("mov_fin_monto", movimiento.monto)
            if (movimiento.metodoPago.isNullOrBlank()) {
                putNull("mov_fin_metodo_pago")
            } else {
                put("mov_fin_metodo_pago", movimiento.metodoPago.trim())
            }
            put("mov_fin_fecha", movimiento.fecha.trim())
        }

        return try {
            db.beginTransaction()

            val idInsertado = db.insert("movimientos_financieros", null, values)
            if (idInsertado == -1L) {
                throw Exception("No se pudo registrar el movimiento financiero.")
            }

            if (movimiento.cuentaId != null) {
                val delta = if (tipoNormalizado == "ingreso") {
                    movimiento.monto
                } else {
                    -movimiento.monto
                }

                db.execSQL(
                    """
                    UPDATE cuentas
                    SET cuenta_saldo_actual = cuenta_saldo_actual + ?
                    WHERE cuenta_id = ? AND cuenta_negocio_id = ?
                    """.trimIndent(),
                    arrayOf(delta, movimiento.cuentaId, movimiento.negocioId)
                )
            }

            db.setTransactionSuccessful()
            ResultadoFinanzas.Exitoso("Movimiento registrado correctamente.")
        } catch (e: Exception) {
            ResultadoFinanzas.Error(
                e.message ?: "No se pudo registrar el movimiento financiero."
            )
        } finally {
            db.endTransaction()
        }
    }

    fun registrarPagoNomina(pago: PagoEmpleado): ResultadoFinanzas {
        if (pago.monto <= 0.0) {
            return ResultadoFinanzas.Error("El monto de nómina debe ser mayor a cero.")
        }
        if (pago.nombreEmpleado.isBlank()) {
            return ResultadoFinanzas.Error("El nombre del empleado es obligatorio.")
        }

        val concepto = buildString {
            append("Nomina - ")
            append(pago.nombreEmpleado.trim())
            if (pago.periodo.isNotBlank()) {
                append(" (")
                append(pago.periodo.trim())
                append(")")
            }
        }

        return registrarMovimiento(
            MovimientoFinanciero(
                negocioId = pago.negocioId,
                cuentaId = pago.cuentaId,
                categoriaGastoId = null,
                tipo = "egreso",
                subtipo = "nomina",
                concepto = concepto,
                monto = pago.monto,
                metodoPago = pago.metodoPago,
                fecha = pago.fechaPago
            )
        )
    }

    fun obtenerBalance(negocioId: Int): BalanceFinanciero {
        val db = databaseHelper.readableDatabase

        var totalIngresos = 0.0
        var totalEgresos = 0.0

        db.rawQuery(
            """
            SELECT
                COALESCE(SUM(CASE WHEN mov_fin_tipo = 'ingreso' THEN mov_fin_monto ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN mov_fin_tipo = 'egreso' THEN mov_fin_monto ELSE 0 END), 0)
            FROM movimientos_financieros
            WHERE mov_fin_negocio_id = ?
            """.trimIndent(),
            arrayOf(negocioId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                totalIngresos = cursor.getDouble(0)
                totalEgresos = cursor.getDouble(1)
            }
        }

        var saldoTotalCuentas = 0.0
        db.rawQuery(
            """
            SELECT COALESCE(SUM(cuenta_saldo_actual), 0)
            FROM cuentas
            WHERE cuenta_negocio_id = ?
            """.trimIndent(),
            arrayOf(negocioId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                saldoTotalCuentas = cursor.getDouble(0)
            }
        }

        return BalanceFinanciero(
            totalIngresos = totalIngresos,
            totalEgresos = totalEgresos,
            balanceNeto = totalIngresos - totalEgresos,
            saldoTotalCuentas = saldoTotalCuentas
        )
    }
}

sealed class ResultadoFinanzas {
    data class Exitoso(val mensaje: String) : ResultadoFinanzas()
    data class Error(val mensaje: String) : ResultadoFinanzas()
}