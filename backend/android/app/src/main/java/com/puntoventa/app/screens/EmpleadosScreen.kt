
package com.puntoventa.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.platform.LocalContext
import com.puntoventa.app.database.DatabaseHelper
import com.puntoventa.app.repositories.UsuarioRepository
import com.puntoventa.app.repositories.ResultadoEmpleado

@Composable
fun EmpleadosScreen(
    onRegresar: () -> Unit
) {
    val context = LocalContext.current

    val repository = remember {
        UsuarioRepository(DatabaseHelper(context))
    }

    // Formulario de registro
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Datos de edición
    var empleadoEditandoId by remember { mutableStateOf<Int?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editApellido by remember { mutableStateOf("") }
    var editApellidoMaterno by remember { mutableStateOf("") }
    var editCorreo by remember { mutableStateOf("") }
    var editNombreUsuario by remember { mutableStateOf("") }
    var editPassword by remember { mutableStateOf("") }

    var mostrarDialogoEdicion by remember { mutableStateOf(false) }
    var mostrarPassword by remember { mutableStateOf(false) }
    var mostrarPasswordEdicion by remember { mutableStateOf(false) }

    var busqueda by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var operacionExitosa by remember { mutableStateOf(false) }
    var mensajeEdicion by remember { mutableStateOf("") }
    var edicionExitosa by remember { mutableStateOf(false) }

    var empleados by remember {
        mutableStateOf(repository.buscarEmpleados(""))
    }

    fun cargarEmpleados() {
        empleados = repository.buscarEmpleados(busqueda)
    }

    fun limpiarFormulario() {
        nombre = ""
        apellido = ""
        apellidoMaterno = ""
        correo = ""
        nombreUsuario = ""
        password = ""
        mostrarPassword = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Gestión de empleados",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Registra, consulta y administra las cuentas de empleados.")

        HorizontalDivider()

        Text(
            "Registrar empleado",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido paterno") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = apellidoMaterno,
            onValueChange = { apellidoMaterno = it },
            label = { Text("Apellido materno") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nombreUsuario,
            onValueChange = { nombreUsuario = it },
            label = { Text("Nombre de usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (mostrarPassword)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = {
                    mostrarPassword = !mostrarPassword
                }) {
                    Icon(
                        imageVector = if (mostrarPassword)
                            Icons.Filled.VisibilityOff
                        else
                            Icons.Filled.Visibility,
                        contentDescription = "Mostrar u ocultar contraseña"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                when (
                    val resultado = repository.crearEmpleado(
                        nombre = nombre,
                        apellido = apellido,
                        apellidoMaterno = apellidoMaterno,
                        correo = correo,
                        nombreUsuario = nombreUsuario,
                        password = password
                    )
                ) {
                    is ResultadoEmpleado.Exitoso -> {
                        mensaje = resultado.mensaje
                        operacionExitosa = true
                        limpiarFormulario()
                        cargarEmpleados()
                    }

                    is ResultadoEmpleado.Error -> {
                        mensaje = resultado.mensaje
                        operacionExitosa = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar empleado")
        }

        if (mensaje.isNotEmpty()) {
            Text(
                mensaje,
                color = if (operacionExitosa)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }

        HorizontalDivider()

        Text(
            "Empleados registrados (${empleados.size})",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = busqueda,
            onValueChange = {
                busqueda = it
                cargarEmpleados()
            },
            label = { Text("Buscar empleado") },
            placeholder = { Text("Nombre, correo o usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (empleados.isEmpty()) {
            Text(
                if (busqueda.isBlank())
                    "Todavía no hay empleados registrados."
                else
                    "No se encontraron empleados."
            )
        } else {
            empleados.forEach { empleado ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        Text(
                            listOfNotNull(
                                empleado.nombre,
                                empleado.apellido,
                                empleado.apellidoMaterno
                            ).joinToString(" "),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text("Usuario: ${empleado.nombreUsuario ?: "Sin usuario"}")
                        Text("Correo: ${empleado.correo}")

                        Text(
                            if (empleado.activo == 1)
                                "Estado: Activo"
                            else
                                "Estado: Inactivo",
                            color = if (empleado.activo == 1)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )

                        OutlinedButton(
                            onClick = {
                                empleadoEditandoId = empleado.id
                                editNombre = empleado.nombre.orEmpty()
                                editApellido = empleado.apellido.orEmpty()
                                editApellidoMaterno = empleado.apellidoMaterno.orEmpty()
                                editCorreo = empleado.correo
                                editNombreUsuario = empleado.nombreUsuario.orEmpty()
                                editPassword = ""

                                mensajeEdicion = ""
                                edicionExitosa = false
                                mostrarPasswordEdicion = false
                                mostrarDialogoEdicion = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Editar empleado")
                        }

                        OutlinedButton(
                            onClick = {
                                val nuevoEstado = empleado.activo != 1

                                when (
                                    val resultado = repository.cambiarEstadoEmpleado(
                                        empleado.id,
                                        nuevoEstado
                                    )
                                ) {
                                    is ResultadoEmpleado.Exitoso -> {
                                        mensaje = resultado.mensaje
                                        operacionExitosa = true
                                        cargarEmpleados()
                                    }

                                    is ResultadoEmpleado.Error -> {
                                        mensaje = resultado.mensaje
                                        operacionExitosa = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (empleado.activo == 1)
                                    "Desactivar cuenta"
                                else
                                    "Activar cuenta"
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Regresar al menú")
        }
    }

    // Ventana emergente para editar
    if (mostrarDialogoEdicion) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoEdicion = false
                empleadoEditandoId = null
            },
            title = {
                Text("Editar empleado")
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedTextField(
                        value = editNombre,
                        onValueChange = { editNombre = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editApellido,
                        onValueChange = { editApellido = it },
                        label = { Text("Apellido paterno") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editApellidoMaterno,
                        onValueChange = { editApellidoMaterno = it },
                        label = { Text("Apellido materno") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editCorreo,
                        onValueChange = { editCorreo = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editNombreUsuario,
                        onValueChange = { editNombreUsuario = it },
                        label = { Text("Nombre de usuario") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPassword,
                        onValueChange = { editPassword = it },
                        label = { Text("Nueva contraseña (opcional)") },
                        supportingText = {
                            Text("Déjala vacía si no deseas cambiarla.")
                        },
                        singleLine = true,
                        visualTransformation = if (mostrarPasswordEdicion)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = {
                                mostrarPasswordEdicion = !mostrarPasswordEdicion
                            }) {
                                Icon(
                                    imageVector = if (mostrarPasswordEdicion)
                                        Icons.Filled.VisibilityOff
                                    else
                                        Icons.Filled.Visibility,
                                    contentDescription = "Mostrar u ocultar contraseña"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (mensajeEdicion.isNotEmpty()) {
                        Text(
                            mensajeEdicion,
                            color = if (edicionExitosa)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = empleadoEditandoId

                        if (id != null) {
                            when (
                                val resultado = repository.actualizarEmpleado(
                                    id = id,
                                    nombre = editNombre,
                                    apellido = editApellido,
                                    apellidoMaterno = editApellidoMaterno,
                                    correo = editCorreo,
                                    nombreUsuario = editNombreUsuario,
                                    password = editPassword
                                )
                            ) {
                                is ResultadoEmpleado.Exitoso -> {
                                    mensaje = resultado.mensaje
                                    operacionExitosa = true
                                    cargarEmpleados()

                                    mostrarDialogoEdicion = false
                                    empleadoEditandoId = null
                                    mensajeEdicion = ""
                                }

                                is ResultadoEmpleado.Error -> {
                                    mensajeEdicion = resultado.mensaje
                                    edicionExitosa = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Guardar cambios")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEdicion = false
                        empleadoEditandoId = null
                        mensajeEdicion = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
