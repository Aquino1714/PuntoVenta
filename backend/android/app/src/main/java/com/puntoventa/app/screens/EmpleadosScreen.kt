
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
import com.puntoventa.app.repositories.NegocioRepository
import com.puntoventa.app.repositories.ResultadoEmpleado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadosScreen(
    administradorId: Int,
    onRegresar: () -> Unit
) {
    val context = LocalContext.current

    val usuarioRepository = remember {
        UsuarioRepository(DatabaseHelper(context))
    }

    val negocioRepository = remember {
        NegocioRepository(DatabaseHelper(context))
    }

    val negocios = remember(administradorId) {
        negocioRepository.obtenerActivosPorPropietario(administradorId)
    }

    var negocioSeleccionadoId by remember(negocios) {
        mutableIntStateOf(negocios.firstOrNull()?.id ?: 0)
    }

    var menuNegociosExpandido by remember {
        mutableStateOf(false)
    }

    val negocioSeleccionado = negocios.firstOrNull {
        it.id == negocioSeleccionadoId
    }

    // FORMULARIO
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // EDICIÓN
    var empleadoEditandoId by remember { mutableStateOf<Int?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editApellido by remember { mutableStateOf("") }
    var editApellidoMaterno by remember { mutableStateOf("") }
    var editCorreo by remember { mutableStateOf("") }
    var editNombreUsuario by remember { mutableStateOf("") }
    var editPassword by remember { mutableStateOf("") }
    var editNegocioId by remember { mutableIntStateOf(0) }
    var menuEdicionNegociosExpandido by remember { mutableStateOf(false) }

    var mostrarDialogoEdicion by remember { mutableStateOf(false) }
    var mostrarPassword by remember { mutableStateOf(false) }
    var mostrarPasswordEdicion by remember { mutableStateOf(false) }

    var busqueda by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var operacionExitosa by remember { mutableStateOf(false) }
    var mensajeEdicion by remember { mutableStateOf("") }
    var edicionExitosa by remember { mutableStateOf(false) }

    // EMPLEADOS DEL NEGOCIO SELECCIONADO
    var empleados by remember(negocioSeleccionadoId) {
        mutableStateOf(
            if (negocioSeleccionadoId > 0) {
                usuarioRepository.buscarEmpleados("", negocioSeleccionadoId)
            } else {
                emptyList()
            }
        )
    }

    fun cargarEmpleados() {
        empleados = if (negocioSeleccionadoId > 0) {
            usuarioRepository.buscarEmpleados(
                busqueda,
                negocioSeleccionadoId
            )
        } else {
            emptyList()
        }
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
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Gestión de empleados",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Registra, consulta y administra las cuentas de empleados.")

        HorizontalDivider()

        // FORMULARIO DE REGISTRO
        Text(
            "Registrar empleado",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            placeholder = { Text("Ej. Juan") },
            singleLine = true,
            enabled = negocios.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido paterno") },
            placeholder = { Text("Ej. Pérez") },
            singleLine = true,
            enabled = negocios.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = apellidoMaterno,
            onValueChange = { apellidoMaterno = it },
            label = { Text("Apellido materno") },
            placeholder = { Text("Ej. López") },
            singleLine = true,
            enabled = negocios.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            placeholder = { Text("Ej. empleado@correo.com") },
            singleLine = true,
            enabled = negocios.isNotEmpty(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nombreUsuario,
            onValueChange = { nombreUsuario = it },
            label = { Text("Nombre de usuario") },
            placeholder = { Text("Ej. juan123") },
            singleLine = true,
            enabled = negocios.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            placeholder = { Text("Mínimo 8 caracteres") },
            singleLine = true,
            enabled = negocios.isNotEmpty(),
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

        // NEGOCIO ASIGNADO AL NUEVO EMPLEADO
        Text(
            "Negocio al que pertenece",
            style = MaterialTheme.typography.titleMedium
        )

        if (negocios.isEmpty()) {
            Text(
                "No tienes negocios activos. Primero registra un negocio.",
                color = MaterialTheme.colorScheme.error
            )
        } else {
            ExposedDropdownMenuBox(
                expanded = menuNegociosExpandido,
                onExpandedChange = {
                    menuNegociosExpandido = !menuNegociosExpandido
                }
            ) {
                OutlinedTextField(
                    value = negocioSeleccionado?.nombreComercial ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar negocio") },
                    placeholder = { Text("Elige un negocio") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = menuNegociosExpandido
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenu(
                    expanded = menuNegociosExpandido,
                    onDismissRequest = {
                        menuNegociosExpandido = false
                    }
                ) {
                    negocios.forEach { negocio ->
                        DropdownMenuItem(
                            text = {
                                Text(negocio.nombreComercial)
                            },
                            onClick = {
                                negocioSeleccionadoId = negocio.id
                                busqueda = ""
                                mensaje = ""
                                menuNegociosExpandido = false
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                when (
                    val resultado = usuarioRepository.crearEmpleado(
                        nombre = nombre,
                        apellido = apellido,
                        apellidoMaterno = apellidoMaterno,
                        correo = correo,
                        nombreUsuario = nombreUsuario,
                        password = password,
                        negocioId = negocioSeleccionadoId
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
            enabled = negocioSeleccionadoId > 0,
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

        // BUSCADOR Y LISTA
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
            enabled = negocioSeleccionadoId > 0,
            modifier = Modifier.fillMaxWidth()
        )

        if (empleados.isEmpty()) {
            Text(
                if (negocioSeleccionadoId > 0)
                    "Todavía no hay empleados registrados en este negocio."
                else
                    "No hay un negocio seleccionado."
            )
        }

        empleados.forEach { empleado ->

            val negocioEmpleado = negocios.firstOrNull {
                it.id == empleado.negocioId
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

                    HorizontalDivider()

                    Text(
                        "Negocio: ${negocioEmpleado?.nombreComercial ?: "Sin negocio asignado"}",
                        style = MaterialTheme.typography.bodyMedium
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
                            editNegocioId = empleado.negocioId ?: negocioSeleccionadoId

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
                                val resultado = usuarioRepository.cambiarEstadoEmpleado(
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

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Regresar al menú")
        }
    }

    // VENTANA DE EDICIÓN
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
                        placeholder = { Text("Ej. Juan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editApellido,
                        onValueChange = { editApellido = it },
                        label = { Text("Apellido paterno") },
                        placeholder = { Text("Ej. Pérez") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editApellidoMaterno,
                        onValueChange = { editApellidoMaterno = it },
                        label = { Text("Apellido materno") },
                        placeholder = { Text("Ej. López") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editCorreo,
                        onValueChange = { editCorreo = it },
                        label = { Text("Correo electrónico") },
                        placeholder = { Text("Ej. empleado@correo.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editNombreUsuario,
                        onValueChange = { editNombreUsuario = it },
                        label = { Text("Nombre de usuario") },
                        placeholder = { Text("Ej. juan123") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // CAMBIAR NEGOCIO DEL EMPLEADO
                    Text(
                        "Negocio al que pertenece",
                        style = MaterialTheme.typography.titleMedium
                    )

                    ExposedDropdownMenuBox(
                        expanded = menuEdicionNegociosExpandido,
                        onExpandedChange = {
                            menuEdicionNegociosExpandido = !menuEdicionNegociosExpandido
                        }
                    ) {
                        OutlinedTextField(
                            value = negocios.firstOrNull {
                                it.id == editNegocioId
                            }?.nombreComercial ?: "Selecciona un negocio",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Negocio asignado") },
                            placeholder = { Text("Selecciona un negocio") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = menuEdicionNegociosExpandido
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = menuEdicionNegociosExpandido,
                            onDismissRequest = {
                                menuEdicionNegociosExpandido = false
                            }
                        ) {
                            negocios.forEach { negocio ->
                                DropdownMenuItem(
                                    text = {
                                        Text(negocio.nombreComercial)
                                    },
                                    onClick = {
                                        editNegocioId = negocio.id
                                        menuEdicionNegociosExpandido = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editPassword,
                        onValueChange = { editPassword = it },
                        label = {
                            Text("Nueva contraseña (opcional)")
                        },
                        placeholder = { Text("Deja vacío para conservar la actual") },
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

                        if (id != null && editNegocioId > 0) {
                            when (
                                val resultado = usuarioRepository.actualizarEmpleado(
                                    id = id,
                                    nombre = editNombre,
                                    apellido = editApellido,
                                    apellidoMaterno = editApellidoMaterno,
                                    correo = editCorreo,
                                    nombreUsuario = editNombreUsuario,
                                    password = editPassword,
                                    negocioId = editNegocioId
                                )
                            ) {
                                is ResultadoEmpleado.Exitoso -> {
                                    mensaje = resultado.mensaje
                                    operacionExitosa = true

                                    // Si cambió de negocio, actualizar la lista actual.
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
                        } else {
                            mensajeEdicion = "Selecciona un negocio válido."
                            edicionExitosa = false
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
