
package com.puntoventa.app.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "punto_venta.db"
        private const val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase) {
        // La base existente se copia desde assets.
     
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        //  migraciones cuando sean necesarias.
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
