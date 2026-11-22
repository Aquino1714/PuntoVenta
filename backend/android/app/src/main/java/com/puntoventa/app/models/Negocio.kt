
package com.puntoventa.app.models

data class Negocio(
    val id: Int,
    val nombreComercial: String,
    val rfc: String?,
    val giro: String?,
    val telefono: String?,
    val correo: String?,
    val direccion: String?,
    val esPrincipal: Int,
    val activo: Int,
    val propietarioId: Int?,
    val logo: String?
)
