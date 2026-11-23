package com.puntoventa.app.models

data class Cliente(
    val id: Int,
    val negocioId: Int,
    val nombre: String,
    val apellidoPaterno: String?,
    val apellidoMaterno: String?,
    val telefono: String?,
    val correo: String?,
    val direccion: String?,
    val tipoPagoHabitual: String?,
    val activo: Int = 1
) {

    // Junta nombre y apellidos, ignorando los que vengan nulos o vacíos
    fun nombreCompleto(): String {
        val partes = listOf(nombre, apellidoPaterno, apellidoMaterno)

        return partes
            .mapNotNull { it?.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(" ")
    }
}