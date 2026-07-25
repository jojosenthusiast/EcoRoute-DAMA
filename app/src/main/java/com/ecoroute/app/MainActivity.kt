package com.ecoroute.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        LocalStorage.inicializar(applicationContext)
        lifecycleScope.launch { LocalStorage.cargarDatosIniciales() }
        enableEdgeToEdge()
        setContent {
            val vista = LocalView.current
            val modoOscuro = AppState.modoOscuro
            SideEffect {
                WindowCompat.getInsetsController(window, vista).apply {
                    isAppearanceLightStatusBars = !modoOscuro
                    isAppearanceLightNavigationBars = !modoOscuro
                }
            }
            EcoRouteTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding(), color = Fondo) {
                    AppNav()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun AppNav() {
    val nav: NavHostController = rememberNavController()
    val scope = rememberCoroutineScope()

    fun irTab(ruta: String) {
        nav.navigate(ruta) {
            launchSingleTop = true
            popUpTo(if (AppState.rol == "vecino") "vecinoHome" else "recoHome") { inclusive = false }
        }
    }

    NavHost(navController = nav, startDestination = "bienvenida") {
        composable("bienvenida") {
            BienvenidaScreen(
                onLogin = { nav.navigate("login") },
                onCrearCuenta = { nav.navigate("rolRegistro") }
            )
        }
        composable("login") {
            LoginScreen(
                onEntrar = {
                    val destino = if (AppState.rol == "vecino") "vecinoHome" else "recoHome"
                    nav.navigate(destino) { popUpTo("bienvenida") { inclusive = true } }
                },
                onCrearCuenta = { nav.navigate("rolRegistro") },
                onRecuperar = { nav.navigate("recuperar") }
            )
        }
        composable("recuperar") {
            RecuperarContrasenaScreen(onBack = { nav.popBackStack() })
        }
        composable("rolRegistro") {
            ConfiguraExperienciaScreen(
                onBack = { nav.popBackStack() },
                onContinuar = { nav.navigate("registro") }
            )
        }
        composable("registro") {
            RegistroScreen(
                onBack = { nav.popBackStack() },
                onRegistrado = { nav.navigate("permisos") { popUpTo("bienvenida") } }
            )
        }
        composable("permisos") {
            PermisosScreen(
                onBack = { nav.popBackStack() },
                onContinuar = {
                    val destino = if (AppState.rol == "vecino") "vecinoHome" else "recoHome"
                    nav.navigate(destino) { popUpTo("login") { inclusive = true } }
                }
            )
        }

        composable("vecinoHome") { HomeVecinoScreen(onNav = ::irTab) }
        composable("registrar") {
            RegistrarReciclajeScreen(
                onBack = { nav.popBackStack() },
                onContinuar = { nav.navigate("detalles") }
            )
        }
        composable("detalles") {
            DetallesRecoleccionScreen(
                onBack = { nav.popBackStack() },
                onRevisar = { nav.navigate("confirmarSolicitud") }
            )
        }
        composable("confirmarSolicitud") {
            ConfirmarSolicitudScreen(
                onBack = { nav.popBackStack() },
                onEnviar = {
                    nav.navigate("estado") { popUpTo("vecinoHome") { inclusive = false } }
                }
            )
        }
        composable("estado") {
            EstadoSolicitudScreen(
                onBack = { nav.popBackStack() },
                onNav = ::irTab
            )
        }
        composable("historial") {
            HistorialScreen(
                onBack = { nav.popBackStack() },
                onNav = ::irTab
            )
        }
        composable("perfil") {
            PerfilScreen(
                onNav = ::irTab,
                onCerrarSesion = {
                    AppState.usuario = ""
                    AppState.usuarioId = ""
                    nav.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable("recoHome") {
            RutaDeHoyScreen(
                onNav = ::irTab,
                onIniciar = { nav.navigate("paradas") }
            )
        }
        composable("paradas") {
            ParadasScreen(
                onBack = { nav.popBackStack() },
                onNav = ::irTab,
                onParada = { i ->
                    AppState.paradaActual = i
                    nav.navigate("parada")
                }
            )
        }
        composable("parada") {
            ParadaDetalleScreen(
                indice = AppState.paradaActual,
                onBack = { nav.popBackStack() },
                onNav = ::irTab,
                onBrujula = { nav.navigate("brujula") },
                onRecolectada = { nav.navigate("confirmarReco") }
            )
        }
        composable("brujula") {
            BrujulaScreen(
                indice = AppState.paradaActual,
                onBack = { nav.popBackStack() },
                onVerDetalles = { nav.popBackStack() },
                onLlegue = { nav.navigate("confirmarReco") }
            )
        }
        composable("confirmarReco") {
            ConfirmarRecoleccionScreen(
                indice = AppState.paradaActual,
                onBack = { nav.popBackStack() },
                onGuardar = {
                    val quedan = AppState.paradas.any { !it.recolectada }
                    if (quedan) {
                        nav.navigate("paradas") { popUpTo("recoHome") { inclusive = false } }
                    } else {
                        nav.navigate("completada") { popUpTo("recoHome") { inclusive = false } }
                    }
                }
            )
        }
        composable("completada") {
            RutaCompletadaScreen(
                onBack = { nav.popBackStack() },
                onNav = ::irTab,
                onVolver = {
                    nav.navigate("recoHome") { popUpTo("recoHome") { inclusive = true } }
                }
            )
        }
    }
}
