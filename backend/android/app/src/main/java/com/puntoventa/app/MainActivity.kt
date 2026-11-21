
package com.puntoventa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.puntoventa.app.database.DatabaseHelper
import com.puntoventa.app.repositories.AuthRepository
import com.puntoventa.app.repositories.ResultadoLogin
import com.puntoventa.app.ui.theme.PuntoVentaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepository = AuthRepository(DatabaseHelper(this))

        setContent {
            PuntoVentaTheme {

                var correo by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var mensaje by remember { mutableStateOf("") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            text = "PuntoVenta",
                            style = MaterialTheme.typography.headlineLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Inicia sesión en tu cuenta")

                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value = correo,
                            onValueChange = { correo = it },
                            label = { Text("Correo electrónico") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation =
                                PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (correo.isBlank() || password.isBlank()) {
                                    mensaje = "Completa todos los campos"
                                } else {
                                    when (
                                        val resultado = authRepository.iniciarSesion(
                                            correo.trim(),
                                            password
                                        )
                                    ) {
                                        is ResultadoLogin.Exitoso -> {
                                            mensaje = "¡Bienvenido! Rol: ${resultado.rol}"
                                        }

                                        is ResultadoLogin.Error -> {
                                            mensaje = resultado.mensaje
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Iniciar sesión")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
