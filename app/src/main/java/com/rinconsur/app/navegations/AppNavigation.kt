package com.rinconsur.app.navegations

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rinconsur.app.repository.models.Parametros
import com.rinconsur.app.view.ui.CheckInScreen
import com.rinconsur.app.view.ui.CheckOutScreen
import com.rinconsur.app.view.ui.EditarRegistroScreen
import com.rinconsur.app.view.ui.HistorialScreen
import com.rinconsur.app.view.ui.HomeScreen
import com.rinconsur.app.view.ui.LoginScreen
import com.rinconsur.app.view.ui.ParametrosScreen
import com.rinconsur.app.view.ui.RegisterScreen
import com.rinconsur.app.view.utils.navEnterTransition
import com.rinconsur.app.view.utils.navExitTransition
import com.rinconsur.app.view.utils.navPopEnterTransition
import com.rinconsur.app.view.utils.navPopExitTransition
import com.rinconsur.app.viewmodels.LoginViewModel
import com.rinconsur.app.viewmodels.ParametrosViewModel
import com.rinconsur.app.viewmodels.RegistroViewModel
import com.rinconsur.app.viewmodels.UserViewModel
import kotlinx.coroutines.launch

/** Rutas de navegación */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "crear_cuenta"
    const val HOME = "home"
    const val CHECK_IN = "check_in"
    const val CHECK_OUT = "check_out"
    const val HISTORIAL = "historial"
    const val PARAMETROS = "parametros"
    const val EDITAR = "editar_registro/{id}"          // ruta con argumento: el id del registro
    fun editar(id: Int) = "editar_registro/$id"        // arma la ruta real, ej: "editar_registro/5"
}

data class MenuItem(val route: String, val label: String, val icon: ImageVector)

/** Opciones del menú lateral */
val menuItems = listOf(
    MenuItem(Routes.HOME, "Inicio", Icons.Default.Home),
    MenuItem(Routes.CHECK_IN, "Registrar entrada", Icons.AutoMirrored.Filled.Login),
    MenuItem(Routes.CHECK_OUT, "Registrar salida", Icons.AutoMirrored.Filled.ExitToApp),
    MenuItem(Routes.HISTORIAL, "Historial", Icons.Default.History),
    MenuItem(Routes.PARAMETROS, "Parámetros del puesto", Icons.Default.Settings)
)

private fun titleFor(route: String?): String = when (route) {
    Routes.REGISTER -> "Crear cuenta"
    Routes.HOME -> "Inicio"
    Routes.CHECK_IN -> "Check-in"
    Routes.CHECK_OUT -> "Check-out"
    Routes.HISTORIAL -> "Historial"
    Routes.PARAMETROS -> "Parámetros"
    Routes.EDITAR -> "Editar registro"
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    userViewModel: UserViewModel,
    registroViewModel: RegistroViewModel,
    parametrosViewModel: ParametrosViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val parametros by parametrosViewModel.parametros.collectAsStateWithLifecycle()

    val isLogin = currentRoute == null || currentRoute == Routes.LOGIN
    val isPublic = isLogin || currentRoute == Routes.REGISTER // pantallas sin sesión
    val isTopLevel = menuItems.any { it.route == currentRoute }

    // Protección de sesión: si Android cerró la app y la reabrió en una pantalla interna
    // sin usuario, se vuelve al Login. Así nunca se registra sin encargado/a.
    LaunchedEffect(loginState.loggedUser, currentRoute) {
        if (loginState.loggedUser == null && !isPublic) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = false }
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isPublic,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("RinconSur", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    loginState.loggedUser?.name ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    loginState.loggedUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = null) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTopLevel(item.route)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        loginViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isLogin) {
                    TopAppBar(
                        title = { Text(titleFor(currentRoute)) },
                        navigationIcon = {
                            if (isTopLevel) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            } else {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                loginViewModel = loginViewModel,
                userViewModel = userViewModel,
                registroViewModel = registroViewModel,
                parametrosViewModel = parametrosViewModel,
                currentUserName = loginState.loggedUser?.name ?: "",
                parametros = parametros,
                onNavigateTopLevel = ::navigateTopLevel
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    loginViewModel: LoginViewModel,
    userViewModel: UserViewModel,
    registroViewModel: RegistroViewModel,
    parametrosViewModel: ParametrosViewModel,
    currentUserName: String,
    parametros: Parametros?,
    onNavigateTopLevel: (String) -> Unit
) {
    // ANIMACIÓN 3 (view/utils/Animations.kt): deslizamiento horizontal entre pantallas
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier,
        enterTransition = navEnterTransition,
        exitTransition = navExitTransition,
        popEnterTransition = navPopEnterTransition,
        popExitTransition = navPopExitTransition
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onCrearCuenta = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(viewModel = userViewModel, onDone = { navController.popBackStack() })
        }
        composable(Routes.HOME) {
            HomeScreen(
                userName = currentUserName,
                viewModel = registroViewModel,
                parametros = parametros,
                onNavigate = onNavigateTopLevel
            )
        }
        composable(Routes.CHECK_IN) {
            CheckInScreen(
                viewModel = registroViewModel,
                encargado = currentUserName,
                parametros = parametros,
                onIrParametros = { onNavigateTopLevel(Routes.PARAMETROS) }
            )
        }
        composable(Routes.CHECK_OUT) {
            CheckOutScreen(viewModel = registroViewModel)
        }
        composable(Routes.HISTORIAL) {
            HistorialScreen(
                viewModel = registroViewModel,
                onEditar = { id -> navController.navigate(Routes.editar(id)) }
            )
        }
        // EDITAR: recibe el id del registro como argumento de navegación
        composable(
            route = Routes.EDITAR,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: 0
            EditarRegistroScreen(
                viewModel = registroViewModel,
                registroId = id,
                onDone = { navController.popBackStack() }
            )
        }
        composable(Routes.PARAMETROS) {
            ParametrosScreen(viewModel = parametrosViewModel)
        }
    }
}
