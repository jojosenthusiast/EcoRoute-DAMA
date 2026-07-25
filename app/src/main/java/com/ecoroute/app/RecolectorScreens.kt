package com.ecoroute.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

val tabsRecolector = listOf(
    TabItem("Ruta", Icons.Outlined.Route, "recoHome"),
    TabItem("Solicitudes", Icons.Outlined.Description, "paradas"),
    TabItem("Historial", Icons.Outlined.Schedule, "historial"),
    TabItem("Perfil", Icons.Outlined.Person, "perfil")
)

@Composable
fun RutaDeHoyScreen(onNav: (String) -> Unit, onIniciar: () -> Unit, onCerrar: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(GrisChip, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Navy) }
                Spacer(Modifier.width(14.dp))
                Text("Ruta de hoy", color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.size(40.dp).background(Superficie, CircleShape).clickable { onCerrar() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Close, contentDescription = null, tint = TextoPrincipal) }
            }
            Text("Hola, ${AppState.usuario.ifBlank { "recolector" }}", color = Navy, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("La ruta esta lista para comenzar", color = GrisTexto, fontSize = 16.sp)
            Spacer(Modifier.height(18.dp))
            Column(modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(20.dp)).padding(14.dp)) {
                MapaReal(
                    puntos = AppState.paradas.map { PuntoMapa(it.nombre, it.latitud, it.longitud) },
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    mostrarRuta = true
                )
                Spacer(Modifier.height(12.dp))
                Text("Ruta Centro Norte", color = TextoPrincipal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("18 paradas - 4.2 km - 1h 20 min", color = GrisTexto, fontSize = 14.sp)
            }
            Spacer(Modifier.height(18.dp))
            BotonVerde("Iniciar ruta", icono = Icons.Filled.KeyboardArrowUp) { onIniciar() }
            Spacer(Modifier.height(22.dp))
            Text("Resumen", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaResumen("8", "paradas", Modifier.weight(1f))
                TarjetaResumen("23", "bolsas", Modifier.weight(1f))
                TarjetaResumen("62 kg", "estimados", Modifier.weight(1f))
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
                    Text("La brujula directa reduce el uso continuo del mapa", color = TextoSecundario, fontSize = 15.sp, lineHeight = 22.sp)
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
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            BarraTitulo("Paradas", onBack = onBack)
            Spacer(Modifier.height(4.dp))
            Text("$pendientes paradas pendientes", color = Navy, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Ordenes para reducir distancia y tiempo.", color = GrisTexto, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar direccion o material", color = GrisTexto) },
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
                            .clickable { onParada(i) }
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
                            else Text("${i + 1}", color = if (i == 0) Color.White else VerdeBoton, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.nombre, color = TextoPrincipal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(p.material, color = TextoSecundario, fontSize = 14.sp)
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
                        Column(horizontalAlignment = Alignment.End) {
                            Text(p.distancia, color = Color(0xFF1B7A2F), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = GrisTexto)
                        }
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
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            BarraTitulo("Parada ${indice + 1} de 8", onBack = onBack)
            Spacer(Modifier.height(4.dp))
            MapaReal(
                puntos = listOf(PuntoMapa(p.nombre, p.latitud, p.longitud)),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(18.dp))
            Column(modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(18.dp)).padding(18.dp)) {
                Text(p.nombre, color = Navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(p.direccion, color = GrisTexto, fontSize = 15.sp)
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
                    Box(
                        modifier = Modifier.background(VerdeClaro, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                    ) { Text(p.distancia, color = VerdeBoton, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
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
            BotonBlanco("Marcar como recolectada") { onRecolectada() }
            Spacer(Modifier.height(20.dp))
        }
        BarraInferior(tabsRecolector, "paradas", onNav)
    }
}

@Composable
fun BrujulaScreen(indice: Int, onBack: () -> Unit, onVerDetalles: () -> Unit, onLlegue: () -> Unit) {
    val contexto = LocalContext.current
    var azimut by remember { mutableFloatStateOf(0f) }
    DisposableEffect(Unit) {
        val sm = contexto.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                val matriz = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(matriz, e.values)
                val orientacion = FloatArray(3)
                SensorManager.getOrientation(matriz, orientacion)
                azimut = Math.toDegrees(orientacion[0].toDouble()).toFloat()
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        if (sensor != null) sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm.unregisterListener(listener) }
    }
    val p = AppState.paradas[indice]
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        BarraTitulo("Navejación directa", onBack = onBack)
        Text(
            "Parada ${indice + 1} -${p.nombre}",
            color = TextoSecundario, fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(20.dp))
        Box(modifier = Modifier.size(300.dp).align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
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
                rotate(-azimut, centro) {
                    val aguja = Path().apply {
                        moveTo(centro.x, centro.y - radio * 0.62f)
                        lineTo(centro.x - radio * 0.09f, centro.y + radio * 0.06f)
                        lineTo(centro.x + radio * 0.04f, centro.y + radio * 0.06f)
                        close()
                    }
                    drawPath(aguja, Color(0xFF14532D))
                }
                drawCircle(Color(0xFFF4D525), 10f, centro)
            }
            Text("N", color = TextoPrincipal, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 46.dp))
            Text("S", color = TextoPrincipal, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 46.dp))
            Text("E", color = TextoPrincipal, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 46.dp))
            Text("O", color = TextoPrincipal, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 46.dp))
        }
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(Superficie, RoundedCornerShape(16.dp))
                .padding(horizontal = 44.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Segun la flecha", color = TextoSecundario, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("560 M", color = Navy, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(VerdeClaro, RoundedCornerShape(18.dp)).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Explore, contentDescription = null, tint = TextoPrincipal, modifier = Modifier.size(50.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Navegacion de bajo consumo", color = TextoPrincipal, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Orientacion en tiempo real con sensores", color = TextoSecundario, fontSize = 15.sp, lineHeight = 22.sp)
            }
        }
        Spacer(Modifier.height(30.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .background(Superficie, RoundedCornerShape(29.dp))
                    .clickable { onVerDetalles() },
                contentAlignment = Alignment.Center
            ) { Text("Ver detales", color = VerdeBoton, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .background(VerdeBoton, RoundedCornerShape(29.dp))
                    .clickable { onLlegue() },
                contentAlignment = Alignment.Center
            ) { Text("Llegue", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun ConfirmarRecoleccionScreen(indice: Int, onBack: () -> Unit, onGuardar: () -> Unit) {
    var bolsas by remember { mutableIntStateOf(2) }
    var nota by remember { mutableStateOf("") }
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
        BarraTitulo("Confirmar recolecion", onBack = onBack, mostrarMenu = false)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier.size(96.dp).background(VerdeCheck, CircleShape).align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(52.dp)) }
        Spacer(Modifier.height(16.dp))
        Text("¿Recoleccion completa?", color = TextoPrincipal, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text("Verifica los datos para la  bitacora local", color = TextoSecundario, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterHorizontally))
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
                modifier = Modifier.size(38.dp).background(GrisChip, CircleShape).clickable { if (bolsas > 1) bolsas-- },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Remove, contentDescription = null, tint = TextoPrincipal) }
            Text("$bolsas bolsas", color = TextoPrincipal, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Box(
                modifier = Modifier.size(38.dp).background(GrisChip, CircleShape).clickable { bolsas++ },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Add, contentDescription = null, tint = TextoPrincipal) }
        }
        Spacer(Modifier.height(20.dp))
        Text("Nota opcional", color = TextoPrincipal, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = nota,
            onValueChange = { nota = it },
            modifier = Modifier.fillMaxWidth().height(110.dp),
            placeholder = { Text("Ej: Material humedo o bolsa rota", color = GrisTexto) },
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Superficie,
                unfocusedContainerColor = Superficie,
                focusedBorderColor = VerdeBoton,
                unfocusedBorderColor = Borde
            )
        )
        Spacer(Modifier.height(26.dp))
        BotonVerde("Guardar y continuar") {
            AppState.bolsasReales = bolsas
            AppState.notaRecolector = nota
            AppState.paradas[indice].recolectada = true
            onGuardar()
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun RutaCompletadaScreen(onBack: () -> Unit, onNav: (String) -> Unit, onVolver: () -> Unit) {
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
            Text("La bitacora de hoy quedo guardada", color = TextoSecundario, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(22.dp))
            Column(modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(22.dp)).padding(20.dp)) {
                Text("Resumen de la ruta", color = Navy, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("8", color = Color(0xFF1B7A2F), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("paradas", color = GrisTexto, fontSize = 15.sp)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("23", color = Color(0xFF1B7A2F), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("bolsas", color = GrisTexto, fontSize = 15.sp)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("62 kg", color = Color(0xFF1B7A2F), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("Recuperados", color = GrisTexto, fontSize = 15.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = Borde)
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Tiempo toal", color = TextoSecundario, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("1 h 42 min", color = TextoSecundario, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(VerdeClaro, RoundedCornerShape(20.dp)).padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(60.dp).background(VerdeCheck, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp)) }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Impacto estimado", color = TextoPrincipal, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("= 87 kg de CO₂ evitados", color = Color(0xFF1B7A2F), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Calculo estimado para el prototipo", color = TextoSecundario, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(30.dp))
            BotonVerde("Volver al inicio") { onVolver() }
            Spacer(Modifier.height(20.dp))
        }
        BarraInferior(tabsRecolector, "recoHome", onNav)
    }
}
