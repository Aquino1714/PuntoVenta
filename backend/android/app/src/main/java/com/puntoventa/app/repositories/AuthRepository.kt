
package com.puntoventa.app.repositories

import android.content.ContentValues
import android.util.Patterns
import com.puntoventa.app.database.DatabaseHelper
import com.puntoventa.app.services.PasswordHasher

class AuthRepository(
    private val databaseHelper: DatabaseHelper
) {

    fun iniciarSesion(
        identificador: String,
        password: String
    ): ResultadoLogin {

        val db = databaseHelper.readableDatabase

        var id = 0
        var correo = ""
        var passwordGuardada = ""
        var rolId = 0
        var activo = 0

        db.rawQuery(
            """
            SELECT usuario_id, usuario_correo, usuario_password,
                   rol_id, usuario_activo
            FROM usuarios
            WHERE (usuario_correo = ? OR usuario_nombre_usuario = ?)
            LIMIT 1
            """.trimIndent(),
            arrayOf(identificador.trim(), identificador.trim())
        ).use { cursor ->

            if (!cursor.moveToFirst()) {
                return ResultadoLogin.Error("Usuario no registrado")
            }

            id = cursor.getInt(0)
            correo = cursor.getString(1)
            passwordGuardada = cursor.getString(2)
            rolId = cursor.getInt(3)
            activo = cursor.getInt(4)
        }

        if (activo != 1) {
            return ResultadoLogin.Error("La cuenta está inactiva")
        }

        // 1. Verificar contraseña según el formato almacenado.
        val esHash = PasswordHasher.esHash(passwordGuardada)

        val passwordCorrecta = if (esHash) {
            PasswordHasher.verificarPassword(
                password,
                passwordGuardada
            )
        } else {
            // Compatibilidad con cuentas antiguas.
            passwordGuardada == password
        }

        if (!passwordCorrecta) {
            return ResultadoLogin.Error("Contraseña incorrecta")
        }

        // 2. Si era una contraseña antigua, actualizarla a hash.
        if (!esHash) {
            val valores = ContentValues().apply {
                put(
                    "usuario_password",
                    PasswordHasher.generarHash(password)
                )
            }

            val filasActualizadas = databaseHelper.writableDatabase.update(
                "usuarios",
                valores,
                "usuario_id = ?",
                arrayOf(id.toString())
            )

            if (filasActualizadas != 1) {
                return ResultadoLogin.Error(
                    "No se pudo actualizar la seguridad de la contraseña"
                )
            }
        }

        return ResultadoLogin.Exitoso(id, correo, rolId)
    }

    fun registrarAdministrador(
        nombre: String,
        apellido: String,
        apellidoMaterno: String,
        correo: String,
        password: String,
        nombreNegocio: String
    ): ResultadoRegistro {

        val nombreLimpio = nombre.trim()
        val apellidoLimpio = apellido.trim()
        val apellidoMaternoLimpio = apellidoMaterno.trim()
        val correoLimpio = correo.trim().lowercase()
        val negocioLimpio = nombreNegocio.trim()

        if (
            nombreLimpio.isBlank() ||
            apellidoLimpio.isBlank() ||
            apellidoMaternoLimpio.isBlank() ||
            correoLimpio.isBlank() ||
            password.isBlank() ||
            negocioLimpio.isBlank()
        ) {
            return ResultadoRegistro.Error("Todos los campos son obligatorios")
        }

        val soloLetras = Regex("^[\\p{L}]+(?:[ '\\u002D][\\p{L}]+)*$")

        if (!soloLetras.matches(nombreLimpio)) {
            return ResultadoRegistro.Error("El nombre solo debe contener letras")
        }

        if (!soloLetras.matches(apellidoLimpio)) {
            return ResultadoRegistro.Error("El apellido paterno no es válido")
        }

        if (!soloLetras.matches(apellidoMaternoLimpio)) {
            return ResultadoRegistro.Error("El apellido materno no es válido")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            return ResultadoRegistro.Error("Ingresa un correo electrónico válido")
        }

        if (password.length < 8) {
            return ResultadoRegistro.Error(
                "La contraseña debe tener al menos 8 caracteres"
            )
        }

        if (!password.any { it.isUpperCase() }) {
            return ResultadoRegistro.Error(
                "La contraseña debe contener una mayúscula"
            )
        }

        if (!password.any { it.isLowerCase() }) {
            return ResultadoRegistro.Error(
                "La contraseña debe contener una minúscula"
            )
        }

        if (!password.any { it.isDigit() }) {
            return ResultadoRegistro.Error(
                "La contraseña debe contener un número"
            )
        }

        val db = databaseHelper.writableDatabase
        db.beginTransaction()

        try {
            val cantidadAdministradores = db.rawQuery(
                "SELECT COUNT(*) FROM usuarios WHERE rol_id = 1",
                null
            ).use {
                if (it.moveToFirst()) it.getInt(0) else 0
            }

            if (cantidadAdministradores > 0) {
                return ResultadoRegistro.Error(
                    "Ya existe un administrador. Solicita autorización."
                )
            }

            val existeCorreo = db.rawQuery(
                """
                SELECT usuario_id FROM usuarios
                WHERE LOWER(usuario_correo) = ?
                LIMIT 1
                """.trimIndent(),
                arrayOf(correoLimpio)
            ).use {
                it.moveToFirst()
            }

            if (existeCorreo) {
                return ResultadoRegistro.Error(
                    "Este correo ya está registrado"
                )
            }

            // 1. Crear administrador con contraseña protegida.
            val valoresUsuario = ContentValues().apply {
                put("usuario_correo", correoLimpio)
                put("usuario_password", PasswordHasher.generarHash(password))
                put("rol_id", 1)
                put("usuario_activo", 1)
                put("usuario_nombre", nombreLimpio)
                put("usuario_apellido", apellidoLimpio)
                put("usuario_apellido_materno", apellidoMaternoLimpio)
            }

            val nuevoId = db.insert("usuarios", null, valoresUsuario)

            if (nuevoId == -1L) {
                return ResultadoRegistro.Error(
                    "No se pudo crear la cuenta"
                )
            }

            // 2. Crear negocio principal del administrador.
            val valoresNegocio = ContentValues().apply {
                put("negocio_nombre_comercial", negocioLimpio)
                put("negocio_es_principal", 1)
                put("negocio_activo", 1)
                put("negocio_propietario_id", nuevoId)
            }

            val nuevoNegocioId = db.insert(
                "negocios",
                null,
                valoresNegocio
            )

            if (nuevoNegocioId == -1L) {
                return ResultadoRegistro.Error(
                    "No se pudo crear el negocio"
                )
            }

            // 3. Relacionar administrador con su negocio.
            val actualizarUsuario = ContentValues().apply {
                put("negocio_id", nuevoNegocioId)
            }

            val filasActualizadas = db.update(
                "usuarios",
                actualizarUsuario,
                "usuario_id = ?",
                arrayOf(nuevoId.toString())
            )

            if (filasActualizadas != 1) {
                return ResultadoRegistro.Error(
                    "No se pudo relacionar el administrador con su negocio"
                )
            }

            db.setTransactionSuccessful()

            return ResultadoRegistro.Exitoso(
                "Administrador y negocio principal creados correctamente"
            )

        } catch (e: Exception) {
            return ResultadoRegistro.Error(
                "Error al crear la cuenta: ${e.message}"
            )
        } finally {
            db.endTransaction()
        }
    }
}

sealed class ResultadoLogin {
    data class Exitoso(
        val id: Int,
        val correo: String,
        val rolId: Int
    ) : ResultadoLogin()

    data class Error(
        val mensaje: String
    ) : ResultadoLogin()
}

sealed class ResultadoRegistro {
    data class Exitoso(
        val mensaje: String
    ) : ResultadoRegistro()

    data class Error(
        val mensaje: String
    ) : ResultadoRegistro()
}
