
package com.puntoventa.app.screens

import android.net.Uri
import android.widget.ImageView
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.puntoventa.app.database.DatabaseHelper
import com.puntoventa.app.repositories.NegocioRepository
import com.puntoventa.app.repositories.ResultadoNegocio

@Composable
fun NegociosScreen(
    administradorId: Int,
    onRegresar: () -> Unit
) {
    val context = LocalContext.current

    val repository = remember {
        NegocioRepository(DatabaseHelper(context))
    }

    var negocios by remember {
        mutableStateOf(repository.obtenerPorPropietario(administradorId))
    }

    var busqueda by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var operacionExitosa by remember { mutableStateOf(false) }

    var negocioEditandoId by remember { mutableStateOf<Int?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    var nombreComercial by remember { mutableStateOf("") }
    var rfc by remember { mutableStateOf("") }
    var giro by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var logo by remember { mutableStateOf<String?>(null) }
    var esPrincipal by remember { mutableStateOf(false) }

    var mensajeFormulario by remember { mutableStateOf("") }

    fun cargarNegocios() {
        negocios = repository.obtenerPorPropietario(administradorId)
    }

    fun limpiarFormulario() {
        nombreComercial = ""
        rfc = ""
        giro = ""
        telefono = ""
        correo = ""
        direccion = ""
        logo = null
        esPrincipal = negocios.isEmpty()
        mensajeFormulario = ""
    }

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Se conserva la URI aunque no se pueda guardar el permiso.
            }

            logo = uri.toString()
        }
    }

    val negociosFiltrados = negocios.filter {
        it.nombreComercial.contains(busqueda, ignoreCase = true) ||
            (it.rfc ?: "").contains(busqueda, ignoreCase = true) ||
            (it.giro ?: "").contains(busqueda, ignoreCase = true)
    }

    // PANTALLA PRINCIPAL CON DESPLAZAMIENTO
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Gestión de negocios",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Administra la información de tus negocios.")

        HorizontalDivider()

        if (negocios.isEmpty()) {

            Spacer(Modifier.height(20.dp))

            Text(
                "🏪",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                "Todavía no tienes negocios",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                "Registra tu primer negocio para comenzar a administrar tu información.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    negocioEditandoId = null
                    limpiarFormulario()
                    mostrarDialogo = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar mi primer negocio")
            }

        } else {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Negocios registrados (${negocios.size})",
                    style = MaterialTheme.typography.titleLarge
                )

                TextButton(
                    onClick = {
                        negocioEditandoId = null
                        limpiarFormulario()
                        mostrarDialogo = true
                    }
                ) {
                    Text("+ Agregar")
                }
            }

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar negocio") },
                placeholder = { Text("Nombre, RFC o giro") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (negociosFiltrados.isEmpty()) {
                Text("No se encontraron negocios.")
            }

            negociosFiltrados.forEach { negocio ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            if (!negocio.logo.isNullOrBlank()) {
                                AndroidView(
                                    factory = { ctx ->
                                        ImageView(ctx).apply {
                                            scaleType = ImageView.ScaleType.CENTER_CROP
                                        }
                                    },
                                    update = { imageView ->
                                        imageView.setImageURI(Uri.parse(negocio.logo))
                                    },
                                    modifier = Modifier.size(70.dp)
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(70.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🏪")
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    negocio.nombreComercial,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text("RFC: ${negocio.rfc ?: "No registrado"}")
                                Text("Giro: ${negocio.giro ?: "No registrado"}")
                            }
                        }

                        if (negocio.esPrincipal == 1) {
                            Text(
                                "★ Negocio principal",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            if (negocio.activo == 1) "Estado: Activo"
                            else "Estado: Inactivo",
                            color = if (negocio.activo == 1)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )

                        OutlinedButton(
                            onClick = {
                                negocioEditandoId = negocio.id
                                nombreComercial = negocio.nombreComercial
                                rfc = negocio.rfc.orEmpty()
                                giro = negocio.giro.orEmpty()
                                telefono = negocio.telefono.orEmpty()
                                correo = negocio.correo.orEmpty()
                                direccion = negocio.direccion.orEmpty()
                                logo = negocio.logo
                                esPrincipal = negocio.esPrincipal == 1
                                mensajeFormulario = ""
                                mostrarDialogo = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Editar negocio")
                        }

                        OutlinedButton(
                            onClick = {
                                when (
                                    val resultado = repository.cambiarEstado(
                                        negocio.id,
                                        negocio.activo != 1,
                                        administradorId
                                    )
                                ) {
                                    is ResultadoNegocio.Exitoso -> {
                                        mensaje = resultado.mensaje
                                        operacionExitosa = true
                                        cargarNegocios()
                                    }

                                    is ResultadoNegocio.Error -> {
                                        mensaje = resultado.mensaje
                                        operacionExitosa = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (negocio.activo == 1)
                                    "Desactivar negocio"
                                else
                                    "Activar negocio"
                            )
                        }
                    }
                }
            }
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

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Regresar al menú")
        }

        Spacer(Modifier.height(16.dp))
    }

    // FORMULARIO DE REGISTRO Y EDICIÓN
    if (mostrarDialogo) {

        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false
                negocioEditandoId = null
            },

            title = {
                Text(
                    if (negocioEditandoId == null)
                        "Registrar negocio"
                    else
                        "Editar negocio"
                )
            },

            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text("Logotipo del negocio")

                    if (!logo.isNullOrBlank()) {
                        AndroidView(
                            factory = { ctx ->
                                ImageView(ctx).apply {
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                }
                            },
                            update = { imageView ->
                                imageView.setImageURI(Uri.parse(logo))
                            },
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            selectorImagen.launch(arrayOf("image/*"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (logo == null)
                                "Seleccionar logotipo"
                            else
                                "Cambiar logotipo"
                        )
                    }

                    OutlinedTextField(
                        value = nombreComercial,
                        onValueChange = { nombreComercial = it },
                        label = { Text("Nombre comercial *") },
                        placeholder = { Text("Ej. Abarrotes Lupita") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = rfc,
                        onValueChange = {
                            rfc = it.uppercase().filter { c ->
                                c.isLetterOrDigit() || c == '&'
                            }.take(13)
                        },
                        label = { Text("RFC") },
                        supportingText = {
                            Text("12 caracteres para persona moral o 13 para persona física.")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = giro,
                        onValueChange = { giro = it },
                        label = { Text("Giro del negocio") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = {
                            telefono = it.filter { c -> c.isDigit() }.take(10)
                        },
                        label = { Text("Teléfono") },
                        supportingText = {
                            Text("Ingresa 10 dígitos.")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
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
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = esPrincipal,
                            onCheckedChange = { esPrincipal = it }
                        )

                        Text("Establecer como negocio principal")
                    }

                    Text(
                        "Los campos marcados con * son obligatorios.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (mensajeFormulario.isNotEmpty()) {
                        Text(
                            mensajeFormulario,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        val rfcLimpio = rfc.trim().uppercase()
                        val telefonoLimpio = telefono.trim()
                        val correoLimpio = correo.trim()

                        when {
                            nombreComercial.isBlank() -> {
                                mensajeFormulario =
                                    "El nombre comercial es obligatorio."
                                return@Button
                            }

                            rfcLimpio.isNotBlank() &&
                                !Regex("^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$")
                                    .matches(rfcLimpio) -> {
                                mensajeFormulario =
                                    "El RFC debe tener un formato válido."
                                return@Button
                            }

                            telefonoLimpio.isNotBlank() &&
                                !Regex("^\\d{10}$").matches(telefonoLimpio) -> {
                                mensajeFormulario =
                                    "El teléfono debe contener 10 dígitos."
                                return@Button
                            }

                            correoLimpio.isNotBlank() &&
                                !Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches() -> {
                                mensajeFormulario =
                                    "Ingresa un correo electrónico válido."
                                return@Button
                            }
                        }

                        mensajeFormulario = ""

                        val id = negocioEditandoId

                        val resultado = if (id == null) {
                            repository.registrar(
                                nombreComercial = nombreComercial,
                                rfc = rfc,
                                giro = giro,
                                telefono = telefono,
                                correo = correo,
                                direccion = direccion,
                                logo = logo,
                                esPrincipal = esPrincipal,
                                propietarioId = administradorId
                            )
                        } else {
                            repository.actualizar(
                                id = id,
                                nombreComercial = nombreComercial,
                                rfc = rfc,
                                giro = giro,
                                telefono = telefono,
                                correo = correo,
                                direccion = direccion,
                                logo = logo,
                                esPrincipal = esPrincipal,
                                propietarioId = administradorId
                            )
                        }

                        when (resultado) {
                            is ResultadoNegocio.Exitoso -> {
                                mensaje = resultado.mensaje
                                operacionExitosa = true
                                cargarNegocios()
                                mostrarDialogo = false
                                negocioEditandoId = null
                            }

                            is ResultadoNegocio.Error -> {
                                mensajeFormulario = resultado.mensaje
                            }
                        }
                    }
                ) {
                    Text(
                        if (negocioEditandoId == null)
                            "Registrar"
                        else
                            "Guardar cambios"
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false
                        negocioEditandoId = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
