
package com.puntoventa.app.repositories

import android.content.ContentValues
import android.util.Patterns
import com.puntoventa.app.database.DatabaseHelper
import com.puntoventa.app.models.Negocio

class NegocioRepository(
    private val databaseHelper: DatabaseHelper
) {

    fun obtenerTodos(): List<Negocio> {
        val db = databaseHelper.readableDatabase
        val negocios = mutableListOf<Negocio>()

        val cursor = db.rawQuery(
            """
            SELECT
                negocio_id,
                negocio_nombre_comercial,
                negocio_rfc,
                negocio_giro,
                negocio_telefono,
                negocio_correo,
                negocio_direccion,
                negocio_es_principal,
                negocio_activo,
                negocio_propietario_id,
                negocio_logo
            FROM negocios
            ORDER BY negocio_es_principal DESC, negocio_id DESC
            """.trimIndent(),
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                negocios.add(
                    Negocio(
                        id = it.getInt(0),
                        nombreComercial = it.getString(1),
                        rfc = it.getString(2),
                        giro = it.getString(3),
                        telefono = it.getString(4),
                        correo = it.getString(5),
                        direccion = it.getString(6),
                        esPrincipal = it.getInt(7),
                        activo = it.getInt(8),
                        propietarioId = if (it.isNull(9)) null else it.getInt(9),
                        logo = if (it.isNull(10)) null else it.getString(10)
                    )
                )
            }
        }

        return negocios
    }

    fun obtenerPorPropietario(propietarioId: Int): List<Negocio> {
        return obtenerNegociosPorPropietario(propietarioId, false)
    }

    fun obtenerActivos(): List<Negocio> {
        return obtenerTodos().filter { it.activo == 1 }
    }

    fun obtenerActivosPorPropietario(propietarioId: Int): List<Negocio> {
        return obtenerNegociosPorPropietario(propietarioId, true)
    }

    private fun obtenerNegociosPorPropietario(
        propietarioId: Int,
        soloActivos: Boolean
    ): List<Negocio> {

        val db = databaseHelper.readableDatabase
        val negocios = mutableListOf<Negocio>()

        val filtroActivo = if (soloActivos) {
            "AND negocio_activo = 1"
        } else {
            ""
        }

        val cursor = db.rawQuery(
            """
            SELECT
                negocio_id,
                negocio_nombre_comercial,
                negocio_rfc,
                negocio_giro,
                negocio_telefono,
                negocio_correo,
                negocio_direccion,
                negocio_es_principal,
                negocio_activo,
                negocio_propietario_id,
                negocio_logo
            FROM negocios
            WHERE negocio_propietario_id = ?
            $filtroActivo
            ORDER BY negocio_es_principal DESC, negocio_id DESC
            """.trimIndent(),
            arrayOf(propietarioId.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                negocios.add(
                    Negocio(
                        id = it.getInt(0),
                        nombreComercial = it.getString(1),
                        rfc = it.getString(2),
                        giro = it.getString(3),
                        telefono = it.getString(4),
                        correo = it.getString(5),
                        direccion = it.getString(6),
                        esPrincipal = it.getInt(7),
                        activo = it.getInt(8),
                        propietarioId = if (it.isNull(9)) null else it.getInt(9),
                        logo = if (it.isNull(10)) null else it.getString(10)
                    )
                )
            }
        }

        return negocios
    }

    fun registrar(
        nombreComercial: String,
        rfc: String,
        giro: String,
        telefono: String,
        correo: String,
        direccion: String,
        logo: String?,
        esPrincipal: Boolean,
        propietarioId: Int
    ): ResultadoNegocio {

        val error = validarDatos(nombreComercial, correo)
        if (error != null) return ResultadoNegocio.Error(error)

        if (propietarioId <= 0) {
            return ResultadoNegocio.Error(
                "No se identificó al administrador."
            )
        }

        val db = databaseHelper.writableDatabase

        val valores = crearValores(
            nombreComercial,
            rfc,
            giro,
            telefono,
            correo,
            direccion,
            logo
        )

        valores.put("negocio_propietario_id", propietarioId)

        return try {
            db.beginTransaction()

            if (esPrincipal) {
                db.execSQL(
                    """
                    UPDATE negocios
                    SET negocio_es_principal = 0
                    WHERE negocio_propietario_id = ?
                    """.trimIndent(),
                    arrayOf(propietarioId)
                )

                valores.put("negocio_es_principal", 1)
            } else {
                valores.put("negocio_es_principal", 0)
            }

            val id = db.insert("negocios", null, valores)

            if (id == -1L) {
                throw Exception("No se pudo registrar el negocio.")
            }

            db.setTransactionSuccessful()

            ResultadoNegocio.Exitoso(
                "Negocio registrado correctamente."
            )

        } catch (e: Exception) {
            ResultadoNegocio.Error(
                e.message ?: "No se pudo registrar el negocio."
            )
        } finally {
            db.endTransaction()
        }
    }

    fun actualizar(
        id: Int,
        nombreComercial: String,
        rfc: String,
        giro: String,
        telefono: String,
        correo: String,
        direccion: String,
        logo: String?,
        esPrincipal: Boolean,
        propietarioId: Int
    ): ResultadoNegocio {

        val error = validarDatos(nombreComercial, correo)
        if (error != null) return ResultadoNegocio.Error(error)

        if (propietarioId <= 0) {
            return ResultadoNegocio.Error(
                "No se identificó al administrador."
            )
        }

        val db = databaseHelper.writableDatabase

        val valores = crearValores(
            nombreComercial,
            rfc,
            giro,
            telefono,
            correo,
            direccion,
            logo
        )

        return try {
            db.beginTransaction()

            if (esPrincipal) {
                db.execSQL(
                    """
                    UPDATE negocios
                    SET negocio_es_principal = 0
                    WHERE negocio_propietario_id = ?
                    """.trimIndent(),
                    arrayOf(propietarioId)
                )

                valores.put("negocio_es_principal", 1)
            } else {
                valores.put("negocio_es_principal", 0)
            }

            val filas = db.update(
                "negocios",
                valores,
                "negocio_id = ? AND negocio_propietario_id = ?",
                arrayOf(id.toString(), propietarioId.toString())
            )

            if (filas == 0) {
                throw Exception(
                    "No se encontró el negocio o no te pertenece."
                )
            }

            db.setTransactionSuccessful()

            ResultadoNegocio.Exitoso(
                "Negocio actualizado correctamente."
            )

        } catch (e: Exception) {
            ResultadoNegocio.Error(
                e.message ?: "No se pudo actualizar el negocio."
            )
        } finally {
            db.endTransaction()
        }
    }

    fun cambiarEstado(
        id: Int,
        activo: Boolean,
        propietarioId: Int
    ): ResultadoNegocio {

        if (propietarioId <= 0) {
            return ResultadoNegocio.Error(
                "No se identificó al administrador."
            )
        }

        val valores = ContentValues().apply {
            put("negocio_activo", if (activo) 1 else 0)
        }

        val filas = databaseHelper.writableDatabase.update(
            "negocios",
            valores,
            "negocio_id = ? AND negocio_propietario_id = ?",
            arrayOf(id.toString(), propietarioId.toString())
        )

        return if (filas > 0) {
            ResultadoNegocio.Exitoso(
                if (activo) {
                    "Negocio activado correctamente."
                } else {
                    "Negocio desactivado correctamente."
                }
            )
        } else {
            ResultadoNegocio.Error(
                "No se encontró el negocio o no te pertenece."
            )
        }
    }

    private fun crearValores(
        nombreComercial: String,
        rfc: String,
        giro: String,
        telefono: String,
        correo: String,
        direccion: String,
        logo: String?
    ): ContentValues {

        return ContentValues().apply {
            put("negocio_nombre_comercial", nombreComercial.trim())
            put("negocio_rfc", rfc.trim())
            put("negocio_giro", giro.trim())
            put("negocio_telefono", telefono.trim())
            put("negocio_correo", correo.trim())
            put("negocio_direccion", direccion.trim())

            if (logo == null) {
                putNull("negocio_logo")
            } else {
                put("negocio_logo", logo)
            }
        }
    }

    private fun validarDatos(
        nombreComercial: String,
        correo: String
    ): String? {

        if (nombreComercial.isBlank()) {
            return "El nombre comercial es obligatorio."
        }

        if (
            correo.isNotBlank() &&
            !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()
        ) {
            return "Ingresa un correo electrónico válido."
        }

        return null
    }
}

sealed class ResultadoNegocio {
    data class Exitoso(val mensaje: String) : ResultadoNegocio()
    data class Error(val mensaje: String) : ResultadoNegocio()
}
