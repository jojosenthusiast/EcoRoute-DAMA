package com.ecoroute.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalStorageTest {
    private lateinit var db: EcoRouteDatabase

    @Before
    fun setUp() {
        resetAppState()
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EcoRouteDatabase::class.java
        ).allowMainThreadQueries().build()
        LocalStorage.inicializar(db)
    }

    @After
    fun tearDown() {
        db.close()
        resetAppState()
    }

    @Test
    fun requestPersistsAcrossReload() = runBlocking {
        AppState.usuarioId = "neighbor-1"
        AppState.materiales.clear()
        AppState.materiales.addAll(listOf("Vidrio", "Metal"))
        AppState.bolsas = 3
        LocalStorage.guardarSolicitudActual()

        val stored = db.solicitudDao().obtenerUltimaDeUsuario("neighbor-1")

        assertEquals("Vidrio, Metal", stored?.materiales)
        assertEquals(3, stored?.bolsas)
        val history = db.historialDao().listarDeUsuario("neighbor-1").single()
        assertEquals("Vidrio y Metal", history.material)
        assertEquals("3 bolsas", history.bolsas)
        assertEquals("En camino", history.estado)
        assertTrue(AppState.solicitudEnviada)
    }

    @Test
    fun latestRequestReturnsRoomValues() = runBlocking {
        AppState.usuarioId = "neighbor-2"
        AppState.materiales.clear()
        AppState.materiales.add("Papel")
        AppState.bolsas = 2
        LocalStorage.guardarSolicitudActual()

        val stored = LocalStorage.cargarUltimaSolicitudUsuario()

        assertEquals("Papel", stored?.materiales)
        assertEquals(2, stored?.bolsas)
    }

    @Test
    fun loginWithoutRequestClearsPreviousAccountRequestState() = runBlocking {
        assertTrue(LocalStorage.crearCuenta("Neighbor A", "neighbor-a", "secret", "vecino"))
        AppState.materiales.clear()
        AppState.materiales.add("Vidrio")
        AppState.bolsas = 4
        AppState.horario = "Mañana"
        AppState.referencia = "Casa A"
        LocalStorage.guardarSolicitudActual()
        assertTrue(LocalStorage.crearCuenta("Neighbor B", "neighbor-b", "secret", "vecino"))

        assertEquals("Neighbor A", LocalStorage.iniciarSesion("neighbor-a", "secret"))
        assertEquals("Vidrio", AppState.materiales.single())
        assertEquals("Neighbor B", LocalStorage.iniciarSesion("neighbor-b", "secret"))

        assertTrue(AppState.materiales.isEmpty())
        assertEquals(0, AppState.bolsas)
        assertEquals("", AppState.horario)
        assertEquals("", AppState.referencia)
        assertFalse(AppState.autorizoUbicacion)
        assertFalse(AppState.solicitudEnviada)
    }

    @Test
    fun requestRollsBackWhenHistoryInsertFails() = runBlocking {
        AppState.usuarioId = "neighbor-rollback"
        AppState.materiales.clear()
        AppState.materiales.add("Papel")
        AppState.bolsas = 2
        AppState.solicitudEnviada = false
        db.openHelper.writableDatabase.execSQL(
            """
            CREATE TRIGGER fail_request_history_insert
            BEFORE INSERT ON historial_recolecciones
            BEGIN
                SELECT RAISE(ABORT, 'forced request history failure');
            END
            """.trimIndent()
        )

        val failure = try {
            LocalStorage.guardarSolicitudActual()
            null
        } catch (throwable: Throwable) {
            throwable
        } finally {
            db.openHelper.writableDatabase.execSQL("DROP TRIGGER IF EXISTS fail_request_history_insert")
        }

        assertNotNull(failure)
        assertEquals(null, db.solicitudDao().obtenerUltimaDeUsuario("neighbor-rollback"))
        assertTrue(db.historialDao().listarDeUsuario("neighbor-rollback").isEmpty())
        assertFalse(AppState.solicitudEnviada)
    }

    @Test
    fun stopCompletionPersistsExactLogValues() = runBlocking {
        val seed = with(LocalStorage) { AppState.paradas.first().toEntity("recolector_local", 1) }
        db.paradaDao().guardarTodas(listOf(seed))
        LocalStorage.cargarParadas()

        LocalStorage.guardarParadaRecolectada(0, 4)

        assertTrue(db.paradaDao().listarDeUsuario("recolector_local").single().recolectada)
        val log = db.historialDao().listarDeUsuario("recolector_local").single()
        assertEquals("Plástico y papel", log.material)
        assertEquals("4 bolsas", log.bolsas)
        assertEquals("Completado", log.estado)
    }

    @Test
    fun stopCompletionRollsBackWhenHistoryInsertFails() = runBlocking {
        val seed = with(LocalStorage) { AppState.paradas.first().toEntity("recolector_local", 1) }
        db.paradaDao().guardarTodas(listOf(seed))
        LocalStorage.cargarParadas()
        db.openHelper.writableDatabase.execSQL(
            """
            CREATE TRIGGER fail_history_insert
            BEFORE INSERT ON historial_recolecciones
            BEGIN
                SELECT RAISE(ABORT, 'forced history failure');
            END
            """.trimIndent()
        )

        val failure = try {
            LocalStorage.guardarParadaRecolectada(0, 4)
            null
        } catch (throwable: Throwable) {
            throwable
        } finally {
            db.openHelper.writableDatabase.execSQL("DROP TRIGGER IF EXISTS fail_history_insert")
        }

        assertNotNull(failure)
        assertFalse(db.paradaDao().listarDeUsuario("recolector_local").single().recolectada)
        assertFalse(AppState.paradas.single().recolectada)
    }

    private fun resetAppState() {
        AppState.usuarioId = ""
        AppState.materiales.clear()
        AppState.materiales.addAll(listOf("Plástico", "Papel"))
        AppState.bolsas = 2
        AppState.horario = "Hoy 3-5 p.m."
        AppState.referencia = "Portón verde, dejar las bolsas junto al árbol."
        AppState.autorizoUbicacion = true
        AppState.solicitudEnviada = false
        AppState.bolsasReales = 2
        AppState.notaRecolector = ""
        AppState.cargarParadas(listOf(defaultStop()))
    }

    private fun defaultStop() = Parada(
        nombre = "Andrea M.",
        direccion = "Residencial Las Flores, pasaje 4",
        material = "Plástico y papel",
        detalle = "2 bolsas medianas 8-12 kg",
        distancia = "0.6 km",
        horario = "2:00-5:00-p.m",
        horarioVerde = true,
        referencia = "Portón verde, bolsas junto al árbol de mango.",
        latitud = 13.9946,
        longitud = -89.5597
    )
}
