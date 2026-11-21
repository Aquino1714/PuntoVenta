
package com.puntoventa.app.models

enum class Rol(val valor: String) {
    ADMIN("admin"),
    EMPLEADO("empleado");

    companion object {
        fun desdeValor(valor: String): Rol? {
            return entries.firstOrNull {
                it.valor.equals(valor.trim(), ignoreCase = true)
            }
        }
    }
}
