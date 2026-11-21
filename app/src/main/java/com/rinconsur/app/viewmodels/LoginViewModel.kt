package com.rinconsur.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rinconsur.app.repository.data.AppDatabase
import com.rinconsur.app.repository.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,     // error por campo (se muestra bajo el campo)
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,          // error general (credenciales inválidas)
    val loggedUser: User? = null        // persona encargada con sesión iniciada
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Cada cambio se valida al instante con Validaciones (retroalimentación por campo)
    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = Validaciones.validarEmail(value), error = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = Validaciones.validarPassword(value), error = null)
    }

    fun login() {
        val state = _uiState.value
        val emailError = Validaciones.validarEmail(state.email)
        val passwordError = Validaciones.validarPassword(state.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(600) // pequeña pausa para mostrar el indicador de carga
            val user = withContext(Dispatchers.IO) {
                userDao.login(state.email.trim(), state.password)
            }
            _uiState.update {
                if (user != null) it.copy(isLoading = false, loggedUser = user, password = "")
                else it.copy(isLoading = false, error = "Credenciales inválidas")
            }
        }
    }

    fun logout() {
        _uiState.value = LoginUiState()
    }
}
