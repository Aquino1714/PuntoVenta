
package com.puntoventa.app.services

object Permisos {

    const val ROL_ADMIN = 1
    const val ROL_EMPLEADO = 2

    fun esAdministrador(rolId: Int): Boolean {
        return rolId == ROL_ADMIN
    }

    fun esEmpleado(rolId: Int): Boolean {
        return rolId == ROL_EMPLEADO
    }

    fun puedeGestionarUsuarios(rolId: Int): Boolean {
        return esAdministrador(rolId)
    }

    fun puedeGestionarNegocios(rolId: Int): Boolean {
        return esAdministrador(rolId)
    }

    fun puedeGestionarProductos(rolId: Int): Boolean {
        return esAdministrador(rolId) ||
                esEmpleado(rolId)
    }

    fun puedeRealizarVentas(rolId: Int): Boolean {
        return esAdministrador(rolId) ||
                esEmpleado(rolId)
    }
}
