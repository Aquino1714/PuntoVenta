
package com.puntoventa.app.repositories

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
            SELECT
                usuario_id,
                usuario_correo,
                usuario_password,
                usuario_rol,
                usuario_activo
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
                        rol = it.getString(3),
                        activo = it.getInt(4)
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
            if (it.moveToFirst()) {
                return Usuario(
                    id = it.getInt(0),
                    correo = it.getString(1),
                    password = it.getString(2),
                    rol = it.getString(3),
                    activo = it.getInt(4)
                )
            }
        }

        return null
    }
}
