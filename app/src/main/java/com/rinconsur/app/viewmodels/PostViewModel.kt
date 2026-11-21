package com.rinconsur.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rinconsur.app.repository.PostRepository
import com.rinconsur.app.repository.models.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la pantalla de Comunicados (lista que viene de la API). */
data class PostUiState(
    val posts: List<Post> = emptyList(),
    val cargando: Boolean = false,  // true mientras se espera la respuesta de la API
    val error: String? = null,      // error al cargar la lista (sin internet, etc.)
    val mensaje: String? = null     // aviso corto después de crear, editar o eliminar
)

/** Estado del formulario para crear (POST) o editar (PUT) un comunicado. */
data class PostFormUiState(
    val visible: Boolean = false,
    val id: Int = 0,                // 0 = nuevo (POST) · mayor a 0 = editar (PUT)
    val titulo: String = "",
    val cuerpo: String = "",
    val tituloError: String? = null,
    val cuerpoError: String? = null,
    val enviando: Boolean = false
)

/**
 * ViewModel que mantiene el estado de los datos obtenidos desde la API Rest.
 * Usa ViewModel (no AndroidViewModel) porque no necesita la base de datos Room.
 */
class PostViewModel : ViewModel() {

    private val repository = PostRepository()

    // Flujo mutable (privado) y flujo público de solo lectura, igual que en la guía
    private val _uiState = MutableStateFlow(PostUiState())
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    private val _form = MutableStateFlow(PostFormUiState())
    val form: StateFlow<PostFormUiState> = _form.asStateFlow()

    // Se llama automáticamente al crear el ViewModel
    init {
        fetchPosts()
    }

    // ---------------- GET: obtener la lista ----------------
    fun fetchPosts() {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val lista = repository.getPosts()
                _uiState.update { it.copy(posts = lista, cargando = false) }
            } catch (e: Exception) {
                println("Error al obtener datos: ${e.localizedMessage}")
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudo conectar con la API. Revisa tu conexión a internet.")
                }
            }
        }
    }

    // ---------------- Formulario ----------------
    fun abrirNuevo() {
        _form.value = PostFormUiState(visible = true)
    }

    fun abrirEditar(post: Post) {
        _form.value = PostFormUiState(visible = true, id = post.id, titulo = post.title, cuerpo = post.body)
    }

    fun cerrarFormulario() {
        _form.value = PostFormUiState()
    }

    fun onTitulo(v: String) = _form.update { it.copy(titulo = v, tituloError = Validaciones.validarTitulo(v)) }
    fun onCuerpo(v: String) = _form.update { it.copy(cuerpo = v, cuerpoError = Validaciones.validarCuerpo(v)) }

    /** Valida y envía a la API: POST si es nuevo, PUT si se está editando. Devuelve false si hay errores. */
    fun guardar(): Boolean {
        val f = _form.value
        val tituloError = Validaciones.validarTitulo(f.titulo)
        val cuerpoError = Validaciones.validarCuerpo(f.cuerpo)
        if (tituloError != null || cuerpoError != null) {
            _form.update { it.copy(tituloError = tituloError, cuerpoError = cuerpoError) }
            return false
        }
        val post = Post(userId = 1, id = f.id, title = f.titulo.trim(), body = f.cuerpo.trim())
        if (f.id == 0) crearPost(post) else actualizarPost(post)
        return true
    }

    // ---------------- POST: crear ----------------
    private fun crearPost(post: Post) {
        _form.update { it.copy(enviando = true) }
        viewModelScope.launch {
            try {
                val creado = repository.crearPost(post)
                // JSONPlaceholder es una API de PRUEBA: responde siempre id = 101 y no guarda nada.
                // Por eso le damos un id que no se repita para poder mostrarlo en la lista.
                val nuevoId = maxOf(creado.id, (_uiState.value.posts.maxOfOrNull { it.id } ?: 0) + 1)
                _uiState.update {
                    it.copy(posts = listOf(creado.copy(id = nuevoId)) + it.posts, mensaje = "Comunicado creado (POST)")
                }
                cerrarFormulario()
            } catch (e: Exception) {
                _form.update { it.copy(enviando = false) }
                _uiState.update { it.copy(mensaje = "Error al crear: revisa tu conexión") }
            }
        }
    }

    // ---------------- PUT: actualizar ----------------
    private fun actualizarPost(post: Post) {
        _form.update { it.copy(enviando = true) }
        viewModelScope.launch {
            try {
                // La API de prueba solo conoce los posts 1 a 100.
                // Los que creamos nosotros (id > 100) se actualizan solo en la app.
                val actualizado = if (post.id <= 100) repository.actualizarPost(post) else post
                _uiState.update { estado ->
                    estado.copy(
                        posts = estado.posts.map { if (it.id == post.id) actualizado else it },
                        mensaje = "Comunicado actualizado (PUT)"
                    )
                }
                cerrarFormulario()
            } catch (e: Exception) {
                _form.update { it.copy(enviando = false) }
                _uiState.update { it.copy(mensaje = "Error al actualizar: revisa tu conexión") }
            }
        }
    }

    // ---------------- DELETE: eliminar ----------------
    fun eliminar(post: Post) {
        viewModelScope.launch {
            try {
                val ok = repository.eliminarPost(post.id)
                if (ok) {
                    _uiState.update { estado ->
                        estado.copy(posts = estado.posts.filter { it.id != post.id }, mensaje = "Comunicado eliminado (DELETE)")
                    }
                } else {
                    _uiState.update { it.copy(mensaje = "La API no permitió eliminar") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = "Error al eliminar: revisa tu conexión") }
            }
        }
    }

    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }
}
