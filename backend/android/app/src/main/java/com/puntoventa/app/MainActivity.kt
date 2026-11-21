
package com.puntoventa.app

import android.os.Bundle
import android.database.sqlite.SQLiteDatabase
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.puntoventa.app.database.DatabaseHelper
import com.puntoventa.app.ui.theme.PuntoVentaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resultado = try {
            val helper = DatabaseHelper(this)
            val db = helper.readableDatabase

            val cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name",
                null
            )

            val tablas = mutableListOf<String>()

            cursor.use {
                while (it.moveToNext()) {
                    tablas.add(it.getString(0))
                }
            }

            "Conexión exitosa\n\nTablas encontradas: ${tablas.size}\n\n" +
                    tablas.joinToString("\n")

        } catch (e: Exception) {
            "Error al conectar SQLite:\n${e.message}"
        }

        setContent {
            PuntoVentaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Prueba de SQLite",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(resultado)
                    }
                }
            }
        }
    }
}
