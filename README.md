# RinconSur Accesos

App Android (Kotlin + Jetpack Compose + Material 3) con arquitectura **MVVM**, **Room**, **corrutinas**, **CameraX**, **vibración**, **Navigation** con menú lateral y **animaciones**.

**Problema:** la Tienda Departamental Rincón Sur tiene 4 accesos y quiere saber cuántas personas entran, por dónde, y cuántas **solo transitan** (salen sin comprar). La persona encargada inicia sesión y registra cada entrada (check-in) y cada salida (check-out).

## Integrantes
- [DEIBER]
- [FRANCISCO SANCHEZ]

Equipo N°: [5] ·

## Abrir y ejecutar
1. Android Studio → *Open* → carpeta `RinconSur`.
2. Esperar el *Gradle Sync* (Gradle 8.13, AGP 8.13.1, Kotlin 2.0.21, JDK 17).
3. Ejecutar en un dispositivo/emulador con cámara (minSdk 26).
4. Login de prueba: **encargado@rinconsur.cl / rincon123** (se crea al abrir la BD, vía `RoomDatabase.Callback`). También se puede crear una cuenta nueva.
5. Recorrido sugerido: Login → Parámetros → Registrar entrada (con foto) → Registrar salida → Historial → Inicio.
6. API Rest: menú lateral → **Comunicados (API Rest)** (necesita internet).

## Estructura (MVVM)
```
app/src/main/java/com/rinconsur/app/
├── MainActivity.kt                (punto de entrada, crea los ViewModels)
├── repository/                    (Model)
│   ├── data/      AppDatabase.kt, UserDao.kt, AccesoDao.kt, ParametrosDao.kt, RegistroDao.kt   (Room)
│   ├── models/    User.kt, Acceso.kt, Parametros.kt, Registro.kt, Post.kt                     (Entidades + modelo API)
│   ├── remote/    ApiService.kt, RetrofitInstance.kt                                          (Retrofit, API Rest)
│   └── PostRepository.kt                                                                      (repositorio de la API)
├── viewmodels/    LoginViewModel.kt, UserViewModel.kt, RegistroViewModel.kt, ParametrosViewModel.kt, PostViewModel.kt, Validaciones.kt
├── navegations/   AppNavigation.kt (Routes, menú lateral, NavHost)
└── view/          theme/Theme.kt
    ├── utils/     CameraCapture.kt, PhotoBox.kt, CamposFormulario.kt, Vibracion.kt, Animations.kt
    └── ui/        LoginScreen.kt, RegisterScreen.kt, HomeScreen.kt,
                   CheckInScreen.kt, CheckOutScreen.kt, HistorialScreen.kt, EditarRegistroScreen.kt, ParametrosScreen.kt,
                   PostScreen.kt
```

## Base de datos (Room / SQLite)
| Tabla | Para qué | Relación |
|---|---|---|
| `users` | Personas encargadas (login) | email único |
| `accesos` | Los 4 accesos de la tienda (datos iniciales) | — |
| `parametros` | Tienda, turno, horario y acceso del puesto (1 fila) | `accesoId` FK → `accesos` |
| `registros` | Cada persona: entrada, salida, compra y foto | `accesoEntradaId` y `accesoSalidaId` FK → `accesos` |

## Requisitos de la evaluación y dónde están
| Requisito | Dónde |
|---|---|
| Diseño Material 3 + navegación | `Theme.kt`; `ModalNavigationDrawer` + `TopAppBar` + `NavHost` en `AppNavigation.kt` |
| Formularios validados con íconos y mensajes | `CamposFormulario.kt` (`CampoTexto`, `Selector`: borde rojo, ícono de error, mensaje; ícono verde si es válido). Formularios: Login, Crear cuenta, Parámetros, Check-in, Check-out, Editar registro |
| Validaciones desde la lógica | `viewmodels/Validaciones.kt`, usadas solo por los ViewModels |
| Gestión de estado | `StateFlow` + `data class` de estado (`LoginUiState`, `UserFormUiState`, `CheckInUiState`, `CheckOutUiState`, `EditarUiState`, `ParametrosUiState`) |
| Persistencia local | Room: DAOs `suspend` + `Flow`; `viewModelScope.launch(Dispatchers.IO)` |
| Recurso nativo 1: cámara | `CameraCapture.kt` (CameraX: Preview + ImageCapture + cambio de lente, permiso en tiempo de ejecución). Foto en Check-in (`PhotoBox`) y mostrada en el Historial |
| Recurso nativo 2: vibración | `Vibracion.kt`: corta al guardar, larga ante errores o login fallido |
| Animación 1 | `AnimacionEntradaFormulario` (fade + slide) en Crear cuenta, Check-in y Parámetros |
| Animación 2 | `Modifier.pulsoYRotacion()` en el logo del Login |
| Animación 3 | Transiciones `slideIntoContainer/slideOutOfContainer` del `NavHost` |
| Animación 4 | `ContadorAnimado` (`AnimatedContent`) en los números del Inicio |
| Otras animaciones | `AnimatedVisibility` (errores, acompañante, avisos), `animateColorAsState` (Check-out), `animateItem` (Historial) |
| API Rest (Guía 14) | Retrofit + Gson + corrutinas contra `https://jsonplaceholder.typicode.com/posts`: `Post.kt` → `ApiService.kt` (GET, POST, PUT, DELETE) → `RetrofitInstance.kt` → `PostRepository.kt` → `PostViewModel.kt` → `PostScreen.kt` (menú "Comunicados (API Rest)"). Permiso `INTERNET` en el Manifest. Detalle en `API-REST.md` |
| CRUD | Crear (check-in), leer (Inicio/Historial), actualizar (check-out, parámetros, **editar registro** con el lápiz del Historial), eliminar (Historial) |

## Notas
- Las fotos se guardan en el almacenamiento interno privado de la app: `/data/data/com.rinconsur.app/files/fotos/registros/img_<timestamp>.jpg`. Se excluyen del respaldo (`res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml`) y se borran al desinstalar.
- BD versión 1 con `fallbackToDestructiveMigration()`.
- Contraseñas en texto plano: solo con fines académicos.
