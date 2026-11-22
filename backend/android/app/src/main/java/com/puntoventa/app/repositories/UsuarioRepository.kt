
package com.puntoventa.app.repositories

import android.content.ContentValues
import android.util.Patterns
import com.puntoventa.app.database.DatabaseHelper
import com.puntoventa.app.models.Usuario

class UsuarioRepository(
    private val databaseHelper: DatabaseHelper
) {

    fun obtenerTodos(): List<Usuario> {
        val db = databaseHelper.readableDatabase
        val usuarios = mutableListOf<Usuario>()

        val cursor = db.rawQuery(
            """
            SELECT usuario_id, usuario_correo, usuario_password,
                   rol_id, usuario_activo, usuario_nombre,
                   usuario_apellido, usuario_apellido_materno,
                   usuario_nombre_usuario, negocio_id
            FROM usuarios
            ORDER BY usuario_id
            """.trimIndent(),
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                usuarios.add(
                    Usuario(
                        id = it.getInt(0),
                        correo = it.getString(1),
                        password = it.getString(2),
                        rolId = it.getInt(3),
                        activo = it.getInt(4),
                        nombre = it.getString(5),
                        apellido = it.getString(6),
                        apellidoMaterno = it.getString(7),
                        nombreUsuario = it.getString(8),
                        negocioId = if (it.isNull(9)) null else it.getInt(9)
                    )
                )
            }
        }

        return usuarios
    }

    fun buscarPorCorreo(correo: String): Usuario? {
        val db = databaseHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT usuario_id, usuario_correo, usuario_password,
                   rol_id, usuario_activo, usuario_nombre,
                   usuario_apellido, usuario_apellido_materno,
                   usuario_nombre_usuario, negocio_id
            FROM usuarios
            WHERE usuario_correo = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(correo)
        )

        cursor.use {
            if (it.moveToFirst()) {
                return Usuario(
                    id = it.getInt(0),
                    correo = it.getString(1),
                    password = it.getString(2),
                    rolId = it.getInt(3),
                    activo = it.getInt(4),
                    nombre = it.getString(5),
                    apellido = it.getString(6),
                    apellidoMaterno = it.getString(7),
                    nombreUsuario = it.getString(8),
                    negocioId = if (it.isNull(9)) null else it.getInt(9)
                )
            }
        }

        return null
    }

    fun buscarEmpleados(
        termino: String,
        negocioId: Int
    ): List<Usuario> {
        val db = databaseHelper.readableDatabase
        val empleados = mutableListOf<Usuario>()
        val busqueda = "%${termino.trim()}%"

        val cursor = db.rawQuery(
            """
            SELECT usuario_id, usuario_correo, usuario_password,
                   rol_id, usuario_activo, usuario_nombre,
                   usuario_apellido, usuario_apellido_materno,
                   usuario_nombre_usuario, negocio_id
            FROM usuarios
            WHERE rol_id = 2
              AND negocio_id = ?
              AND (
                  usuario_nombre LIKE ?
                  OR usuario_apellido LIKE ?
                  OR usuario_apellido_materno LIKE ?
                  OR usuario_correo LIKE ?
                  OR usuario_nombre_usuario LIKE ?
              )
            ORDER BY usuario_nombre
            """.trimIndent(),
            arrayOf(
                negocioId.toString(),
                busqueda,
                busqueda,
                busqueda,
                busqueda,
                busqueda
            )
        )

        cursor.use {
            while (it.moveToNext()) {
                empleados.add(
                    Usuario(
                        id = it.getInt(0),
                        correo = it.getString(1),
                        password = it.getString(2),
                        rolId = it.getInt(3),
                        activo = it.getInt(4),
                        nombre = it.getString(5),
                        apellido = it.getString(6),
                        apellidoMaterno = it.getString(7),
                        nombreUsuario = it.getString(8),
                        negocioId = if (it.isNull(9)) null else it.getInt(9)
                    )
                )
            }
        }

        return empleados
    }

    fun crearEmpleado(
        nombre: String,
        apellido: String,
        apellidoMaterno: String,
        correo: String,
        nombreUsuario: String,
        password: String,
        negocioId: Int
    ): ResultadoEmpleado {

        val nombreLimpio = nombre.trim()
        val apellidoLimpio = apellido.trim()
        val apellidoMaternoLimpio = apellidoMaterno.trim()
        val correoLimpio = correo.trim().lowercase()
        val usuarioLimpio = nombreUsuario.trim()

        if (
            nombreLimpio.isBlank() ||
            apellidoLimpio.isBlank() ||
            apellidoMaternoLimpio.isBlank() ||
            correoLimpio.isBlank() ||
            usuarioLimpio.isBlank() ||
            password.isBlank()
        ) {
            return ResultadoEmpleado.Error("Todos los campos son obligatorios")
        }

        if (!nombresValidos(nombreLimpio, apellidoLimpio, apellidoMaternoLimpio)) {
            return ResultadoEmpleado.Error("Nombre o apellido no válido")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            return ResultadoEmpleado.Error("Ingresa un correo válido")
        }

        if (!passwordValida(password)) {
            return ResultadoEmpleado.Error(
                "La contraseña requiere 8 caracteres, mayúscula, minúscula y número"
            )
        }

        val db = databaseHelper.writableDatabase

        val negocioValido = db.rawQuery(
            """
            SELECT negocio_id FROM negocios
            WHERE negocio_id = ? AND negocio_activo = 1
            LIMIT 1
            """.trimIndent(),
            arrayOf(negocioId.toString())
        ).use { it.moveToFirst() }

        if (!negocioValido) {
            return ResultadoEmpleado.Error("Selecciona un negocio válido y activo")
        }

        val existeCorreo = db.rawQuery(
            """
            SELECT usuario_id FROM usuarios
            WHERE LOWER(usuario_correo) = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(correoLimpio)
        ).use { it.moveToFirst() }

        if (existeCorreo) {
            return ResultadoEmpleado.Error("El correo ya está registrado")
        }

        val existeUsuario = db.rawQuery(
            """
            SELECT usuario_id FROM usuarios
            WHERE usuario_nombre_usuario = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(usuarioLimpio)
        ).use { it.moveToFirst() }

        if (existeUsuario) {
            return ResultadoEmpleado.Error("El nombre de usuario ya existe")
        }

        val valores = ContentValues().apply {
            put("usuario_correo", correoLimpio)
            put("usuario_password", password)
            put("rol_id", 2)
            put("usuario_activo", 1)
            put("usuario_nombre", nombreLimpio)
            put("usuario_apellido", apellidoLimpio)
            put("usuario_apellido_materno", apellidoMaternoLimpio)
            put("usuario_nombre_usuario", usuarioLimpio)
            put("negocio_id", negocioId)
        }

        val nuevoId = db.insert("usuarios", null, valores)

        return if (nuevoId != -1L) {
            ResultadoEmpleado.Exitoso("Empleado registrado correctamente")
        } else {
            ResultadoEmpleado.Error("No se pudo registrar el empleado")
        }
    }

    fun actualizarEmpleado(
        id: Int,
        nombre: String,
        apellido: String,
        apellidoMaterno: String,
        correo: String,
        nombreUsuario: String,
        password: String,
        negocioId: Int
    ): ResultadoEmpleado {

        val nombreLimpio = nombre.trim()
        val apellidoLimpio = apellido.trim()
        val apellidoMaternoLimpio = apellidoMaterno.trim()
        val correoLimpio = correo.trim().lowercase()
        val usuarioLimpio = nombreUsuario.trim()

        if (
            nombreLimpio.isBlank() ||
            apellidoLimpio.isBlank() ||
            apellidoMaternoLimpio.isBlank() ||
            correoLimpio.isBlank() ||
            usuarioLimpio.isBlank()
        ) {
            return ResultadoEmpleado.Error("Completa todos los campos obligatorios")
        }

        if (!nombresValidos(nombreLimpio, apellidoLimpio, apellidoMaternoLimpio)) {
            return ResultadoEmpleado.Error("Nombre o apellido no válido")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            return ResultadoEmpleado.Error("Ingresa un correo válido")
        }

        if (password.isNotBlank() && !passwordValida(password)) {
            return ResultadoEmpleado.Error(
                "La nueva contraseña requiere 8 caracteres, mayúscula, minúscula y número"
            )
        }

        val db = databaseHelper.writableDatabase

        val negocioValido = db.rawQuery(
            """
            SELECT negocio_id FROM negocios
            WHERE negocio_id = ? AND negocio_activo = 1
            LIMIT 1
            """.trimIndent(),
            arrayOf(negocioId.toString())
        ).use { it.moveToFirst() }

        if (!negocioValido) {
            return ResultadoEmpleado.Error("Selecciona un negocio válido y activo")
        }

        val existeCorreo = db.rawQuery(
            """
            SELECT usuario_id FROM usuarios
            WHERE LOWER(usuario_correo) = ?
              AND usuario_id != ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(correoLimpio, id.toString())
        ).use { it.moveToFirst() }

        if (existeCorreo) {
            return ResultadoEmpleado.Error("Ese correo ya pertenece a otro usuario")
        }

        val existeUsuario = db.rawQuery(
            """
            SELECT usuario_id FROM usuarios
            WHERE usuario_nombre_usuario = ?
              AND usuario_id != ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(usuarioLimpio, id.toString())
        ).use { it.moveToFirst() }

        if (existeUsuario) {
            return ResultadoEmpleado.Error("Ese nombre de usuario ya existe")
        }

        val valores = ContentValues().apply {
            put("usuario_nombre", nombreLimpio)
            put("usuario_apellido", apellidoLimpio)
            put("usuario_apellido_materno", apellidoMaternoLimpio)
            put("usuario_correo", correoLimpio)
            put("usuario_nombre_usuario", usuarioLimpio)
            put("negocio_id", negocioId)

            if (password.isNotBlank()) {
                put("usuario_password", password)
            }
        }

        val filas = db.update(
            "usuarios",
            valores,
            "usuario_id = ? AND rol_id = 2",
            arrayOf(id.toString())
        )

        return if (filas > 0) {
            ResultadoEmpleado.Exitoso("Empleado actualizado correctamente")
        } else {
            ResultadoEmpleado.Error("No se pudo actualizar el empleado")
        }
    }

    fun cambiarEstadoEmpleado(
        id: Int,
        activo: Boolean
    ): ResultadoEmpleado {

        val db = databaseHelper.writableDatabase

        val valores = ContentValues().apply {
            put("usuario_activo", if (activo) 1 else 0)
        }

        val filas = db.update(
            "usuarios",
            valores,
            "usuario_id = ? AND rol_id = 2",
            arrayOf(id.toString())
        )

        return if (filas > 0) {
            ResultadoEmpleado.Exitoso(
                if (activo) "Empleado activado correctamente"
                else "Empleado desactivado correctamente"
            )
        } else {
            ResultadoEmpleado.Error("No se pudo cambiar el estado del empleado")
        }
    }

    private fun nombresValidos(vararg nombres: String): Boolean {
        val soloLetras =
            Regex("^[\\p{L}]+(?:[ '\\u002D][\\p{L}]+)*$")

        return nombres.all { soloLetras.matches(it) }
    }

    private fun passwordValida(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() }
    }
}

sealed class ResultadoEmpleado {
    data class Exitoso(val mensaje: String) : ResultadoEmpleado()
    data class Error(val mensaje: String) : ResultadoEmpleado()
}
