

package com.puntoventa.app.models

data class Usuario(
    val id: Int,
    val correo: String,
    val password: String,
    val rol: String,
    val activo: Int
)


