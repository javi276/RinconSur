package com.rinconsur.app.repository.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton (object) que contiene la configuración de Retrofit.
 * Se crea UNA sola vez para toda la app.
 */
object RetrofitInstance {

    // URL base de la API pública (debe terminar en "/")
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    // Se instancia el servicio de la API una sola vez ("by lazy" = la primera vez que se usa)
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)                                   // URL base de la API
            .addConverterFactory(GsonConverterFactory.create())  // Conversor JSON <-> Post
            .build()
            .create(ApiService::class.java)                      // Implementa la interfaz ApiService
    }
}
