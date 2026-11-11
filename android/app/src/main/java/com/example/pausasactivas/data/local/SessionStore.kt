package com.example.pausasactivas.data.local

import android.content.Context

/** Usuario recordado localmente para la demo. */
data class UserSession(
    val id: String,
    val correo: String,
    val nombre: String
)

/**
 * Guarda el ID del usuario en SharedPreferences para no pedir el acceso cada vez.
 * No guarda contraseñas ni tokens: la demo no tiene autenticación.
 */
class SessionStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(user: UserSession) {
        prefs.edit()
            .putString(KEY_ID, user.id)
            .putString(KEY_CORREO, user.correo)
            .putString(KEY_NOMBRE, user.nombre)
            .apply()
    }

    fun load(): UserSession? {
        val id = prefs.getString(KEY_ID, null)
        if (id.isNullOrBlank()) return null
        return UserSession(
            id = id,
            correo = prefs.getString(KEY_CORREO, "") ?: "",
            nombre = prefs.getString(KEY_NOMBRE, "") ?: ""
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "pausas_activas_session"
        const val KEY_ID = "usuario_id"
        const val KEY_CORREO = "usuario_correo"
        const val KEY_NOMBRE = "usuario_nombre"
    }
}
