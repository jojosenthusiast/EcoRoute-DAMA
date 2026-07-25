package com.ecoroute.app

import android.content.Context
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

val tabsRecolector = listOf(
    TabItem("Ruta", Icons.Outlined.Route, "recoHome"),
    TabItem("Paradas", Icons.Outlined.Description, "paradas"),
    TabItem("Historial", Icons.Outlined.Schedule, "historial"),
    TabItem("Perfil", Icons.Outlined.Person, "perfil")
)

@Composable
fun RutaDeHoyScreen(onNav: (String) -> Unit, onIniciar: () -> Unit) {
    val summary = routeSummary(AppState.paradas)
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            Text("Ruta de hoy", color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 14.dp))
            Text("Hola, ${AppState.usuario.ifBlank { "recolector" }}", color = Navy, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("La ruta está lista para comenzar", color = GrisTexto, fontSize = 16.sp)
            Spacer(Modifier.height(18.dp))
            Column(modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(20.dp)).padding(14.dp)) {
                MapaReal(
                    puntos = AppState.paradas.map { PuntoMapa(it.nombre, it.latitud, it.longitud) },
                    modifier = Modifier.fillMaxWidth().height(130.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("Ruta Centro Norte", color = TextoPrincipal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("${summary.total} paradas", color = GrisTexto, fontSize = 14.sp)
            }
            Spacer(Modifier.height(18.dp))
            BotonVerde("Iniciar ruta", icono = Icons.Filled.KeyboardArrowUp) { onIniciar() }
            Spacer(Modifier.height(22.dp))
            Text("Resumen", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaResumen("${summary.total}", "paradas", Modifier.weight(1f))
                TarjetaResumen("${summary.pending}", "pendientes", Modifier.weight(1f))
                TarjetaResumen("${summary.completed}", "completadas", Modifier.weight(1f))
            }
            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(VerdeClaro, RoundedCornerShape(20.dp)).padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Explore, contentDescription = null, tint = TextoPrincipal, modifier = Modifier.size(56.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Modo de bajo consumo", color = TextoPrincipal, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("La brújula directa reduce el uso continuo del mapa", color = TextoSecundario, fontSize = 15.sp, lineHeight = 22.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        BarraInferior(tabsRecolector, "recoHome", onNav)
    }
}

@Composable
fun TarjetaResumen(valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(Superficie, RoundedCornerShape(16.dp)).padding(vertical = 20.dp, horizontal = 14.dp)
    ) {
        Text(valor, color = Color(0xFF1B7A2F), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(etiqueta, color = GrisTexto, fontSize = 14.sp)
    }
}

@Composable
fun ParadasScreen(onBack: () -> Unit, onNav: (String) -> Unit, onParada: (Int) -> Unit) {
    var busqueda by remember { mutableStateOf("") }
    val pendientes = AppState.paradas.count { !it.recolectada }
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var locationResult by remember(contexto) { mutableStateOf(lastKnownLocationResult(contexto)) }
    DisposableEffect(lifecycleOwner) {
        var cancelarSeguimiento: (() -> Unit)? = null
        var active = false

        fun iniciarSeguimiento() {
            if (active) return
            active = true
            locationResult = lastKnownLocationResult(contexto)
            cancelarSeguimiento = observeLocationUpdates(
                context = contexto,
                minTimeMillis = if (AppState.ahorroBateria) 2_500L else 1_000L,
                minDistanceMeters = if (AppState.ahorroBateria) 2.5f else 0f
            ) { resultado -> locationResult = resultado }
        }

        fun detenerSeguimiento() {
            if (!active) return
            active = false
            cancelarSeguimiento?.invoke()
            cancelarSeguimiento = null
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> iniciarSeguimiento()
                Lifecycle.Event.ON_PAUSE -> detenerSeguimiento()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            iniciarSeguimiento()
        }
        onDispose {
            detenerSeguimiento()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val ubicacionActual = (locationResult as? LocationResult.Available)?.point
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            BarraTitulo("Paradas")
            Spacer(Modifier.height(4.dp))
            Text("$pendientes paradas pendientes", color = Navy, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Paradas guardadas en el orden de la ruta local.", color = GrisTexto, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar dirección o material", color = GrisTexto) },
                trailingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextoPrincipal) },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Superficie,
                    unfocusedContainerColor = Superficie,
                    focusedBorderColor = VerdeBoton,
                    unfocusedBorderColor = Borde
                )
            )
            Spacer(Modifier.height(20.dp))
            AppState.paradas.forEachIndexed { i, p ->
                if (busqueda.isBlank() || p.material.contains(busqueda, true) || p.direccion.contains(busqueda, true) || p.nombre.contains(busqueda, true)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Superficie, RoundedCornerShape(20.dp))
                            .semantics { role = Role.Button }
                            .let { if (p.recolectada) it else it.clickable { onParada(i) } }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(if (i == 0 && !p.recolectada) VerdeBoton else GrisChip, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (p.recolectada) Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeBoton)
                            else Text(
                                "${i + 1}",
                                color = if (i == 0 && !p.recolectada) SobreVerdeBoton else VerdeBoton,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.nombre, color = TextoPrincipal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(p.material, color = TextoSecundario, fontSize = 14.sp)
                            ubicacionActual?.let { origen ->
                                Text(
                                    "A ${formatDistanceKm(distanceKm(origen, GeoPoint(p.latitud, p.longitud)))}",
                                    color = TextoSecundario,
                                    fontSize = 13.sp
                                )
                            }
                            if (p.solicitudId != null) {
                                Text(
                                    "Solicitud real de vecino",
                                    color = VerdeBoton,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        when {
                                            p.horario == "Sin restriccion" -> GrisChip
                                            p.horarioVerde -> VerdeClaro
                                            else -> AmarilloChip
                                        },
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    p.horario,
                                    color = when {
                                        p.horario == "Sin restriccion" -> Color(0xFF9CA3AF)
                                        p.horarioVerde -> VerdeBoton
                                        else -> Color(0xFFB88A00)
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = GrisTexto)
                    }
                    Spacer(Modifier.height(18.dp))
                }
            }
        }
        BarraInferior(tabsRecolector, "paradas", onNav)
    }
}

@Composable
fun ParadaDetalleScreen(indice: Int, onBack: () -> Unit, onNav: (String) -> Unit, onBrujula: () -> Unit, onRecolectada: () -> Unit) {
    val p = AppState.paradas[indice]
    val contexto = LocalContext.current
    var miUbicacion by remember { mutableStateOf<org.osmdroid.util.GeoPoint?>(null) }
    DisposableEffect(indice) {
        val cancelar = requestFreshLocation(contexto) { resultado ->
            if (resultado is LocationResult.Available) {
                miUbicacion = org.osmdroid.util.GeoPoint(resultado.point.latitude, resultado.point.longitude)
            }
        }
        onDispose { cancelar() }
    }
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            BarraTitulo("Parada ${indice + 1} de ${AppState.paradas.size}", onBack = onBack)
            Spacer(Modifier.height(4.dp))
            MapaReal(
                puntos = listOf(PuntoMapa(p.nombre, p.latitud, p.longitud)),
                modifier = Modifier.fillMaxWidth(),
                miUbicacion = miUbicacion,
                expandible = true
            )
            Spacer(Modifier.height(18.dp))
            Column(modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(18.dp)).padding(18.dp)) {
                Text(p.nombre, color = Navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(p.direccion, color = GrisTexto, fontSize = 15.sp)
                if (p.solicitudId != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Solicitud real de vecino",
                        color = VerdeBoton,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Borde)
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(50.dp).background(VerdeClaro, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.LocalDrink, contentDescription = null, tint = Color(0xFF1B7A2F)) }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.material, color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(p.detalle, color = GrisTexto, fontSize = 14.sp)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Referencia", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().background(GrisCampo, RoundedCornerShape(18.dp)).padding(18.dp)) {
                Text(p.referencia, color = Navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, lineHeight = 26.sp)
            }
            Spacer(Modifier.height(28.dp))
            BotonVerde("Navegar con brújula", icono = Icons.Outlined.Navigation) { onBrujula() }
            Spacer(Modifier.height(14.dp))
            if (p.recolectada) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VerdeClaro, RoundedCornerShape(28.dp))
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = VerdeBoton)
                    Spacer(Modifier.width(8.dp))
                    Text("Ya recolectada", color = VerdeBoton, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                BotonBlanco("Marcar como recolectada") { onRecolectada() }
            }
            Spacer(Modifier.height(20.dp))
        }
        BarraInferior(tabsRecolector, "paradas", onNav)
    }
}

@Composable
fun BrujulaScreen(indice: Int, onBack: () -> Unit, onVerDetalles: () -> Unit, onLlegue: () -> Unit) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var azimut by remember { mutableStateOf<Float?>(null) }
    var locationResult by remember(contexto) { mutableStateOf(lastKnownLocationResult(contexto)) }
    var actualizacionesGps by remember { mutableIntStateOf(0) }
    var sensorRegistered by remember { mutableStateOf<Boolean?>(null) }
    var necesitaCalibracion by remember { mutableStateOf(false) }
    val sensorManager = contexto.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val delaySensor = if (AppState.ahorroBateria) SensorManager.SENSOR_DELAY_NORMAL else SensorManager.SENSOR_DELAY_UI
    val controller = remember(sensorManager, delaySensor) {
        CompassSensorController(sensorManager, delaySensor, onCalibrationNeeded = { necesitaCalibracion = it }) { azimut = it }
    }
    DisposableEffect(lifecycleOwner, controller) {
        var cancelarSeguimiento: (() -> Unit)? = null
        var active = false

        fun iniciarSeguimiento() {
            if (active) return
            active = true
            locationResult = lastKnownLocationResult(contexto)
            actualizacionesGps = 0
            cancelarSeguimiento = observeLocationUpdates(
                context = contexto,
                minTimeMillis = if (AppState.ahorroBateria) 2_500L else 1_000L,
                minDistanceMeters = if (AppState.ahorroBateria) 2.5f else 0f
            ) { resultado ->
                locationResult = resultado
                if (resultado is LocationResult.Available) actualizacionesGps++
            }
            sensorRegistered = controller.onResume()
        }

        fun detenerSeguimiento() {
            if (!active) return
            active = false
            controller.onPause()
            cancelarSeguimiento?.invoke()
            cancelarSeguimiento = null
            azimut = null
            sensorRegistered = null
            necesitaCalibracion = false
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    azimut = null
                    necesitaCalibracion = false
                    iniciarSeguimiento()
                }
                Lifecycle.Event.ON_PAUSE -> detenerSeguimiento()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            iniciarSeguimiento()
        }
        onDispose {
            detenerSeguimiento()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val p = AppState.paradas[indice]
    val uiState = compassUiState(locationResult, controller.sensorsAvailable, sensorRegistered, azimut, p.nombre, necesitaCalibracion)
    val ubicacionActual = (locationResult as? LocationResult.Available)
    val distanciaActual = ubicacionActual?.point?.let { origen ->
        formatDistanceKm(distanceKm(origen, GeoPoint(p.latitud, p.longitud)))
    }
    val guidance = if (uiState.guidanceReady) {
        ubicacionActual?.point?.let { origin ->
            azimut?.let { heading ->
                destinationGuidance(
                    origin = origin,
                    destination = GeoPoint(p.latitud, p.longitud),
                    heading = heading
                )
            }
        }
    } else {
        null
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        BarraTitulo("Navegación directa", onBack = onBack)
        Text(
            "Parada ${indice + 1} -${p.nombre}",
            color = TextoSecundario, fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Sostené el teléfono parado (vertical), no acostado, para que la brújula lea bien.",
            color = TextoSecundario,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.CenterHorizontally)
                .clearAndSetSemantics {
                    contentDescription = guidance?.let {
                        "Brújula hacia ${p.nombre}, ${it.angleText}"
                    } ?: "Brújula. ${uiState.message}"
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radio = size.minDimension / 2f
                val centro = Offset(size.width / 2f, size.height / 2f)
                drawCircle(Superficie, radio, centro)
                drawCircle(VerdeSuave, radio * 0.86f, centro)
                for (i in 0 until 12) {
                    val ang = Math.toRadians(i * 30.0)
                    val ext = radio * 0.84f
                    val inte = radio * 0.74f
                    drawLine(
                        Color(0xFF6B7280),
                        Offset(centro.x + (ext * sin(ang)).toFloat(), centro.y - (ext * cos(ang)).toFloat()),
                        Offset(centro.x + (inte * sin(ang)).toFloat(), centro.y - (inte * cos(ang)).toFloat()),
                        strokeWidth = 4f
                    )
                }
                guidance?.let {
                    rotate(it.rotation, centro) {
                        val aguja = Path().apply {
                            moveTo(centro.x, centro.y - radio * 0.62f)
                            lineTo(centro.x - radio * 0.09f, centro.y + radio * 0.06f)
                            lineTo(centro.x + radio * 0.04f, centro.y + radio * 0.06f)
                            close()
                        }
                        drawPath(aguja, Color(0xFF14532D))
                    }
                }
                drawCircle(Color(0xFFF4D525), 10f, centro)
            }
        }
        Spacer(Modifier.height(24.dp))
        guidance?.let {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(Superficie, RoundedCornerShape(16.dp))
                    .padding(horizontal = 44.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ángulo hacia la parada", color = TextoSecundario, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(it.angleText, color = Navy, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                distanciaActual?.let { distancia ->
                    Spacer(Modifier.height(4.dp))
                    Text("Distancia aproximada: $distancia", color = TextoSecundario, fontSize = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(VerdeClaro, RoundedCornerShape(18.dp)).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Explore, contentDescription = null, tint = TextoPrincipal, modifier = Modifier.size(50.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Navegación de bajo consumo", color = TextoPrincipal, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(uiState.message, color = TextoSecundario, fontSize = 15.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    when (locationResult) {
                        is LocationResult.Available -> {
                            val precision = ubicacionActual?.accuracyMeters?.let { " · precisión ${it.toInt()} m" }.orEmpty()
                            "GPS activo · $actualizacionesGps actualizaciones$precision"
                        }
                        LocationResult.PermissionRequired -> "GPS sin permiso"
                        LocationResult.Unavailable -> "GPS sin señal o desactivado"
                    },
                    color = TextoSecundario,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "Para probar: sostén el teléfono vertical, gira lentamente y detente antes de revisar la pantalla mientras caminas.",
            color = TextoSecundario,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(Superficie, RoundedCornerShape(14.dp))
                .padding(14.dp)
        )
        Spacer(Modifier.height(30.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .background(Superficie, RoundedCornerShape(29.dp))
                    .semantics { role = Role.Button }
                    .clickable { onVerDetalles() },
                contentAlignment = Alignment.Center
            ) { Text("Ver detalles", color = VerdeBoton, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .background(VerdeBoton, RoundedCornerShape(29.dp))
                    .semantics { role = Role.Button }
                    .clickable { onLlegue() },
                contentAlignment = Alignment.Center
            ) { Text("Llegué", color = SobreVerdeBoton, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun ConfirmarRecoleccionScreen(indice: Int, onBack: () -> Unit, onGuardar: () -> Unit) {
    var bolsas by remember { mutableIntStateOf(2) }
    var nota by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val p = AppState.paradas[indice]
    val materiales = p.material.split(" y ").map { it.replaceFirstChar { c -> c.uppercase() } }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        BarraTitulo("Confirmar recolección", onBack = onBack)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier.size(96.dp).background(VerdeCheck, CircleShape).align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(52.dp)) }
        Spacer(Modifier.height(16.dp))
        Text("¿Recolección completa?", color = TextoPrincipal, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text("Verifica los datos para la bitácora local", color = TextoSecundario, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(22.dp))
        Text("Material recibido", color = TextoPrincipal, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        materiales.forEach { m ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Superficie, RoundedCornerShape(28.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(34.dp).background(VerdeCheck, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(14.dp))
                Text(m, color = TextoPrincipal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text("Volumen real", color = TextoPrincipal, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(28.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(GrisChip, CircleShape).semantics { role = Role.Button }.clickable { if (bolsas > 1) bolsas-- },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Remove, contentDescription = "Disminuir cantidad de bolsas", tint = TextoPrincipal) }
            Text("$bolsas bolsas", color = TextoPrincipal, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Box(
                modifier = Modifier.size(48.dp).background(GrisChip, CircleShape).semantics { role = Role.Button }.clickable { bolsas++ },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Add, contentDescription = "Aumentar cantidad de bolsas", tint = TextoPrincipal) }
        }
        Spacer(Modifier.height(20.dp))
        Text("Nota opcional", color = TextoPrincipal, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = nota,
            onValueChange = { nota = it },
            modifier = Modifier.fillMaxWidth().height(110.dp),
            placeholder = { Text("Ej.: Material húmedo o bolsa rota", color = GrisTexto) },
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Superficie,
                unfocusedContainerColor = Superficie,
                focusedBorderColor = VerdeBoton,
                unfocusedBorderColor = Borde
            )
        )
        Spacer(Modifier.height(26.dp))
        error?.let {
            Text(it, color = Color(0xFFB3261E), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
        }
        BotonVerde(if (guardando) "Guardando..." else "Guardar y continuar") {
            if (!guardando) {
                guardando = true
                error = null
                scope.launch {
                    try {
                        LocalStorage.guardarParadaRecolectada(indice, bolsas)
                        AppState.bolsasReales = bolsas
                        AppState.notaRecolector = nota
                        onGuardar()
                    } catch (cancellation: kotlinx.coroutines.CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        error = "No se pudo guardar la recolección."
                    } finally {
                        guardando = false
                    }
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun RutaCompletadaScreen(onBack: () -> Unit, onNav: (String) -> Unit, onVolver: () -> Unit) {
    val summary = routeSummary(AppState.paradas)
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            BarraTitulo("Ruta completada", onBack = onBack)
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier.size(100.dp).background(VerdeCheck, CircleShape).align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp)) }
            Spacer(Modifier.height(16.dp))
            Text("Buen trabajo, ${AppState.usuario.ifBlank { "recolector" }}", color = TextoPrincipal, fontSize = 27.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text("Bitácora guardada", color = TextoSecundario, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(22.dp))
            Column(modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(22.dp)).padding(20.dp)) {
                Text("Resumen de la ruta", color = Navy, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${summary.total}", color = Color(0xFF1B7A2F), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("totales", color = GrisTexto, fontSize = 15.sp)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${summary.pending}", color = Color(0xFF1B7A2F), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("pendientes", color = GrisTexto, fontSize = 15.sp)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${summary.completed}", color = Color(0xFF1B7A2F), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("completadas", color = GrisTexto, fontSize = 15.sp)
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
            BotonVerde("Volver al inicio") { onVolver() }
            Spacer(Modifier.height(20.dp))
        }
        BarraInferior(tabsRecolector, "recoHome", onNav)
    }
}
