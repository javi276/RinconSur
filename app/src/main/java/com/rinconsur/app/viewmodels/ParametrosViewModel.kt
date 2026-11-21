package com.rinconsur.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rinconsur.app.repository.data.AppDatabase
import com.rinconsur.app.repository.models.Acceso
import com.rinconsur.app.repository.models.Parametros
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val TURNOS = listOf("Mañana", "Tarde", "Noche")

/** Estado del formulario de Parámetros (tienda, turno, horario y acceso). */
data class ParametrosUiState(
    val tienda: String = "",
    val turno: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val accesoId: Int = 0, // 0 = sin seleccionar
    val tiendaError: String? = null,
    val turnoError: String? = null,
    val inicioError: String? = null,
    val finError: String? = null,
    val accesoError: String? = null,
    val guardado: Boolean = false
)

class ParametrosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val parametrosDao = db.parametrosDao()

    // READ: parámetros guardados (null si todavía no hay). Eagerly: se cargan al crear el ViewModel.
    val parametros: StateFlow<Parametros?> = parametrosDao.get()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Lista de accesos desde Room (para el selector)
    val accesos: StateFlow<List<Acceso>> = db.accesoDao().getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _form = MutableStateFlow(ParametrosUiState())
    val form: StateFlow<ParametrosUiState> = _form.asStateFlow()

    init {
        // Si ya había parámetros guardados, el formulario parte con esos valores
        viewModelScope.launch {
            parametrosDao.get().firstOrNull()?.let { p ->
                _form.update {
                    it.copy(tienda = p.tienda, turno = p.turno, horaInicio = p.horaInicio, horaFin = p.horaFin, accesoId = p.accesoId)
                }
            }
        }
    }

    fun onTienda(v: String) = _form.update { it.copy(tienda = v, tiendaError = Validaciones.validarTienda(v)) }
    fun onTurno(v: String) = _form.update { it.copy(turno = v, turnoError = Validaciones.validarSeleccion(v, "un turno")) }
    fun onHoraInicio(v: String) = _form.update { it.copy(horaInicio = v, inicioError = Validaciones.validarHora(v)) }
    fun onHoraFin(v: String) = _form.update { it.copy(horaFin = v, finError = Validaciones.validarHora(v)) }
    fun onAcceso(id: Int) = _form.update { it.copy(accesoId = id, accesoError = Validaciones.validarAcceso(id, "tu acceso")) }

    /** CREATE / UPDATE. Devuelve false si hay errores (la vista vibra como aviso). */
    fun guardar(): Boolean {
        val f = _form.value
        val tiendaError = Validaciones.validarTienda(f.tienda)
        val turnoError = Validaciones.validarSeleccion(f.turno, "un turno")
        val inicioError = Validaciones.validarHora(f.horaInicio)
        val finError = Validaciones.validarHora(f.horaFin) ?: Validaciones.validarHorario(f.horaInicio, f.horaFin)
        val accesoError = Validaciones.validarAcceso(f.accesoId, "tu acceso")

        if (listOfNotNull(tiendaError, turnoError, inicioError, finError, accesoError).isNotEmpty()) {
            _form.update {
                it.copy(tiendaError = tiendaError, turnoError = turnoError, inicioError = inicioError, finError = finError, accesoError = accesoError)
            }
            return false
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                parametrosDao.guardar(
                    Parametros(tienda = f.tienda.trim(), turno = f.turno, horaInicio = f.horaInicio, horaFin = f.horaFin, accesoId = f.accesoId)
                )
            }
            _form.update { it.copy(guardado = true) }
        }
        return true
    }

    fun limpiarGuardado() = _form.update { it.copy(guardado = false) }
}
