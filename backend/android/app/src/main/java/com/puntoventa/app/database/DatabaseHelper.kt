
package com.puntoventa.app.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "punto_venta.db"
        private const val DATABASE_VERSION = 3
    }

    override fun onCreate(db: SQLiteDatabase) {
        // La base existente se copia desde assets.
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 3) {
            var existeNegocioId = false

            db.rawQuery("PRAGMA table_info(usuarios)", null).use { cursor ->
                val indiceNombre = cursor.getColumnIndexOrThrow("name")

                while (cursor.moveToNext()) {
                    if (cursor.getString(indiceNombre) == "negocio_id") {
                        existeNegocioId = true
                        break
                    }
                }
            }

            if (!existeNegocioId) {
                db.execSQL(
                    """
                    ALTER TABLE usuarios
                    ADD COLUMN negocio_id INTEGER
                    REFERENCES negocios(negocio_id)
                    """.trimIndent()
                )
            }
        }
    }

    init {
        val dbFile = context.getDatabasePath(DATABASE_NAME)

        if (!dbFile.exists()) {
            copiarBaseDeDatos(context, dbFile)
        }
    }

    private fun copiarBaseDeDatos(context: Context, destino: File) {
        destino.parentFile?.mkdirs()

        context.assets.open(DATABASE_NAME).use { input ->
            destino.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }
}
