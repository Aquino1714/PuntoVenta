
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.puntoventa.app.database.DatabaseHelper
import com.puntoventa.app.repositories.AuthRepository
import com.puntoventa.app.repositories.ResultadoLogin
import com.puntoventa.app.repositories.ResultadoRegistro
import com.puntoventa.app.services.Permisos
import com.puntoventa.app.ui.theme.PuntoVentaTheme
import com.puntoventa.app.screens.EmpleadosScreen
import com.puntoventa.app.screens.NegociosScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepository = AuthRepository(DatabaseHelper(this))

        setContent {
            PuntoVentaTheme {

                var pantalla by remember { mutableStateOf("inicio") }
                var moduloSeleccionado by remember { mutableStateOf("Inicio") }

                var correo by remember { mutableStateOf("") }
                var usuario by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var nombre by remember { mutableStateOf("") }
                var apellido by remember { mutableStateOf("") }
                var apellidoMaterno by remember { mutableStateOf("") }
                var confirmarPassword by remember { mutableStateOf("") }

                var mostrarPasswordLogin by remember { mutableStateOf(false) }
                var mostrarPasswordRegistro by remember { mutableStateOf(false) }

                var mensaje by remember { mutableStateOf("") }
                var registroExitoso by remember { mutableStateOf(false) }
                var correoSesion by remember { mutableStateOf("") }
                var rolSesion by remember { mutableIntStateOf(0) }
                var idSesion by remember { mutableIntStateOf(0) }

                var errorNombre by remember { mutableStateOf("") }
                var errorApellido by remember { mutableStateOf("") }
                var errorApellidoMaterno by remember { mutableStateOf("") }
                var errorCorreo by remember { mutableStateOf("") }
                var errorPassword by remember { mutableStateOf("") }
                var errorConfirmarPassword by remember { mutableStateOf("") }

                fun cerrarSesion() {
                    correoSesion = ""
                    idSesion = 0
                    rolSesion = 0
                    usuario = ""
                    password = ""
                    mensaje = ""
                    moduloSeleccionado = "Inicio"
                    mostrarPasswordLogin = false
                    pantalla = "inicio"
                }

                fun iniciarSesion(tipoRol: Int) {
                    val identificador =
                        if (tipoRol == Permisos.ROL_ADMIN) correo.trim()
                        else usuario.trim()

                    if (identificador.isBlank() || password.isBlank()) {
                        mensaje = "Completa todos los campos"
                        return
                    }

                    when (
                        val resultado = authRepository.iniciarSesion(
                            identificador,
                            password
                        )
                    ) {
                        is ResultadoLogin.Exitoso -> {
                            if (resultado.rolId != tipoRol) {
                                mensaje = "Esta cuenta no corresponde a este acceso"
                            } else {
                                idSesion = resultado.id
                                correoSesion = resultado.correo
                                rolSesion = resultado.rolId
                                password = ""
                                mensaje = ""
                                moduloSeleccionado = "Inicio"
                                mostrarPasswordLogin = false
                                pantalla = "menu"
                            }
                        }

                        is ResultadoLogin.Error -> {
                            mensaje = resultado.mensaje
                        }
                    }
                }

                fun validarFormularioRegistro(): Boolean {
                    errorNombre = ""
                    errorApellido = ""
                    errorApellidoMaterno = ""
                    errorCorreo = ""
                    errorPassword = ""
                    errorConfirmarPassword = ""

                    val soloLetras =
                        Regex("^[\\p{L}]+(?:[ '\\u002D][\\p{L}]+)*$")

                    if (nombre.isBlank()) {
                        errorNombre = "Ingresa tu nombre"
                    } else if (!soloLetras.matches(nombre.trim())) {
                        errorNombre = "Solo se permiten letras"
                    }

                    if (apellido.isBlank()) {
                        errorApellido = "Ingresa tu apellido paterno"
                    } else if (!soloLetras.matches(apellido.trim())) {
                        errorApellido = "Apellido no válido"
                    }

                    if (apellidoMaterno.isBlank()) {
                        errorApellidoMaterno = "Ingresa tu apellido materno"
                    } else if (!soloLetras.matches(apellidoMaterno.trim())) {
                        errorApellidoMaterno = "Apellido no válido"
                    }

                    if (correo.isBlank()) {
                        errorCorreo = "Ingresa tu correo"
                    } else if (
                        !android.util.Patterns.EMAIL_ADDRESS
                            .matcher(correo.trim()).matches()
                    ) {
                        errorCorreo = "Ingresa un correo válido"
                    }

                    if (password.length < 8) {
                        errorPassword = "Mínimo 8 caracteres"
                    } else if (!password.any { it.isUpperCase() }) {
                        errorPassword = "Agrega una letra mayúscula"
                    } else if (!password.any { it.isLowerCase() }) {
                        errorPassword = "Agrega una letra minúscula"
                    } else if (!password.any { it.isDigit() }) {
                        errorPassword = "Agrega un número"
                    }

                    if (confirmarPassword.isBlank()) {
                        errorConfirmarPassword = "Confirma tu contraseña"
                    } else if (password != confirmarPassword) {
                        errorConfirmarPassword = "Las contraseñas no coinciden"
                    }

                    return errorNombre.isEmpty() &&
                            errorApellido.isEmpty() &&
                            errorApellidoMaterno.isEmpty() &&
                            errorCorreo.isEmpty() &&
                            errorPassword.isEmpty() &&
                            errorConfirmarPassword.isEmpty()
                }

                fun registrarAdministrador() {
                    if (!validarFormularioRegistro()) {
                        registroExitoso = false
                        return
                    }

                    when (
                        val resultado = authRepository.registrarAdministrador(
                            nombre = nombre,
                            apellido = apellido,
                            apellidoMaterno = apellidoMaterno,
                            correo = correo,
                            password = password
                        )
                    ) {
                        is ResultadoRegistro.Exitoso -> {
                            mensaje = resultado.mensaje
                            registroExitoso = true
                            nombre = ""
                            apellido = ""
                            apellidoMaterno = ""
                            password = ""
                            confirmarPassword = ""
                        }

                        is ResultadoRegistro.Error -> {
                            mensaje = resultado.mensaje
                            registroExitoso = false
                        }
                    }
                }

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

                        when (pantalla) {

                            // INICIO
                            "inicio" -> {
                                Text(
                                    "MY BUSINESS",
                                    style = MaterialTheme.typography.headlineLarge
                                )

                                Spacer(Modifier.height(16.dp))
                                Text("Todos tus negocios en una App")

                                Spacer(Modifier.height(32.dp))
                                Text("¿Eres administrador?")

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        pantalla = "admin"
                                        mensaje = ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Iniciar sesión como administrador")
                                }

                                OutlinedButton(
                                    onClick = {
                                        pantalla = "registro"
                                        mensaje = ""
                                        registroExitoso = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Crear cuenta")
                                }

                                Spacer(Modifier.height(24.dp))
                                Text("¿Eres empleado?")

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        pantalla = "empleado"
                                        mensaje = ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Iniciar sesión con cuenta empleado")
                                }

                                Spacer(Modifier.height(24.dp))

                                OutlinedButton(
                                    onClick = {
                                        finish()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Salir de la aplicación")
                                }
                            }

                            // LOGIN ADMINISTRADOR
                            "admin" -> {
                                Text(
                                    "Iniciar sesión",
                                    style = MaterialTheme.typography.headlineMedium
                                )

                                Spacer(Modifier.height(24.dp))

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

                                Spacer(Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Contraseña") },
                                    singleLine = true,
                                    visualTransformation = if (mostrarPasswordLogin)
                                        VisualTransformation.None
                                    else
                                        PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            mostrarPasswordLogin = !mostrarPasswordLogin
                                        }) {
                                            Icon(
                                                imageVector = if (mostrarPasswordLogin)
                                                    Icons.Filled.VisibilityOff
                                                else
                                                    Icons.Filled.Visibility,
                                                contentDescription = "Mostrar u ocultar contraseña"
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                TextButton(
                                    onClick = {
                                        mensaje = "Contacta al administrador para recuperar tu contraseña"
                                    }
                                ) {
                                    Text("¿Olvidaste tu contraseña?")
                                }

                                Button(
                                    onClick = {
                                        iniciarSesion(Permisos.ROL_ADMIN)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Iniciar sesión")
                                }

                                OutlinedButton(
                                    onClick = {
                                        mensaje = "El acceso con Google requiere configuración adicional"
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Iniciar con Google")
                                }

                                TextButton(
                                    onClick = {
                                        pantalla = "inicio"
                                        mensaje = ""
                                    }
                                ) {
                                    Text("Regresar")
                                }

                                Text(
                                    mensaje,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            // REGISTRO ADMINISTRADOR
                            "registro" -> {
                                Text(
                                    "Crear cuenta",
                                    style = MaterialTheme.typography.headlineMedium
                                )

                                Spacer(Modifier.height(20.dp))

                                OutlinedTextField(
                                    value = nombre,
                                    onValueChange = {
                                        nombre = it
                                        errorNombre = ""
                                    },
                                    label = { Text("Nombre") },
                                    singleLine = true,
                                    isError = errorNombre.isNotEmpty(),
                                    supportingText = {
                                        if (errorNombre.isNotEmpty()) Text(errorNombre)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = apellido,
                                    onValueChange = {
                                        apellido = it
                                        errorApellido = ""
                                    },
                                    label = { Text("Apellido paterno") },
                                    singleLine = true,
                                    isError = errorApellido.isNotEmpty(),
                                    supportingText = {
                                        if (errorApellido.isNotEmpty()) Text(errorApellido)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = apellidoMaterno,
                                    onValueChange = {
                                        apellidoMaterno = it
                                        errorApellidoMaterno = ""
                                    },
                                    label = { Text("Apellido materno") },
                                    singleLine = true,
                                    isError = errorApellidoMaterno.isNotEmpty(),
                                    supportingText = {
                                        if (errorApellidoMaterno.isNotEmpty()) Text(errorApellidoMaterno)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = correo,
                                    onValueChange = {
                                        correo = it
                                        errorCorreo = ""
                                    },
                                    label = { Text("Correo electrónico") },
                                    singleLine = true,
                                    isError = errorCorreo.isNotEmpty(),
                                    supportingText = {
                                        if (errorCorreo.isNotEmpty()) Text(errorCorreo)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email
                                    )
                                )

                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        errorPassword = ""
                                    },
                                    label = { Text("Contraseña") },
                                    singleLine = true,
                                    isError = errorPassword.isNotEmpty(),
                                    supportingText = {
                                        if (errorPassword.isNotEmpty()) Text(errorPassword)
                                    },
                                    visualTransformation = if (mostrarPasswordRegistro)
                                        VisualTransformation.None
                                    else
                                        PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                mostrarPasswordRegistro =
                                                    !mostrarPasswordRegistro
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (mostrarPasswordRegistro)
                                                    Icons.Filled.VisibilityOff
                                                else
                                                    Icons.Filled.Visibility,
                                                contentDescription = "Mostrar u ocultar contraseña"
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = confirmarPassword,
                                    onValueChange = {
                                        confirmarPassword = it
                                        errorConfirmarPassword = ""
                                    },
                                    label = { Text("Confirmar contraseña") },
                                    singleLine = true,
                                    isError = errorConfirmarPassword.isNotEmpty(),
                                    supportingText = {
                                        if (errorConfirmarPassword.isNotEmpty()) Text(errorConfirmarPassword)
                                    },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        registrarAdministrador()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Crear cuenta")
                                }

                                OutlinedButton(
                                    onClick = {
                                        mensaje = "El registro con Google requiere configuración adicional"
                                        registroExitoso = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Registrarse con Google")
                                }

                                TextButton(
                                    onClick = {
                                        pantalla = "inicio"
                                        mensaje = ""
                                    }
                                ) {
                                    Text("Regresar")
                                }

                                Text(
                                    mensaje,
                                    color = if (registroExitoso)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
                                )

                                if (registroExitoso) {
                                    TextButton(
                                        onClick = {
                                            correo = correo.trim()
                                            pantalla = "admin"
                                            mensaje = "Tu cuenta fue creada. Inicia sesión."
                                            registroExitoso = false
                                        }
                                    ) {
                                        Text("Ir a iniciar sesión")
                                    }
                                }
                            }

                            // LOGIN EMPLEADO
                            "empleado" -> {
                                Text(
                                    "Acceso de empleado",
                                    style = MaterialTheme.typography.headlineMedium
                                )

                                Spacer(Modifier.height(24.dp))

                                OutlinedTextField(
                                    value = usuario,
                                    onValueChange = { usuario = it },
                                    label = { Text("Nombre de usuario") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Contraseña") },
                                    singleLine = true,
                                    visualTransformation = if (mostrarPasswordLogin)
                                        VisualTransformation.None
                                    else
                                        PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            mostrarPasswordLogin = !mostrarPasswordLogin
                                        }) {
                                            Icon(
                                                imageVector = if (mostrarPasswordLogin)
                                                    Icons.Filled.VisibilityOff
                                                else
                                                    Icons.Filled.Visibility,
                                                contentDescription = "Mostrar u ocultar contraseña"
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        iniciarSesion(Permisos.ROL_EMPLEADO)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Iniciar sesión")
                                }

                                TextButton(
                                    onClick = {
                                        pantalla = "inicio"
                                        mensaje = ""
                                    }
                                ) {
                                    Text("Regresar")
                                }

                                Text(
                                    mensaje,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            // MENU PRINCIPAL
                            "menu" -> {
                                val esAdmin = Permisos.esAdministrador(rolSesion)

                                Text(
                                    "¡Bienvenido!",
                                    style = MaterialTheme.typography.headlineLarge
                                )

                                Spacer(Modifier.height(8.dp))
                                Text(correoSesion)
                                Text(if (esAdmin) "Administrador" else "Empleado")

                                Spacer(Modifier.height(24.dp))

                                Text(
                                    moduloSeleccionado,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Spacer(Modifier.height(16.dp))

                                when (moduloSeleccionado) {

                                    "Inicio" -> {
                                        Text("Todos tus negocios en una App")
                                        Text("Selecciona una sección desde el menú inferior.")
                                    }

                                    "Usuarios" -> {
                                        Text("Administración de usuarios")

                                        Button(
                                            onClick = {
                                                pantalla = "empleados"
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.People,
                                                contentDescription = null
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text("Gestionar empleados")
                                        }
                                    }

                                    "Negocios" -> {
                                        NegociosScreen(
                                            administradorId = idSesion,
                                            onRegresar = {
                                                moduloSeleccionado = "Inicio"
                                                mensaje = ""
                                            }
                                        )
                                    }

                                    "Productos" -> {
                                        Text("Productos e inventario: módulo en desarrollo")
                                    }

                                    "Ventas" -> {
                                        Text("Ventas: módulo en desarrollo")
                                    }

                                    "Más" -> {
                                        if (esAdmin) {
                                            val otrosModulos = listOf(
                                                "Clientes",
                                                "Finanzas",
                                                "Reportes"
                                            )

                                            otrosModulos.forEach { modulo ->
                                                OutlinedButton(
                                                    onClick = {
                                                        mensaje = "$modulo: módulo en desarrollo"
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(modulo)
                                                }
                                            }
                                        } else {
                                            Text("No hay más opciones disponibles.")
                                        }
                                    }

                                    "Perfil" -> {
                                        Text("Tu cuenta")
                                        Text(correoSesion)

                                        Button(
                                            onClick = { cerrarSesion() },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Cerrar sesión")
                                        }
                                    }

                                    else -> {
                                        Text("$moduloSeleccionado: módulo en desarrollo")
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                Text(mensaje)

                                NavigationBar {
                                    val opciones = if (esAdmin) {
                                        listOf(
                                            "Inicio",
                                            "Usuarios",
                                            "Negocios",
                                            "Más",
                                            "Perfil"
                                        )
                                    } else {
                                        listOf(
                                            "Inicio",
                                            "Productos",
                                            "Ventas",
                                            "Perfil"
                                        )
                                    }

                                    val iconos = mapOf(
                                        "Inicio" to Icons.Filled.Home,
                                        "Usuarios" to Icons.Filled.People,
                                        "Negocios" to Icons.Filled.Store,
                                        "Productos" to Icons.Filled.Inventory,
                                        "Ventas" to Icons.Filled.ShoppingCart,
                                        "Más" to Icons.Filled.MoreHoriz,
                                        "Perfil" to Icons.Filled.Person
                                    )

                                    opciones.forEach { opcion ->
                                        NavigationBarItem(
                                            selected = moduloSeleccionado == opcion,
                                            onClick = {
                                                moduloSeleccionado = opcion
                                                mensaje = ""
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = iconos[opcion]
                                                        ?: Icons.Filled.Home,
                                                    contentDescription = opcion
                                                )
                                            },
                                            label = { Text(opcion) }
                                        )
                                    }
                                }
                            }

                            // EMPLEADOS
                            "empleados" -> {
                                EmpleadosScreen(
                                    administradorId = idSesion,
                                    onRegresar = {
                                        pantalla = "menu"
                                        moduloSeleccionado = "Usuarios"
                                        mensaje = ""
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
