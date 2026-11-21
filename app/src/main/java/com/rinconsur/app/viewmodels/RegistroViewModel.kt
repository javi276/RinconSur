package com.rinconsur.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rinconsur.app.repository.data.AppDatabase
import com.rinconsur.app.repository.models.Acceso
import com.rinconsur.app.repository.models.Parametros
import com.rinconsur.app.repository.models.Registro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Opciones fijas de los formularios
val SEXOS = listOf("Femenino", "Masculino")
val RANGOS = listOf("18-25", "26-35", "36-50", "51+")
val TIPOS_ACOMPANANTE = listOf("Adulto", "Menor")

/** Formulario de CHECK-IN (entrada). */
data class CheckInUiState(
    val accesoId: Int = 0,           // 0 = sin seleccionar
    val sexo: String = "",
    val acompanado: Boolean = false,
    val tipoAcomp: String = "",
    val sexoAcomp: String = "",
    val rango: String = "",
    val fotoUri: String? = null,
    val accesoError: String? = null,
    val sexoError: String? = null,
    val rangoError: String? = null,
    val tipoAcompError: String? = null,
    val sexoAcompError: String? = null,
    val guardado: Boolean = false
)

/** Formulario de CHECK-OUT (salida) de una persona que está dentro. */
data class CheckOutUiState(
    val seleccionado: Registro? = null,
    val accesoSalidaId: Int = 0,
    val compro: Boolean = false,
    val accesoError: String? = null,
    val guardado: Boolean = false
)

/**
 * Formulario de EDICIÓN: corrige un registro que ya se guardó.
 * Parte con los datos del registro (formulario precargado).
 */
data class EditarUiState(
    val original: Registro? = null,  // el registro tal como está en la BD (null = cargando)
    val accesoId: Int = 0,
    val sexo: String = "",
    val acompanado: Boolean = false,
    val tipoAcomp: String = "",
    val sexoAcomp: String = "",
    val rango: String = "",
    val fotoUri: String? = null,
    // Datos de salida: solo se editan si la persona ya hizo check-out
    val accesoSalidaId: Int = 0,
    val compro: Boolean = false,
    val accesoError: String? = null,
    val sexoError: String? = null,
    val rangoError: String? = null,
    val tipoAcompError: String? = null,
    val sexoAcompError: String? = null,
    val accesoSalidaError: String? = null,
    val guardado: Boolean = false
)

/** Resumen de un acceso (lo que muestra el Inicio). */
data class ResumenAcceso(
    val acceso: Acceso,
    val entradas: Int,
    val salidas: Int,
    val transitan: Int, // entraron por este acceso y salieron sin comprar
    val compran: Int    // entraron por este acceso y compraron
)

/** Perfil de las personas que solo transitan (salieron sin comprar). */
data class PerfilTransito(
    val total: Int = 0,
    val femenino: Int = 0,
    val masculino: Int = 0,
    val solos: Int = 0,
    val acompanados: Int = 0,
    val acompAdultos: Int = 0,
    val acompMenores: Int = 0,
    val porRango: List<Pair<String, Int>> = emptyList()
)

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val registroDao = db.registroDao()

    // READ: todos los registros desde Room (se actualiza solo con Flow)
    val registros: StateFlow<List<Registro>> = registroDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Los 4 accesos de la tienda
    val accesos: StateFlow<List<Acceso>> = db.accesoDao().getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Personas que siguen dentro (sin check-out)
    val dentro: StateFlow<List<Registro>> = registros
        .map { lista -> lista.filter { it.checkOut == null } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Resumen por acceso: combina registros + accesos
    val resumen: StateFlow<List<ResumenAcceso>> = combine(registros, accesos) { lista, accs ->
        accs.map { a ->
            ResumenAcceso(
                acceso = a,
                entradas = lista.count { it.accesoEntradaId == a.id },
                salidas = lista.count { it.accesoSalidaId == a.id },
                transitan = lista.count { it.accesoEntradaId == a.id && it.compro == false },
                compran = lista.count { it.accesoEntradaId == a.id && it.compro == true }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Perfil de quienes salieron sin comprar
    val perfil: StateFlow<PerfilTransito> = registros
        .map { lista ->
            val t = lista.filter { it.compro == false }
            PerfilTransito(
                total = t.size,
                femenino = t.count { it.sexo == "Femenino" },
                masculino = t.count { it.sexo == "Masculino" },
                solos = t.count { !it.acompanado },
                acompanados = t.count { it.acompanado },
                acompAdultos = t.count { it.acompanado && it.tipoAcompanante == "Adulto" },
                acompMenores = t.count { it.acompanado && it.tipoAcompanante == "Menor" },
                porRango = RANGOS.map { r -> r to t.count { it.rangoEtario == r } }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PerfilTransito())

    /** Nombre del acceso para mostrar en pantalla (ej: "Acceso 2 - Estacionamiento"). */
    fun nombreAcceso(id: Int?): String =
        accesos.value.firstOrNull { it.id == id }?.nombre ?: "Acceso ${id ?: "-"}"

    private fun ahora(): String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

    // ======================= CHECK-IN =======================
    private val _checkIn = MutableStateFlow(CheckInUiState())
    val checkIn: StateFlow<CheckInUiState> = _checkIn.asStateFlow()

    fun onAcceso(id: Int) = _checkIn.update { it.copy(accesoId = id, accesoError = Validaciones.validarAcceso(id, "un acceso")) }
    fun onSexo(v: String) = _checkIn.update { it.copy(sexo = v, sexoError = Validaciones.validarSeleccion(v, "el sexo")) }
    fun onRango(v: String) = _checkIn.update { it.copy(rango = v, rangoError = Validaciones.validarSeleccion(v, "un rango de edad")) }
    fun onTipoAcomp(v: String) = _checkIn.update { it.copy(tipoAcomp = v, tipoAcompError = Validaciones.validarSeleccion(v, "adulto o menor")) }
    fun onSexoAcomp(v: String) = _checkIn.update { it.copy(sexoAcomp = v, sexoAcompError = Validaciones.validarSeleccion(v, "el sexo del acompañante")) }
    fun onFoto(uri: String?) = _checkIn.update { it.copy(fotoUri = uri) }

    fun onAcompanado(valor: Boolean) = _checkIn.update {
        // Si deja de ir acompañada, se borran los datos del acompañante
        if (valor) it.copy(acompanado = true)
        else it.copy(acompanado = false, tipoAcomp = "", sexoAcomp = "", tipoAcompError = null, sexoAcompError = null)
    }

    /**
     * CREATE (check-in). Devuelve true si el formulario es válido y se guarda;
     * false si hay errores (la vista usa ese false para vibrar).
     */
    fun guardarCheckIn(encargado: String, parametros: Parametros?): Boolean {
        if (parametros == null || encargado.isBlank()) return false
        val f = _checkIn.value
        val accesoError = Validaciones.validarAcceso(f.accesoId, "un acceso")
        val sexoError = Validaciones.validarSeleccion(f.sexo, "el sexo")
        val rangoError = Validaciones.validarSeleccion(f.rango, "un rango de edad")
        // El acompañante solo se valida si la persona entra acompañada
        val tipoAcompError = if (f.acompanado) Validaciones.validarSeleccion(f.tipoAcomp, "adulto o menor") else null
        val sexoAcompError = if (f.acompanado) Validaciones.validarSeleccion(f.sexoAcomp, "el sexo del acompañante") else null

        if (listOfNotNull(accesoError, sexoError, rangoError, tipoAcompError, sexoAcompError).isNotEmpty()) {
            _checkIn.update {
                it.copy(accesoError = accesoError, sexoError = sexoError, rangoError = rangoError, tipoAcompError = tipoAcompError, sexoAcompError = sexoAcompError)
            }
            return false
        }
        val registro = Registro(
            accesoEntradaId = f.accesoId,
            sexo = f.sexo,
            acompanado = f.acompanado,
            tipoAcompanante = if (f.acompanado) f.tipoAcomp else null,
            sexoAcompanante = if (f.acompanado) f.sexoAcomp else null,
            rangoEtario = f.rango,
            checkIn = ahora(),
            encargado = encargado,
            tienda = parametros.tienda,
            turno = parametros.turno,
            fotoUri = f.fotoUri
        )
        // Se limpia el formulario de inmediato para evitar guardar dos veces con doble toque
        _checkIn.value = CheckInUiState(accesoId = parametros.accesoId, guardado = true)
        viewModelScope.launch(Dispatchers.IO) { registroDao.insert(registro) }
        return true
    }

    fun limpiarCheckInGuardado() = _checkIn.update { it.copy(guardado = false) }

    // ======================= CHECK-OUT =======================
    private val _checkOut = MutableStateFlow(CheckOutUiState())
    val checkOut: StateFlow<CheckOutUiState> = _checkOut.asStateFlow()

    fun seleccionarParaSalida(registro: Registro) { _checkOut.value = CheckOutUiState(seleccionado = registro) }
    fun cancelarSalida() { _checkOut.value = CheckOutUiState() }
    fun onAccesoSalida(id: Int) = _checkOut.update { it.copy(accesoSalidaId = id, accesoError = Validaciones.validarAcceso(id, "el acceso de salida")) }
    fun onCompro(valor: Boolean) = _checkOut.update { it.copy(compro = valor) }

    /** UPDATE (check-out). true si se guarda, false si falta el acceso de salida. */
    fun confirmarCheckOut(): Boolean {
        val f = _checkOut.value
        val registro = f.seleccionado ?: return false
        val accesoError = Validaciones.validarAcceso(f.accesoSalidaId, "el acceso de salida")
        if (accesoError != null) {
            _checkOut.update { it.copy(accesoError = accesoError) }
            return false
        }
        val actualizado = registro.copy(accesoSalidaId = f.accesoSalidaId, checkOut = ahora(), compro = f.compro)
        _checkOut.value = CheckOutUiState(guardado = true)
        viewModelScope.launch(Dispatchers.IO) { registroDao.update(actualizado) }
        return true
    }

    fun limpiarCheckOutGuardado() = _checkOut.update { it.copy(guardado = false) }

    // ======================= EDITAR (desde el Historial) =======================
    private val _editar = MutableStateFlow(EditarUiState())
    val editar: StateFlow<EditarUiState> = _editar.asStateFlow()

    /** READ: busca el registro en Room y llena el formulario con sus datos. */
    fun cargarParaEditar(id: Int) {
        viewModelScope.launch {
            val r = withContext(Dispatchers.IO) { registroDao.getById(id) } ?: return@launch
            _editar.value = EditarUiState(
                original = r,
                accesoId = r.accesoEntradaId,
                sexo = r.sexo,
                acompanado = r.acompanado,
                tipoAcomp = r.tipoAcompanante ?: "",
                sexoAcomp = r.sexoAcompanante ?: "",
                rango = r.rangoEtario,
                fotoUri = r.fotoUri,
                accesoSalidaId = r.accesoSalidaId ?: 0,
                compro = r.compro ?: false
            )
        }
    }

    fun onEditAcceso(id: Int) = _editar.update { it.copy(accesoId = id, accesoError = Validaciones.validarAcceso(id, "un acceso")) }
    fun onEditSexo(v: String) = _editar.update { it.copy(sexo = v, sexoError = Validaciones.validarSeleccion(v, "el sexo")) }
    fun onEditRango(v: String) = _editar.update { it.copy(rango = v, rangoError = Validaciones.validarSeleccion(v, "un rango de edad")) }
    fun onEditTipoAcomp(v: String) = _editar.update { it.copy(tipoAcomp = v, tipoAcompError = Validaciones.validarSeleccion(v, "adulto o menor")) }
    fun onEditSexoAcomp(v: String) = _editar.update { it.copy(sexoAcomp = v, sexoAcompError = Validaciones.validarSeleccion(v, "el sexo del acompañante")) }
    fun onEditFoto(uri: String?) = _editar.update { it.copy(fotoUri = uri) }
    fun onEditAccesoSalida(id: Int) = _editar.update { it.copy(accesoSalidaId = id, accesoSalidaError = Validaciones.validarAcceso(id, "el acceso de salida")) }
    fun onEditCompro(valor: Boolean) = _editar.update { it.copy(compro = valor) }

    fun onEditAcompanado(valor: Boolean) = _editar.update {
        // Igual que en el check-in: si deja de ir acompañada, se borran los datos del acompañante
        if (valor) it.copy(acompanado = true)
        else it.copy(acompanado = false, tipoAcomp = "", sexoAcomp = "", tipoAcompError = null, sexoAcompError = null)
    }

    /**
     * UPDATE (editar). Valida con las mismas reglas del check-in y guarda con registroDao.update.
     * La fecha de entrada/salida, el encargado, la tienda y el turno NO cambian.
     * Devuelve true si se guardó; false si hay errores (la vista vibra).
     */
    fun guardarEdicion(): Boolean {
        val f = _editar.value
        val original = f.original ?: return false
        val yaSalio = original.checkOut != null

        val accesoError = Validaciones.validarAcceso(f.accesoId, "un acceso")
        val sexoError = Validaciones.validarSeleccion(f.sexo, "el sexo")
        val rangoError = Validaciones.validarSeleccion(f.rango, "un rango de edad")
        val tipoAcompError = if (f.acompanado) Validaciones.validarSeleccion(f.tipoAcomp, "adulto o menor") else null
        val sexoAcompError = if (f.acompanado) Validaciones.validarSeleccion(f.sexoAcomp, "el sexo del acompañante") else null
        val accesoSalidaError = if (yaSalio) Validaciones.validarAcceso(f.accesoSalidaId, "el acceso de salida") else null

        if (listOfNotNull(accesoError, sexoError, rangoError, tipoAcompError, sexoAcompError, accesoSalidaError).isNotEmpty()) {
            _editar.update {
                it.copy(
                    accesoError = accesoError, sexoError = sexoError, rangoError = rangoError,
                    tipoAcompError = tipoAcompError, sexoAcompError = sexoAcompError, accesoSalidaError = accesoSalidaError
                )
            }
            return false
        }
        // copy() mantiene el mismo id: por eso Room ACTUALIZA la fila en vez de crear una nueva
        val actualizado = original.copy(
            accesoEntradaId = f.accesoId,
            sexo = f.sexo,
            acompanado = f.acompanado,
            tipoAcompanante = if (f.acompanado) f.tipoAcomp else null,
            sexoAcompanante = if (f.acompanado) f.sexoAcomp else null,
            rangoEtario = f.rango,
            fotoUri = f.fotoUri,
            accesoSalidaId = if (yaSalio) f.accesoSalidaId else null,
            compro = if (yaSalio) f.compro else null
        )
        _editar.update { it.copy(guardado = true) }
        viewModelScope.launch(Dispatchers.IO) { registroDao.update(actualizado) }
        return true
    }

    /** Deja el formulario de edición vacío al salir de la pantalla. */
    fun limpiarEdicion() { _editar.value = EditarUiState() }

    // ======================= DELETE (Historial) =======================
    fun eliminarRegistro(registro: Registro) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { registroDao.delete(registro) }
        }
    }
}
