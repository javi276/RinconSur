package com.rinconsur.app.repository.models

/**
 * MODELO de la API Rest (Guía 14).
 * Representa un "post" que llega desde https://jsonplaceholder.typicode.com/posts
 * En RinconSur lo mostramos como "Comunicados" de la tienda.
 * Gson convierte el JSON a esta clase usando los MISMOS nombres de los campos.
 *
 * Ejemplo del JSON:  { "userId": 1, "id": 1, "title": "...", "body": "..." }
 */
data class Post(
    val userId: Int,   // ID del usuario que creó el post
    val id: Int,       // ID del post
    val title: String, // Título del post
    val body: String   // Cuerpo o contenido del post
)
