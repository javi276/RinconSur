package com.rinconsur.app.repository

import com.rinconsur.app.repository.models.Post
import com.rinconsur.app.repository.remote.RetrofitInstance

/**
 * Este repositorio se encarga de acceder a los datos de la API usando Retrofit.
 * El ViewModel le pide los datos al repositorio y NO sabe que existe Retrofit.
 * (Igual que los DAO de Room para los datos locales.)
 */
class PostRepository {

    private val api = RetrofitInstance.api

    // GET: obtiene los posts desde la API
    suspend fun getPosts(): List<Post> = api.getPosts()

    // POST: crea un post nuevo y devuelve lo que respondió la API
    suspend fun crearPost(post: Post): Post = api.createPost(post)

    // PUT: actualiza un post existente
    suspend fun actualizarPost(post: Post): Post = api.updatePost(post.id, post)

    // DELETE: elimina un post. Devuelve true si la API respondió OK (código 2xx)
    suspend fun eliminarPost(id: Int): Boolean = api.deletePost(id).isSuccessful
}
