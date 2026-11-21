# API Rest en RinconSur (Guía 14)

La app ahora tiene una pantalla nueva: **Comunicados (API Rest)** (menú lateral o tarjeta en Inicio).
Trae los "posts" de la API pública **https://jsonplaceholder.typicode.com/posts** con **Retrofit** y deja crear, editar y eliminar.

## Qué se agregó y dónde está

| Paso de la guía | Archivo en RinconSur | Qué hace (simple) |
|---|---|---|
| Permiso de internet | `app/src/main/AndroidManifest.xml` | `<uses-permission android:name="android.permission.INTERNET" />` |
| Dependencias | `gradle/libs.versions.toml` + `app/build.gradle.kts` | Retrofit 2.11.0 y converter-gson 2.11.0 (corrutinas ya estaban) |
| Post.kt (model) | `repository/models/Post.kt` | El "molde" del JSON: userId, id, title, body |
| ApiService.kt (remote) | `repository/remote/ApiService.kt` | La lista de llamadas: **GET, POST, PUT, DELETE** |
| RetrofitInstance.kt (remote) | `repository/remote/RetrofitInstance.kt` | Arma Retrofit una sola vez con la URL base y Gson |
| PostRepository.kt | `repository/PostRepository.kt` | El ViewModel le pide los datos a él (no a Retrofit directo) |
| PostViewModel.kt | `viewmodels/PostViewModel.kt` | Guarda la lista en un `StateFlow`, llama a la API con `viewModelScope.launch`, valida el formulario |
| PostScreen.kt | `view/ui/PostScreen.kt` | Lista con `LazyColumn`, cargando, error + Reintentar, botón Nuevo, lápiz y basurero |
| Modificar MainActivity | `MainActivity.kt` | Crea `postViewModel` y lo pasa a la navegación |
| Navegación | `navegations/AppNavigation.kt` | Ruta `comunicados` + opción en el menú |
| Validación | `viewmodels/Validaciones.kt` | `validarTitulo` y `validarCuerpo` |

Nota: las carpetas siguen el estilo de RinconSur (igual que el proyecto del profe): lo que la guía llama `data/model` está en `repository/models`, `data/remote` en `repository/remote`, `viewmodel` en `viewmodels` y `ui/screens` en `view/ui`.

## Los métodos HTTP (punto 11 de la guía)

| Método | Para qué | En la app |
|---|---|---|
| GET `/posts` | Leer la lista | Al abrir la pantalla y con el botón Recargar |
| POST `/posts` | Crear | Botón **Nuevo** → "Publicar (POST)" |
| PUT `/posts/{id}` | Editar | Lápiz → "Guardar (PUT)" |
| DELETE `/posts/{id}` | Eliminar | Basurero → "Eliminar (DELETE)" |

Ojo: JSONPlaceholder es una API **de prueba**. Responde como si guardara, pero **no guarda de verdad**. Por eso, al recargar (GET) vuelven los 100 posts originales. Eso es normal y se puede explicar así al profe.

## El viaje de un dato (para la defensa)

`PostScreen` (botón) → `PostViewModel` → `PostRepository` → `RetrofitInstance.api` (ApiService) → internet → JSON → Gson lo convierte a `Post` → `StateFlow` → la pantalla se redibuja sola.

## Cómo probar
1. Abrir en Android Studio y hacer **Sync Now** (descarga Retrofit).
2. El emulador debe tener internet.
3. Iniciar sesión (encargado@rinconsur.cl / rincon123) → menú → **Comunicados (API Rest)**.
4. Sin internet aparece "No se pudo conectar con la API" y el botón **Reintentar**.
