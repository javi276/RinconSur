package com.rinconsur.app.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rinconsur.app.repository.data.AppDatabase
import com.rinconsur.app.repository.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Estado del formulario "Crear cuenta" de la persona encargada. */
data class UserFormUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val isSaving: Boolean = false,
    val message: String? = null // error de la BD (ej: email repetido)
)

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _form = MutableStateFlow(UserFormUiState())
    val form: StateFlow<UserFormUiState> = _form.asStateFlow()

    fun onNameChange(value: String) = _form.update {
        it.copy(name = value, nameError = Validaciones.validarNombre(value))
    }

    fun onEmailChange(value: String) = _form.update {
        it.copy(email = value, emailError = Validaciones.validarEmail(value), message = null)
    }

    fun onPasswordChange(value: String) = _form.update {
        it.copy(
            password = value,
            passwordError = Validaciones.validarPassword(value),
            // si ya escribió la confirmación, se vuelve a comparar
            confirmError = if (it.confirm.isNotEmpty()) Validaciones.validarConfirmacion(value, it.confirm) else null
        )
    }

    fun onConfirmChange(value: String) = _form.update {
        it.copy(confirm = value, confirmError = Validaciones.validarConfirmacion(it.password, value))
    }

    /** CREATE. onSuccess se ejecuta en el hilo principal. Devuelve false si hay errores. */
    fun saveUser(onSuccess: () -> Unit): Boolean {
        val f = _form.value
        val nameError = Validaciones.validarNombre(f.name)
        val emailError = Validaciones.validarEmail(f.email)
        val passwordError = Validaciones.validarPassword(f.password)
        val confirmError = Validaciones.validarConfirmacion(f.password, f.confirm)
        if (listOfNotNull(nameError, emailError, passwordError, confirmError).isNotEmpty()) {
            _form.update {
                it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError, confirmError = confirmError)
            }
            return false
        }
        viewModelScope.launch {
            _form.update { it.copy(isSaving = true, message = null) }
            try {
                withContext(Dispatchers.IO) {
                    userDao.insertUser(User(name = f.name.trim(), email = f.email.trim(), password = f.password))
                }
                _form.value = UserFormUiState() // limpia el formulario
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _form.update { it.copy(isSaving = false, message = "Ya existe una cuenta con el email ${f.email.trim()}") }
            }
        }
        return true
    }

    fun clearForm() {
        _form.value = UserFormUiState()
    }
}
