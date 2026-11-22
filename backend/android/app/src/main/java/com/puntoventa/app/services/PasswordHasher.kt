
package com.puntoventa.app.services

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 210_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val PREFIX = "pbkdf2_sha256"

    fun generarHash(password: String): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        val hash = derivarClave(password, salt, ITERATIONS)

        return "$PREFIX$$ITERATIONS$${
            Base64.encodeToString(salt, Base64.NO_WRAP)
        }$${
            Base64.encodeToString(hash, Base64.NO_WRAP)
        }"
    }

    fun verificarPassword(
        password: String,
        hashGuardado: String
    ): Boolean {
        return try {
            val partes = hashGuardado.split("$")

            if (partes.size != 4 || partes[0] != PREFIX) {
                return false
            }

            val iteraciones = partes[1].toInt()
            if (iteraciones <= 0) return false

            val salt = Base64.decode(partes[2], Base64.NO_WRAP)
            val hashOriginal = Base64.decode(partes[3], Base64.NO_WRAP)

            val hashCalculado = derivarClave(
                password,
                salt,
                iteraciones
            )

            MessageDigest.isEqual(hashOriginal, hashCalculado)

        } catch (e: Exception) {
            false
        }
    }

    fun esHash(passwordGuardado: String): Boolean {
        return passwordGuardado.startsWith("$PREFIX$")
    }

    private fun derivarClave(
        password: String,
        salt: ByteArray,
        iteraciones: Int
    ): ByteArray {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            iteraciones,
            KEY_LENGTH
        )

        return try {
            SecretKeyFactory.getInstance(ALGORITHM)
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }
}
