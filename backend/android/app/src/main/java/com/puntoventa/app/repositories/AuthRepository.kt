
package com.puntoventa.app.repositories

import com.puntoventa.app.database.DatabaseHelper

class AuthRepository(
    private val databaseHelper: DatabaseHelper
) {

    fun iniciarSesion(
        correo: String,
        password: String
    ): ResultadoLogin {

        val db = databaseHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                usuario_id,
                usuario_correo,
                usuario_password,
                usuario_rol,
                usuario_activo
            FROM usuarios
            WHERE usuario_correo = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(correo)
        )

        cursor.use {
            if (!it.moveToFirst()) {
                return ResultadoLogin.Error("Correo no registrado")
            }

            val id = it.getInt(0)
            val correoUsuario = it.getString(1)
            val passwordGuardada = it.getString(2)
            val rol = it.getString(3)
            val activo = it.getInt(4)

            if (activo != 1) {
                return ResultadoLogin.Error("La cuenta está inactiva")
            }

            if (passwordGuardada != password) {
                return ResultadoLogin.Error("Contraseña incorrecta")
            }

            return ResultadoLogin.Exitoso(
                id,
                correoUsuario,
                rol
            )
        }
    }
}

sealed class ResultadoLogin {

    data class Exitoso(
        val id: Int,
        val correo: String,
        val rol: String
    ) : ResultadoLogin()

    data class Error(
        val mensaje: String
    ) : ResultadoLogin()
}
