package com.puntoventa.app.repositories

import android.content.ContentValues
import android.database.Cursor
import android.util.Patterns
import com.puntoventa.app.database.DatabaseHelper
import com.puntoventa.app.models.Cliente

class ClienteRepository(private val databaseHelper: DatabaseHelper) {

    fun obtenerPorNegocio(negocioId: Int, soloActivos: Boolean = false): List<Cliente> {
        if (negocioId <= 0) return emptyList()

        val filtroActivo = if (soloActivos) "AND cliente_activo = 1" else ""

        val sql = """
            SELECT cliente_id, cliente_negocio_id, cliente_nombre,
                   cliente_apellido_paterno, cliente_apellido_materno,
                   cliente_telefono, cliente_correo, cliente_direccion,
                   cliente_tipo_pago_habitual, cliente_activo
            FROM clientes
            WHERE cliente_negocio_id = ? $filtroActivo
            ORDER BY cliente_activo DESC, cliente_nombre ASC, cliente_id DESC
        """

        val lista = mutableListOf<Cliente>()
        databaseHelper.readableDatabase
            .rawQuery(sql, arrayOf(negocioId.toString()))
            .use { cursor ->
                while (cursor.moveToNext()) {
                    lista.add(mapearCliente(cursor))
                }
            }
        return lista
    }

    fun buscar(negocioId: Int, termino: String, soloActivos: Boolean = false): List<Cliente> {
        if (negocioId <= 0) return emptyList()

        val clientes = obtenerPorNegocio(negocioId, soloActivos)
        val texto = termino.trim().lowercase()
        if (texto.isEmpty()) return clientes

        // Se filtra en memoria porque la lista de clientes de un negocio no es enorme
        return clientes.filter { c ->
            c.nombreCompleto().lowercase().contains(texto) ||
                    contiene(c.telefono, texto) ||
                    contiene(c.correo, texto) ||
                    contiene(c.tipoPagoHabitual, texto)
        }
    }

    fun registrar(
        negocioId: Int,
        nombre: String,
        apellidoPaterno: String?,
        apellidoMaterno: String?,
        telefono: String?,
        correo: String?,
        direccion: String?,
        tipoPagoHabitual: String?
    ): ResultadoCliente {
        validar(negocioId, nombre, correo)?.let { return ResultadoCliente.Error(it) }

        val valores = crearValores(
            negocioId, nombre, apellidoPaterno, apellidoMaterno,
            telefono, correo, direccion, tipoPagoHabitual
        )
        valores.put("cliente_activo", 1)

        return try {
            val id = databaseHelper.writableDatabase.insert("clientes", null, valores)
            if (id == -1L) {
                ResultadoCliente.Error("No se pudo registrar el cliente.")
            } else {
                ResultadoCliente.Exitoso("Cliente registrado correctamente.")
            }
        } catch (e: Exception) {
            ResultadoCliente.Error(e.message ?: "No se pudo registrar el cliente.")
        }
    }

    fun actualizar(
        clienteId: Int,
        negocioId: Int,
        nombre: String,
        apellidoPaterno: String?,
        apellidoMaterno: String?,
        telefono: String?,
        correo: String?,
        direccion: String?,
        tipoPagoHabitual: String?
    ): ResultadoCliente {
        validar(negocioId, nombre, correo)?.let { return ResultadoCliente.Error(it) }

        val valores = crearValores(
            negocioId, nombre, apellidoPaterno, apellidoMaterno,
            telefono, correo, direccion, tipoPagoHabitual
        )

        return try {
            val filas = actualizarFilas(clienteId, negocioId, valores)
            if (filas > 0) {
                ResultadoCliente.Exitoso("Cliente actualizado correctamente.")
            } else {
                ResultadoCliente.Error("No se encontró el cliente en este negocio.")
            }
        } catch (e: Exception) {
            ResultadoCliente.Error(e.message ?: "No se pudo actualizar el cliente.")
        }
    }

    fun cambiarEstado(clienteId: Int, negocioId: Int, activo: Boolean): ResultadoCliente {
        if (negocioId <= 0) {
            return ResultadoCliente.Error("No se identificó el negocio.")
        }

        val valores = ContentValues()
        valores.put("cliente_activo", if (activo) 1 else 0)

        return try {
            val filas = actualizarFilas(clienteId, negocioId, valores)
            if (filas > 0) {
                val accion = if (activo) "activado" else "desactivado"
                ResultadoCliente.Exitoso("Cliente $accion correctamente.")
            } else {
                ResultadoCliente.Error("No se encontró el cliente en este negocio.")
            }
        } catch (e: Exception) {
            ResultadoCliente.Error(e.message ?: "No se pudo cambiar el estado del cliente.")
        }
    }

    // Update común: siempre filtra por cliente Y negocio para no tocar datos de otro negocio
    private fun actualizarFilas(clienteId: Int, negocioId: Int, valores: ContentValues): Int {
        return databaseHelper.writableDatabase.update(
            "clientes",
            valores,
            "cliente_id = ? AND cliente_negocio_id = ?",
            arrayOf(clienteId.toString(), negocioId.toString())
        )
    }

    private fun crearValores(
        negocioId: Int,
        nombre: String,
        apellidoPaterno: String?,
        apellidoMaterno: String?,
        telefono: String?,
        correo: String?,
        direccion: String?,
        tipoPagoHabitual: String?
    ): ContentValues {
        val valores = ContentValues()
        valores.put("cliente_negocio_id", negocioId)
        valores.put("cliente_nombre", nombre.trim())
        valores.putTexto("cliente_apellido_paterno", apellidoPaterno)
        valores.putTexto("cliente_apellido_materno", apellidoMaterno)
        valores.putTexto("cliente_telefono", telefono)
        valores.putTexto("cliente_correo", correo)
        valores.putTexto("cliente_direccion", direccion)
        valores.putTexto("cliente_tipo_pago_habitual", tipoPagoHabitual)
        return valores
    }

    // Devuelve el mensaje de error, o null si todo está bien
    private fun validar(negocioId: Int, nombre: String, correo: String?): String? {
        if (negocioId <= 0) return "No se identificó el negocio."
        if (nombre.isBlank()) return "El nombre del cliente es obligatorio."

        if (!correo.isNullOrBlank() && !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) {
            return "Ingresa un correo electrónico válido."
        }
        return null
    }

    // getString ya regresa null cuando la columna es NULL, no hace falta revisar isNull
    private fun mapearCliente(c: Cursor): Cliente {
        return Cliente(
            id = c.getInt(0),
            negocioId = c.getInt(1),
            nombre = c.getString(2),
            apellidoPaterno = c.getString(3),
            apellidoMaterno = c.getString(4),
            telefono = c.getString(5),
            correo = c.getString(6),
            direccion = c.getString(7),
            tipoPagoHabitual = c.getString(8),
            activo = c.getInt(9)
        )
    }

    private fun contiene(campo: String?, texto: String): Boolean {
        return campo?.lowercase()?.contains(texto) == true
    }

    // Guarda NULL en lugar de textos vacíos
    private fun ContentValues.putTexto(columna: String, valor: String?) {
        val limpio = valor?.trim()
        if (limpio.isNullOrEmpty()) putNull(columna) else put(columna, limpio)
    }
}

sealed class ResultadoCliente {
    data class Exitoso(val mensaje: String) : ResultadoCliente()
    data class Error(val mensaje: String) : ResultadoCliente()
}