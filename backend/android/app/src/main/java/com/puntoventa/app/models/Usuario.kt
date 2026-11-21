
package com.puntoventa.app.models

data class Usuario(
    val id: Int,
    val correo: String,
    val password: String,
    val rolId: Int,
    val activo: Int,
    val nombre: String?,
    val apellido: String?,
    val nombreUsuario: String?
)
