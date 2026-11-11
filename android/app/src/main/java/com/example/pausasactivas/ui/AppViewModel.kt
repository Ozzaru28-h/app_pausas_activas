package com.example.pausasactivas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pausasactivas.data.api.SessionDto
import com.example.pausasactivas.data.api.UserDto
import com.example.pausasactivas.data.local.SessionStore
import com.example.pausasactivas.data.local.UserSession
import com.example.pausasactivas.data.repository.PausasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen { LOGIN, HOME, CAMERA, HISTORY }

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null
)

data class HistoryUiState(
    val loading: Boolean = false,
    val sessions: List<SessionDto> = emptyList(),
    val error: String? = null
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = SessionStore(application)
    private val repository = PausasRepository()

    private val _user = MutableStateFlow(store.load())
    val user: StateFlow<UserSession?> = _user.asStateFlow()

    private val _screen = MutableStateFlow(if (_user.value != null) Screen.HOME else Screen.LOGIN)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _auth = MutableStateFlow(AuthUiState())
    val auth: StateFlow<AuthUiState> = _auth.asStateFlow()

    private val _history = MutableStateFlow(HistoryUiState())
    val history: StateFlow<HistoryUiState> = _history.asStateFlow()

    fun navigate(target: Screen) {
        _screen.value = target
    }

    fun clearAuthError() {
        _auth.update { it.copy(error = null) }
    }

    /** Acceso de demo: solo comprueba que el correo esté registrado. No hay contraseña. */
    fun login(correo: String) {
        val email = correo.trim().lowercase()
        if (!isInstitutionalEmail(email)) {
            _auth.value = AuthUiState(error = EMAIL_ERROR)
            return
        }
        launchAuth { repository.login(email) }
    }

    fun register(correo: String, nombre: String, carrera: String) {
        val email = correo.trim().lowercase()
        if (!isInstitutionalEmail(email)) {
            _auth.value = AuthUiState(error = EMAIL_ERROR)
            return
        }
        if (nombre.isBlank()) {
            _auth.value = AuthUiState(error = "El nombre es obligatorio.")
            return
        }
        launchAuth { repository.register(email, nombre.trim(), carrera.trim().ifBlank { null }) }
    }

    fun logout() {
        store.clear()
        _user.value = null
        _history.value = HistoryUiState()
        _auth.value = AuthUiState()
        _screen.value = Screen.LOGIN
    }

    fun loadHistory() {
        val current = _user.value ?: return
        _history.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            repository.getHistory(current.id).fold(
                onSuccess = { sessions ->
                    _history.value = HistoryUiState(sessions = sessions)
                },
                onFailure = { e ->
                    _history.value = HistoryUiState(
                        error = e.message ?: "No se pudo cargar el historial."
                    )
                }
            )
        }
    }

    // ------------------------------------------------------------------ Internos

    private fun launchAuth(request: suspend () -> Result<UserDto>) {
        _auth.value = AuthUiState(loading = true)
        viewModelScope.launch {
            request().fold(
                onSuccess = { dto ->
                    val session = UserSession(
                        id = dto.id.orEmpty(),
                        correo = dto.correo.orEmpty(),
                        nombre = dto.nombre.orEmpty()
                    )
                    store.save(session)
                    _user.value = session
                    _auth.value = AuthUiState()
                    _screen.value = Screen.HOME
                },
                onFailure = { e ->
                    _auth.value = AuthUiState(error = e.message ?: "No se pudo completar el acceso.")
                }
            )
        }
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@uniautonoma\\.edu\\.co$")
        const val EMAIL_ERROR = "Usa tu correo institucional (termina en @uniautonoma.edu.co)."

        fun isInstitutionalEmail(email: String) = EMAIL_REGEX.matches(email)
    }
}
