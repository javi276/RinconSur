package com.rinconsur.app.repository.remote

import com.rinconsur.app.repository.models.Post
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Esta interfaz define los endpoints HTTP de la API (Retrofit escribe el código por nosotros).
 * Cada función = un método HTTP + una ruta.
 *   GET    /posts       -> leer la lista        (READ)
 *   POST   /posts       -> crear uno nuevo      (CREATE)
 *   PUT    /posts/{id}  -> reemplazar/editar    (UPDATE)
 *   DELETE /posts/{id}  -> eliminar             (DELETE)
 * "suspend" = se ejecuta con corrutinas, sin congelar la pantalla.
 */
interface ApiService {

    // GET: trae todos los posts
    @GET("posts")
    suspend fun getPosts(): List<Post>

    // POST: envía un post nuevo en el cuerpo (@Body) de la petición
    @POST("posts")
    suspend fun createPost(@Body post: Post): Post

    // PUT: reemplaza el post con ese id (@Path pone el id dentro de la URL)
    @PUT("posts/{id}")
    suspend fun updatePost(@Path("id") id: Int, @Body post: Post): Post

    // DELETE: elimina el post con ese id. La API no devuelve datos,
    // por eso usamos Response<Unit> para revisar solo si salió bien (código 200).
    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Int): Response<Unit>
}
