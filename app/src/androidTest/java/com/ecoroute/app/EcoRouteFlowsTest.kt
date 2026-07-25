package com.ecoroute.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EcoRouteFlowsTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetContext get() = instrumentation.targetContext
    private val locationPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val permissionsToRestore = linkedMapOf<String, Boolean>()
    private lateinit var db: EcoRouteDatabase
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        LocalStorage.inicializar(targetContext)
        db = EcoRouteDatabase.obtener(targetContext)
        resetAppState()
        runBlocking {
            db.clearAllTables()
            LocalStorage.guardarModoOscuro(false)
        }
    }

    @After
    fun tearDown() {
        closeActivity()
        restorePermissions()
        runBlocking {
            db.clearAllTables()
            LocalStorage.guardarModoOscuro(false)
        }
        resetAppState()
    }

    @Test
    fun deniedLocationRemainsUsable() {
        val username = "m5t2_collector_denied"
        runBlocking {
            db.paradaDao().guardarTodas(testStops(1))
            LocalStorage.cargarParadas()
            LocalStorage.crearCuenta("M5 Denied", username, "secret1", "recolector")
        }

        try {
            locationPermissions.forEach { rememberAndSetPermission(it, granted = false) }
            launchActivity()
            loginThroughUi(username, "recolector")
            clickText("Iniciar ruta")
            waitForText("M5 Stop 1")
            clickText("M5 Stop 1")
            clickText("Navegar con brújula")

            waitForText("Activa el permiso de ubicación para orientar la brújula hacia esta parada.")
            composeRule.onNode(
                hasText("Ver detalles") and hasClickAction()
            ).assertIsDisplayed()
        } finally {
            closeActivity()
            restorePermissions()
        }
    }

    @Test
    fun neighborCanRegisterAndSeePersistedHistory() {
        val username = "m5t2_neighbor_history"
        grantRegistrationPermissions()
        launchActivity()

        registerThroughUi("M5 Neighbor", username, "vecino")
        clickText("Registrar reciclaje")
        clickText("Plástico")
        clickDescription("Aumentar cantidad de bolsas")
        clickText("Continuar")
        clickText("Hoy")
        clickText("3-5 p.m.")
        clickText("Revisar solicitud")
        clickText("Enviar solicitud")
        waitForText("Estado de solicitud")

        scenario!!.recreate()
        waitForText("Conecta vecinos y recolectores para recuperar materiales de forma ordenada.")
        loginThroughUi(username, "vecino")
        clickText("Historial")
        waitForText("Tus recolecciones")

        composeRule.onNodeWithText("Plástico").assertIsDisplayed()
        composeRule.onNodeWithText("1 bolsas").assertIsDisplayed()
        composeRule.onNodeWithText("En camino").assertIsDisplayed()
    }

    @Test
    fun collectorCanOpenStopUseCompassAndCompleteLog() {
        val username = "m5t2_collector_compass"
        runBlocking {
            db.paradaDao().guardarTodas(testStops(1))
            LocalStorage.cargarParadas()
            LocalStorage.crearCuenta("M5 Collector", username, "secret1", "recolector")
        }
        grantRegistrationPermissions()
        launchActivity()

        loginThroughUi(username, "recolector")
        clickText("Iniciar ruta")
        waitForText("M5 Stop 1")
        clickText("M5 Stop 1")
        clickText("Navegar con brújula")
        waitForText("Navegación directa")

        composeRule.onNode(hasContentDescription("Brújula", substring = true)).assertIsDisplayed()
        val truthfulStates = listOf(
            "Este dispositivo no tiene los sensores requeridos.",
            "Activa el permiso de ubicación para orientar la brújula hacia esta parada.",
            "No hay una ubicación disponible. Activa la ubicación y vuelve a intentarlo.",
            "No se pudo iniciar la brújula.",
            "Esperando orientación de los sensores.",
            "Brújula descalibrada: mové el celular en forma de 8, lejos de imanes o metal.",
            "La flecha apunta hacia M5 Stop 1."
        )
        composeRule.waitUntil(timeoutMillis = 5_000) {
            truthfulStates.any { composeRule.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty() }
        }
        assertTrue(truthfulStates.any { composeRule.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty() })
        clickText("Llegué")
        clickText("Guardar y continuar")
        waitForText("Bitácora guardada")

        clickText("Historial")
        waitForText("Tus recolecciones")
        composeRule.onNodeWithText("Material 1").assertIsDisplayed()
        composeRule.onNodeWithText("2 bolsas").assertIsDisplayed()
        composeRule.onNodeWithText("Completado").assertIsDisplayed()
    }

    @Test
    fun darkModeRestoresFromRoomAfterActivityRecreation() {
        val username = "m5t2_neighbor_dark"
        runBlocking {
            LocalStorage.crearCuenta("M5 Dark", username, "secret1", "vecino")
            LocalStorage.guardarModoOscuro(true)
        }
        launchActivity()
        composeRule.runOnUiThread { AppState.modoOscuro = false }

        scenario!!.recreate()
        waitForText("Conecta vecinos y recolectores para recuperar materiales de forma ordenada.")
        loginThroughUi(username, "vecino")
        clickText("Perfil")

        val darkModeOn = hasContentDescription("Modo oscuro") and SemanticsMatcher.expectValue(
            SemanticsProperties.ToggleableState,
            ToggleableState.On
        )
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(darkModeOn).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(darkModeOn).assertIsOn()
    }

    @Test
    fun collectorSummaryUsesLoadedStopCount() {
        val username = "m5t2_collector_summary"
        val loadedStopCount = runBlocking {
            db.paradaDao().guardarTodas(testStops(3))
            LocalStorage.cargarParadas()
            LocalStorage.crearCuenta("M5 Summary", username, "secret1", "recolector")
            db.paradaDao().listarDeUsuario(AppState.usuarioId).size
        }
        launchActivity()

        loginThroughUi(username, "recolector")

        composeRule.onNodeWithText("$loadedStopCount paradas").assertIsDisplayed()
        composeRule.onNodeWithText("Resumen").assertIsDisplayed()
        composeRule.onAllNodesWithText(loadedStopCount.toString())[0].assertIsDisplayed()
    }

    private fun launchActivity() {
        check(scenario == null)
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Conecta vecinos y recolectores para recuperar materiales de forma ordenada.")
    }

    private fun closeActivity() {
        scenario?.close()
        scenario = null
    }

    private fun registerThroughUi(name: String, username: String, role: String) {
        clickText("Crear cuenta")
        clickText(if (role == "vecino") "Soy vecino" else "Soy recolector")
        clickText("Continuar")
        inputText("Tu nombre", name)
        inputText("Elige un usuario", username)
        inputText("Mínimo 6 caracteres", "secret1")
        inputText("Repite tu contraseña", "secret1")
        clickText("Crear cuenta")
        waitForText("Permisos necesarios")
        clickText("Permitir y continuar")
        waitForText(if (role == "vecino") "Tu reciclaje de hoy suma." else "La ruta está lista para comenzar")
    }

    private fun loginThroughUi(username: String, role: String) {
        clickText("Iniciar sesión")
        inputText("Ingrese su usuario", username)
        inputText("Ingrese su contraseña", "secret1")
        clickText("Iniciar sesión")
        waitForText(if (role == "recolector") "La ruta está lista para comenzar" else "Tu reciclaje de hoy suma.")
    }

    private fun clickText(text: String) {
        composeRule.onNode(hasText(text) and hasClickAction()).performClick()
    }

    private fun clickDescription(description: String) {
        composeRule.onNode(hasContentDescription(description) and hasClickAction()).performClick()
    }

    private fun inputText(placeholder: String, value: String) {
        composeRule.onNode(hasText(placeholder) and hasSetTextAction()).performTextInput(value)
    }

    private fun grantRegistrationPermissions() {
        locationPermissions.forEach { rememberAndSetPermission(it, granted = true) }
        if (Build.VERSION.SDK_INT >= 33) {
            rememberAndSetPermission(Manifest.permission.POST_NOTIFICATIONS, granted = true)
        }
    }

    private fun rememberAndSetPermission(permission: String, granted: Boolean) {
        permissionsToRestore.putIfAbsent(permission, isPermissionGranted(permission))
        setPermission(permission, granted)
    }

    private fun restorePermissions() {
        val previousStates = permissionsToRestore.toMap()
        permissionsToRestore.clear()
        previousStates.forEach { (permission, granted) -> setPermission(permission, granted) }
    }

    private fun setPermission(permission: String, granted: Boolean) {
        if (isPermissionGranted(permission) != granted) {
            if (granted) {
                instrumentation.uiAutomation.grantRuntimePermission(targetContext.packageName, permission)
            } else {
                instrumentation.uiAutomation.revokeRuntimePermission(targetContext.packageName, permission)
            }
        }
        assertEquals("Permission state for $permission", granted, isPermissionGranted(permission))
    }

    private fun isPermissionGranted(permission: String): Boolean =
        targetContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun resetAppState() {
        AppState.rol = "vecino"
        AppState.usuario = ""
        AppState.usuarioId = ""
        AppState.modoOscuro = false
        AppState.materiales.clear()
        AppState.bolsas = 0
        AppState.horario = ""
        AppState.referencia = ""
        AppState.autorizoUbicacion = false
        AppState.solicitudEnviada = false
        AppState.paradaActual = 0
        AppState.bolsasReales = 2
        AppState.notaRecolector = ""
        AppState.paradas.clear()
    }

    private fun testStops(count: Int) = (1..count).map { index ->
        ParadaEntity(
            usuarioId = "recolector_local",
            id = index,
            nombre = "M5 Stop $index",
            direccion = "M5 Address $index",
            material = "Material $index",
            detalle = "$index bags",
            distancia = "$index km",
            horario = "Sin restriccion",
            horarioVerde = false,
            referencia = "M5 Reference $index",
            latitud = 13.9946 + index / 1000.0,
            longitud = -89.5597 - index / 1000.0,
            recolectada = false
        )
    }
}
