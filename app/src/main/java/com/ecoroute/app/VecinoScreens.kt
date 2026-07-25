package com.ecoroute.app

import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

val tabsVecino = listOf(
    TabItem("Inicio", Icons.Outlined.Home, "vecinoHome"),
    TabItem("Solicitudes", Icons.Outlined.Description, "estado"),
    TabItem("Historial", Icons.Outlined.Schedule, "historial"),
    TabItem("Perfil", Icons.Outlined.Person, "perfil")
)

@Composable
fun MapaCasa(
    modifier: Modifier = Modifier,
    latitud: Double = AppState.LATITUD_CASA,
    longitud: Double = AppState.LONGITUD_CASA,
    expandible: Boolean = false,
    onTocarMapa: ((Double, Double) -> Unit)? = null
) {
    MapaReal(
        puntos = listOf(PuntoMapa("Punto de recolección", latitud, longitud)),
        modifier = modifier.fillMaxWidth(),
        expandible = expandible,
        onTocarMapa = onTocarMapa
    )
}

@Composable
fun HomeVecinoScreen(onNav: (String) -> Unit) {
    var solicitud by remember { mutableStateOf<SolicitudEntity?>(null) }
    var historial by remember { mutableStateOf<List<HistorialEntity>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorCarga by remember { mutableStateOf(false) }
    LaunchedEffect(AppState.usuarioId) {
        cargando = true
        errorCarga = false
        solicitud = null
        historial = emptyList()
        try {
            val solicitudRoom = LocalStorage.cargarUltimaSolicitudUsuario()
            val historialRoom = LocalStorage.cargarHistorialUsuario()
            solicitud = solicitudRoom
            historial = historialRoom
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            errorCarga = true
        }
        cargando = false
    }
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LogoEcoRoute(40)
                Spacer(Modifier.width(12.dp))
                Text("EcoRoute", color = Navy, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
            Text("Buenos días , ${AppState.usuario.ifBlank { "usuario" }}", color = Navy, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Tu reciclaje de hoy suma.", color = GrisTexto, fontSize = 16.sp)
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth().background(Amarillo, RoundedCornerShape(20.dp)).padding(20.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("¿Tienes material listo?", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Text("Registra una bolsa en\nmenos de un minuto.", color = GrisTexto, fontSize = 14.sp, lineHeight = 22.sp)
                        }
                        Box(
                            modifier = Modifier.size(56.dp).background(VerdeClaro, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = Color(0xFF1B7A2F))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .width(250.dp)
                            .height(48.dp)
                            .background(VerdeBoton, RoundedCornerShape(10.dp))
                            .semantics { role = Role.Button }
                            .clickable { onNav("registrar") },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(12.dp))
                        Box(Modifier.size(24.dp).background(Superficie, CircleShape))
                        Spacer(Modifier.width(14.dp))
                        Text("Registrar reciclaje", color = SobreVerdeBoton, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Solicitud activa", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            when {
                cargando -> Text("Cargando solicitud...", color = GrisTexto, fontSize = 14.sp)
                errorCarga -> Text("No se pudo cargar la solicitud.", color = GrisTexto, fontSize = 14.sp)
                solicitud == null -> Text("No tienes una solicitud activa.", color = GrisTexto, fontSize = 14.sp)
                else -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Superficie, RoundedCornerShape(24.dp))
                        .semantics { role = Role.Button }
                        .clickable { onNav("estado") }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(52.dp).background(VerdeClaro, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.LocalDrink, contentDescription = null, tint = Color(0xFF1B7A2F))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(solicitud!!.materiales, color = Navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("${solicitud!!.bolsas} bolsas - ${solicitud!!.horario}", color = GrisTexto, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.background(VerdeClaro, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                            Text(solicitud!!.estado, color = VerdeBoton, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Navy)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Tu impacto", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            when {
                cargando -> Text("Cargando impacto...", color = GrisTexto, fontSize = 14.sp)
                errorCarga -> Text("No se pudo cargar el impacto.", color = GrisTexto, fontSize = 14.sp)
                else -> TarjetaImpacto("${historial.size}", "recolecciones", Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(20.dp))
        }
        BarraInferior(tabsVecino, "vecinoHome", onNav)
    }
}

@Composable
fun TarjetaImpacto(valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Superficie, RoundedCornerShape(50))
            .padding(vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor, color = Color(0xFF1B7A2F), fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(etiqueta, color = GrisTexto, fontSize = 13.sp)
    }
}

@Composable
fun BarraProgreso(progreso: Float) {
    Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFF9AA0AB), RoundedCornerShape(3.dp))) {
        Box(Modifier.fillMaxWidth(progreso).height(6.dp).background(VerdeBoton, RoundedCornerShape(3.dp)))
    }
}

@Composable
fun TarjetaMaterial(nombre: String, icono: ImageVector, seleccionado: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(120.dp)
            .background(if (seleccionado) VerdeClaro else Superficie, RoundedCornerShape(20.dp))
            .semantics {
                role = Role.Button
                selected = seleccionado
            }
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        if (seleccionado) {
            Box(
                modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color(0xFF1B7A2F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Row(modifier = Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = if (seleccionado) Color(0xFF1B7A2F) else Navy, modifier = Modifier.size(38.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(nombre, color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(if (seleccionado) "Seleccionado" else "Agregar", color = if (seleccionado) Navy else GrisTexto, fontSize = 13.sp)
            }
        }
    }
}

val rangosKgPorTamano = linkedMapOf(
    "Pequeña" to (1 to 3),
    "Mediana" to (4 to 6),
    "Grande" to (7 to 10)
)

@Composable
fun RegistrarReciclajeScreen(onBack: () -> Unit, onContinuar: () -> Unit) {
    val seleccion = remember { mutableStateListOf(*AppState.materiales.toTypedArray()) }
    var bolsas by remember { mutableIntStateOf(AppState.bolsas) }
    var tamano by remember { mutableStateOf(AppState.tamanoBolsa) }
    var error by remember { mutableStateOf<String?>(null) }
    fun alternar(m: String) {
        if (seleccion.contains(m)) seleccion.remove(m) else seleccion.add(m)
        error = null
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        BarraTitulo("Registrar reciclaje", onBack = onBack)
        Spacer(Modifier.height(6.dp))
        Text("Materiales", color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Paso 1 de 3 . Selecciona todo lo que aplique.", color = GrisTexto, fontSize = 15.sp)
        Spacer(Modifier.height(14.dp))
        BarraProgreso(0.33f)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TarjetaMaterial("Plástico", Icons.Outlined.LocalDrink, seleccion.contains("Plástico"), Modifier.weight(1f)) { alternar("Plástico") }
            TarjetaMaterial("Papel", Icons.Outlined.Description, seleccion.contains("Papel"), Modifier.weight(1f)) { alternar("Papel") }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TarjetaMaterial("Vidrio", Icons.Outlined.LocalDrink, seleccion.contains("Vidrio"), Modifier.weight(1f)) { alternar("Vidrio") }
            TarjetaMaterial("Metal", Icons.Outlined.Inventory2, seleccion.contains("Metal"), Modifier.weight(1f)) { alternar("Metal") }
        }
        Spacer(Modifier.height(28.dp))
        Text("Tamaño de bolsa", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            rangosKgPorTamano.keys.forEach { t ->
                ChipHorario(t, tamano == t) { tamano = t }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Volumen aproximado", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(20.dp)).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(GrisChip, CircleShape).semantics { role = Role.Button }.clickable {
                    if (bolsas > 1) {
                        bolsas--
                        error = null
                    }
                },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Remove, contentDescription = "Disminuir cantidad de bolsas", tint = Navy) }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val rangoKg = rangosKgPorTamano.getValue(tamano)
                Text("$bolsas bolsas ${tamano.lowercase()}s", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("aprox. ${bolsas * rangoKg.first} - ${bolsas * rangoKg.second} kg", color = GrisTexto, fontSize = 13.sp)
            }
            Box(
                modifier = Modifier.size(48.dp).background(VerdeClaro, CircleShape).semantics { role = Role.Button }.clickable {
                    bolsas++
                    error = null
                },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Add, contentDescription = "Aumentar cantidad de bolsas", tint = VerdeBoton) }
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(GrisCampo, RoundedCornerShape(18.dp)).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(26.dp).background(VerdeClaro, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("i", color = VerdeBoton, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(14.dp))
            Text("No necesitas pesar las bolsas; usa una estimación visual", color = GrisTexto, fontSize = 14.sp, lineHeight = 20.sp)
        }
        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error!!, color = Color(0xFFB3261E), fontSize = 14.sp, lineHeight = 20.sp)
        }
        Spacer(Modifier.height(30.dp))
        BotonVerde("Continuar") {
            error = validateCollection(seleccion, bolsas)
            if (error != null) return@BotonVerde
            AppState.materiales.clear()
            AppState.materiales.addAll(seleccion)
            AppState.bolsas = bolsas
            AppState.tamanoBolsa = tamano
            onContinuar()
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun ChipHorario(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .background(if (seleccionado) VerdeClaro else Superficie, RoundedCornerShape(22.dp))
            .semantics {
                role = Role.Button
                selected = seleccionado
            }
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, color = if (seleccionado) VerdeBoton else Navy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

val diasHorario = listOf("Hoy", "Mañana", "Pasado mañana")
val franjasHorario = listOf("8-10 a.m.", "11 a.m.-1 p.m.", "3-5 p.m.", "5-7 p.m.")

@Composable
fun DetallesRecoleccionScreen(onBack: () -> Unit, onRevisar: () -> Unit) {
    val horarioInicial = AppState.horario
    val franjaConocida = franjasHorario.any { horarioInicial.endsWith(it) }
    var usarOtroHorario by remember { mutableStateOf(horarioInicial.isNotBlank() && !franjaConocida) }
    var dia by remember { mutableStateOf(diasHorario.firstOrNull { horarioInicial.startsWith(it) } ?: diasHorario.first()) }
    var franja by remember { mutableStateOf(franjasHorario.firstOrNull { horarioInicial.endsWith(it) } ?: franjasHorario.first()) }
    var horarioOtro by remember { mutableStateOf(if (usarOtroHorario) horarioInicial else "") }
    var referencia by remember { mutableStateOf(AppState.referencia) }
    var direccion by remember { mutableStateOf(AppState.direccion) }
    var latitud by remember { mutableDoubleStateOf(AppState.latitudSolicitud) }
    var longitud by remember { mutableDoubleStateOf(AppState.longitudSolicitud) }
    var buscandoUbicacion by remember { mutableStateOf(false) }
    var errorUbicacion by remember { mutableStateOf<String?>(null) }
    var cancelarUbicacion by remember { mutableStateOf<(() -> Unit)?>(null) }
    val contexto = LocalContext.current
    DisposableEffect(Unit) {
        onDispose { cancelarUbicacion?.invoke() }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        BarraTitulo("Detalles de recolección", onBack = onBack)
        Spacer(Modifier.height(6.dp))
        Text("¿Dónde y cuándo?", color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Paso 2 de 3 . Confirma los datos de la parada.", color = GrisTexto, fontSize = 15.sp)
        Spacer(Modifier.height(14.dp))
        BarraProgreso(0.66f)
        Spacer(Modifier.height(20.dp))
        MapaCasa(
            latitud = latitud,
            longitud = longitud,
            expandible = true,
            onTocarMapa = { lat, lng ->
                latitud = lat
                longitud = lng
            }
        )
        Spacer(Modifier.height(8.dp))
        Text("Toca el mapa para ajustar el punto exacto de recolección.", color = GrisTexto, fontSize = 12.sp)
        Spacer(Modifier.height(20.dp))
        Text("Dirección", color = Navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Superficie,
                unfocusedContainerColor = Superficie,
                focusedBorderColor = VerdeBoton,
                unfocusedBorderColor = Color.Transparent
            )
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VerdeClaro, RoundedCornerShape(14.dp))
                .semantics { role = Role.Button }
                .clickable {
                    errorUbicacion = null
                    buscandoUbicacion = true
                    cancelarUbicacion = requestFreshLocation(contexto) { resultado ->
                        buscandoUbicacion = false
                        when (resultado) {
                            is LocationResult.Available -> {
                                latitud = resultado.point.latitude
                                longitud = resultado.point.longitude
                            }
                            LocationResult.PermissionRequired ->
                                errorUbicacion = "Activa el permiso de ubicación para usar tu posición actual."
                            LocationResult.Unavailable ->
                                errorUbicacion = "No se pudo obtener tu ubicación actual."
                        }
                    }
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = VerdeBoton)
            Spacer(Modifier.width(10.dp))
            Text(
                if (buscandoUbicacion) "Buscando tu ubicación..." else "Usar mi ubicación actual",
                color = VerdeBoton,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (errorUbicacion != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorUbicacion!!, color = Color(0xFFB3261E), fontSize = 13.sp, lineHeight = 18.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text("Horario preferido", color = Navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Text("Día", color = GrisTexto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            diasHorario.forEach { d ->
                ChipHorario(d, !usarOtroHorario && dia == d) {
                    usarOtroHorario = false
                    dia = d
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Franja horaria", color = GrisTexto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            franjasHorario.take(2).forEach { f ->
                ChipHorario(f, !usarOtroHorario && franja == f) {
                    usarOtroHorario = false
                    franja = f
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            franjasHorario.takeLast(2).forEach { f ->
                ChipHorario(f, !usarOtroHorario && franja == f) {
                    usarOtroHorario = false
                    franja = f
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        ChipHorario("Otro horario", usarOtroHorario) { usarOtroHorario = true }
        if (usarOtroHorario) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = horarioOtro,
                onValueChange = { horarioOtro = it },
                placeholder = { Text("Ej: Sábado 9-11 a.m.") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Superficie,
                    unfocusedContainerColor = Superficie,
                    focusedBorderColor = VerdeBoton,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
        Spacer(Modifier.height(20.dp))
        Text("Referencia para el recolector", color = Navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = referencia,
            onValueChange = { referencia = it },
            modifier = Modifier.fillMaxWidth().height(110.dp),
            shape = RoundedCornerShape(18.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Superficie,
                unfocusedContainerColor = Superficie,
                focusedBorderColor = VerdeBoton,
                unfocusedBorderColor = Color.Transparent
            )
        )
        Spacer(Modifier.height(30.dp))
        BotonVerde("Revisar solicitud") {
            AppState.horario = if (usarOtroHorario) horarioOtro.ifBlank { "Otro" } else "$dia $franja"
            AppState.referencia = referencia
            AppState.direccion = direccion.ifBlank { AppState.direccion }
            AppState.latitudSolicitud = latitud
            AppState.longitudSolicitud = longitud
            onRevisar()
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun FilaResumen(clave: String, valor: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
            Text(clave, color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(12.dp))
            Text(
                valor,
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(color = Borde)
    }
}

@Composable
fun ConfirmarSolicitudScreen(onBack: () -> Unit, onEnviar: () -> Unit) {
    var guardando by remember { mutableStateOf(false) }
    var errorGuardar by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        BarraTitulo("Confirmar solicitud", onBack = onBack)
        Spacer(Modifier.height(6.dp))
        Text("Todo listo", color = Navy, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Paso 3 de 3 - Revisa antes de enviar", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        BarraProgreso(1f)
        Spacer(Modifier.height(20.dp))
        Column(modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(20.dp)).padding(18.dp)) {
            Text("Resumen de recolección", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            FilaResumen("Materiales", AppState.materiales.joinToString(", "))
            FilaResumen("Volumen", "${AppState.bolsas} bolsas ${AppState.tamanoBolsa.lowercase()}s")
            FilaResumen("Horario", AppState.horario)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                Text("Ubicación", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(12.dp))
                Text(
                    AppState.direccion,
                    color = Navy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier.fillMaxWidth().background(VerdeClaro, RoundedCornerShape(20.dp)).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ruta inteligente", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).background(Superficie, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Route, contentDescription = null, tint = Navy, modifier = Modifier.size(30.dp)) }
                Spacer(Modifier.width(16.dp))
                Text("Tu solicitud se guardará en este dispositivo.", color = TextoSecundario, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(28.dp))
        BotonVerde(if (guardando) "Guardando..." else "Enviar solicitud") {
            if (guardando) return@BotonVerde
            guardando = true
            errorGuardar = null
            scope.launch {
                try {
                    LocalStorage.guardarSolicitudActual()
                    onEnviar()
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    errorGuardar = "No se pudo guardar la solicitud."
                } finally {
                    guardando = false
                }
            }
        }
        if (errorGuardar != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorGuardar!!, color = Color(0xFFB3261E), fontSize = 14.sp)
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun EstadoSolicitudScreen(onBack: () -> Unit, onNav: (String) -> Unit) {
    var solicitudes by remember { mutableStateOf<List<SolicitudEntity>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorCarga by remember { mutableStateOf(false) }
    LaunchedEffect(AppState.usuarioId) {
        cargando = true
        errorCarga = false
        solicitudes = emptyList()
        try {
            solicitudes = LocalStorage.cargarSolicitudesActivasUsuario()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            errorCarga = true
        }
        cargando = false
    }
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            BarraTitulo("Estado de solicitud", onBack = onBack)
            Spacer(Modifier.height(6.dp))
            when {
                cargando -> Text("Cargando solicitud...", color = GrisTexto, fontSize = 14.sp)
                errorCarga -> Text("No se pudo cargar la solicitud.", color = GrisTexto, fontSize = 14.sp)
                solicitudes.isEmpty() -> Text("No hay una solicitud activa.", color = GrisTexto, fontSize = 14.sp)
                else -> {
                    solicitudes.forEachIndexed { indice, solicitudActual ->
                        val completada = solicitudActual.estado == "Completado"
                        Column(modifier = Modifier.fillMaxWidth().background(VerdeClaro, RoundedCornerShape(20.dp)).padding(20.dp)) {
                            Text(solicitudActual.estado, color = VerdeBoton, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(18.dp))
                            PasoSeguimiento("Solicitud enviada", completado = true, esUltimo = false)
                            PasoSeguimiento(if (completada) "Recolectada" else "En camino", completado = completada, esUltimo = true)
                        }
                        Spacer(Modifier.height(14.dp))
                        Column(modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(20.dp)).padding(18.dp)) {
                            Text("Solicitud actual", color = TextoPrincipal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            FilaResumen("Materiales", solicitudActual.materiales)
                            FilaResumen("Volumen", "${solicitudActual.bolsas} bolsas")
                            FilaResumen("Horario", solicitudActual.horario)
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                                Text("Referencia", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    solicitudActual.referencia,
                                    color = Navy,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        if (indice != solicitudes.lastIndex) Spacer(Modifier.height(26.dp))
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        BarraInferior(tabsVecino, "estado", onNav)
    }
}

@Composable
fun PasoSeguimiento(texto: String, completado: Boolean, esUltimo: Boolean) {
    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(22.dp).background(if (completado) VerdeBoton else GrisChip, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (completado) Icon(Icons.Filled.Check, contentDescription = null, tint = SobreVerdeBoton, modifier = Modifier.size(14.dp))
            }
            if (!esUltimo) {
                Box(modifier = Modifier.width(2.dp).height(28.dp).background(if (completado) VerdeBoton else GrisChip))
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(texto, color = Navy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 2.dp))
    }
}

data class Recoleccion(val fecha: String, val material: String, val bolsas: String, val estado: String, val icono: ImageVector)

@Composable
fun HistorialScreen(onBack: () -> Unit, onNav: (String) -> Unit) {
    var filtro by remember { mutableStateOf("Todas") }
    var historialRoom by remember { mutableStateOf<List<HistorialEntity>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorCarga by remember { mutableStateOf(false) }
    LaunchedEffect(AppState.usuarioId) {
        cargando = true
        errorCarga = false
        historialRoom = emptyList()
        try {
            historialRoom = LocalStorage.cargarHistorialUsuario()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            errorCarga = true
        }
        cargando = false
    }
    val items = historialRoom.map {
        Recoleccion(it.fecha, it.material, it.bolsas, it.estado, Icons.Outlined.LocalDrink)
    }
    val visibles = when (filtro) {
        "Completadas" -> items.filter { it.estado == "Completado" }
        "Pendientes" -> items.filter { it.estado != "Completado" }
        else -> items
    }
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            BarraTitulo("Historial")
            Spacer(Modifier.height(6.dp))
            Text("Tus recolecciones", color = Navy, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("Todas", "Completadas", "Pendientes").forEach { f ->
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .background(if (filtro == f) VerdeClaro else Superficie, RoundedCornerShape(20.dp))
                            .semantics {
                                role = Role.Button
                                selected = filtro == f
                            }
                            .clickable { filtro = f }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(f, color = TextoPrincipal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            if (cargando) {
                Text("Cargando historial...", color = GrisTexto, fontSize = 14.sp)
            } else if (errorCarga) {
                Text("No se pudo cargar el historial.", color = GrisTexto, fontSize = 14.sp)
            } else if (visibles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Superficie, RoundedCornerShape(22.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.Description, contentDescription = null, tint = VerdeBoton, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Aún no hay historial", color = TextoPrincipal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Cuando registres una solicitud o completes una recolección, aparecerá aquí.", color = GrisTexto, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
            if (!cargando && !errorCarga) visibles.forEach { r ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Superficie, RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(58.dp).background(if (r.estado == "En camino") VerdeClaro else GrisChip, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Icon(r.icono, contentDescription = null, tint = if (r.estado == "En camino") Color(0xFF1B7A2F) else TextoPrincipal) }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(r.fecha, color = GrisTexto, fontSize = 13.sp)
                        Text(r.material, color = TextoPrincipal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(r.bolsas, color = GrisTexto, fontSize = 13.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(if (r.estado == "En camino") VerdeClaro else Superficie, RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(r.estado, color = TextoPrincipal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = GrisTexto)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        BarraInferior(if (AppState.rol == "vecino") tabsVecino else tabsRecolector, "historial", onNav)
    }
}

@Composable
private fun FilaPreferencia(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Superficie, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(GrisChip, CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(icono, contentDescription = null, tint = Navy) }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, color = Navy, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(subtitulo, color = GrisTexto, fontSize = 13.sp)
        }
        trailing()
    }
}

/**
 * A diferencia de "Ahorro de batería" (que sí cambia el muestreo de sensores), esta app
 * no tiene backend ni canal propio de push: solo puede reflejar y abrir el permiso de
 * notificaciones real del sistema, no simular envíos que nunca ocurren.
 */
private fun abrirAjustesNotificaciones(contexto: android.content.Context) {
    if (Build.VERSION.SDK_INT >= 26) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, contexto.packageName)
        }
        runCatching { contexto.startActivity(intent) }
    }
}

@Composable
fun PerfilScreen(onNav: (String) -> Unit, onCerrarSesion: () -> Unit) {
    val tabs = if (AppState.rol == "vecino") tabsVecino else tabsRecolector
    val scope = rememberCoroutineScope()
    val contexto = LocalContext.current
    val notificacionesActivas = remember {
        (contexto.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as? NotificationManager)
            ?.areNotificationsEnabled() ?: true
    }
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))
            BarraTitulo("Perfil")
            Spacer(Modifier.height(30.dp))
            Box(
                modifier = Modifier.size(110.dp).background(Superficie, CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.Person, contentDescription = null, tint = Navy, modifier = Modifier.size(60.dp)) }
            Spacer(Modifier.height(16.dp))
            Text(AppState.usuario.ifBlank { "Usuario" }, color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(if (AppState.rol == "vecino") "Vecino" else "Recolector local", color = GrisTexto, fontSize = 15.sp)
            Spacer(Modifier.height(30.dp))
            Text("Preferencias", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            FilaPreferencia(Icons.Outlined.DarkMode, "Modo oscuro", "Usar colores oscuros en toda la app") {
                Switch(
                    modifier = Modifier.semantics { contentDescription = "Modo oscuro" },
                    checked = AppState.modoOscuro,
                    onCheckedChange = { activo -> scope.launch { LocalStorage.guardarModoOscuro(activo) } }
                )
            }
            Spacer(Modifier.height(14.dp))
            if (AppState.rol == "recolector") {
                FilaPreferencia(Icons.Outlined.BatterySaver, "Ahorro de batería", "Reduce la frecuencia de los sensores de la brújula") {
                    Switch(
                        modifier = Modifier.semantics { contentDescription = "Ahorro de batería" },
                        checked = AppState.ahorroBateria,
                        onCheckedChange = { activo -> scope.launch { LocalStorage.guardarAhorroBateria(activo) } }
                    )
                }
                Spacer(Modifier.height(14.dp))
            }
            FilaPreferencia(
                Icons.Outlined.NotificationsNone,
                "Notificaciones",
                if (notificacionesActivas) "Activadas en este dispositivo" else "Desactivadas: toca para habilitarlas",
                modifier = Modifier
                    .semantics { role = Role.Button }
                    .clickable { abrirAjustesNotificaciones(contexto) }
            ) {
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = GrisTexto)
            }
            Spacer(Modifier.height(14.dp))
            FilaPreferencia(Icons.Outlined.Storage, "Datos y almacenamiento", "Solo local: tus datos no salen de este dispositivo") {}
            Spacer(Modifier.height(24.dp))
            BotonVerde("Cerrar sesión") { onCerrarSesion() }
        }
        BarraInferior(tabs, "perfil", onNav)
    }
}
