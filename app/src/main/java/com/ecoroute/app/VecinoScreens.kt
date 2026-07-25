package com.ecoroute.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val tabsVecino = listOf(
    TabItem("Inicio", Icons.Outlined.Home, "vecinoHome"),
    TabItem("Solicitudes", Icons.Outlined.Description, "estado"),
    TabItem("Historial", Icons.Outlined.Schedule, "historial"),
    TabItem("Perfil", Icons.Outlined.Person, "perfil")
)

@Composable
fun MapaCasa(modifier: Modifier = Modifier) {
    MapaReal(
        puntos = listOf(
            PuntoMapa(
                "Punto de recolección",
                AppState.LATITUD_CASA,
                AppState.LONGITUD_CASA
            )
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun HomeVecinoScreen(onNav: (String) -> Unit) {
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
                Box(
                    modifier = Modifier.size(42.dp).background(GrisChip, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = Navy)
                }
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
                            .clickable { onNav("registrar") },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(12.dp))
                        Box(Modifier.size(24.dp).background(Superficie, CircleShape))
                        Spacer(Modifier.width(14.dp))
                        Text("Registrar reciclaje", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Solicitud activa", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Superficie, RoundedCornerShape(24.dp))
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
                    Text("Plástico y papel", color = Navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("2 bolsas - Hoy 3: 00 - 5: 00 p.m", color = GrisTexto, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.background(VerdeClaro, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                        Text("En camino", color = VerdeBoton, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Navy)
            }
            Spacer(Modifier.height(24.dp))
            Text("Tu impacto", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaImpacto("8", "bolsas", Modifier.weight(1f))
                TarjetaImpacto("14 kg", "recuperados", Modifier.weight(1f))
                TarjetaImpacto("3", "recolecciones", Modifier.weight(1f))
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

@Composable
fun RegistrarReciclajeScreen(onBack: () -> Unit, onContinuar: () -> Unit) {
    val seleccion = remember { mutableStateListOf(*AppState.materiales.toTypedArray()) }
    var bolsas by remember { mutableIntStateOf(AppState.bolsas) }
    fun alternar(m: String) { if (seleccion.contains(m)) seleccion.remove(m) else seleccion.add(m) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        BarraTitulo("Registrar reciclaje", onBack = onBack, mostrarMenu = false)
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
        Text("Volumen aproximado", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(20.dp)).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(38.dp).background(GrisChip, CircleShape).clickable { if (bolsas > 1) bolsas-- },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Remove, contentDescription = null, tint = Navy) }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$bolsas bolsas medianas", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("arox ${bolsas * 4} - ${bolsas * 6} kg", color = GrisTexto, fontSize = 13.sp)
            }
            Box(
                modifier = Modifier.size(38.dp).background(VerdeClaro, CircleShape).clickable { bolsas++ },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Add, contentDescription = null, tint = VerdeBoton) }
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
        Spacer(Modifier.height(30.dp))
        BotonVerde("Continuar") {
            AppState.materiales.clear()
            AppState.materiales.addAll(seleccion)
            AppState.bolsas = bolsas
            onContinuar()
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun ChipHorario(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (seleccionado) VerdeClaro else Superficie, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(texto, color = if (seleccionado) VerdeBoton else Navy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DetallesRecoleccionScreen(onBack: () -> Unit, onRevisar: () -> Unit) {
    var horario by remember { mutableStateOf(AppState.horario) }
    var referencia by remember { mutableStateOf(AppState.referencia) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        BarraTitulo("Detalles de recolección", onBack = onBack, mostrarMenu = false)
        Spacer(Modifier.height(6.dp))
        Text("¿Dónde y cuándo?", color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Paso 2 de 3 . Confirma los datos de la parada.", color = GrisTexto, fontSize = 15.sp)
        Spacer(Modifier.height(14.dp))
        BarraProgreso(0.66f)
        Spacer(Modifier.height(20.dp))
        MapaCasa()
        Spacer(Modifier.height(20.dp))
        Text("Dirección", color = Navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(16.dp)).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Residencial Las Flores, pasaje 4", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Navy)
        }
        Spacer(Modifier.height(20.dp))
        Text("Horario preferido", color = Navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChipHorario("Hoy 3-5 p.m.", horario == "Hoy 3-5 p.m.") { horario = "Hoy 3-5 p.m." }
            ChipHorario("Mañana", horario == "Mañana") { horario = "Mañana" }
            ChipHorario("Otro", horario == "Otro") { horario = "Otro" }
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
            AppState.horario = horario
            AppState.referencia = referencia
            onRevisar()
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun FilaResumen(clave: String, valor: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
            Text(clave, color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(valor, color = Navy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(color = Borde)
    }
}

@Composable
fun ConfirmarSolicitudScreen(onBack: () -> Unit, onEnviar: () -> Unit) {
    var autorizo by remember { mutableStateOf(AppState.autorizoUbicacion) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        BarraTitulo("Confirmar solicitud", onBack = onBack, mostrarMenu = false)
        Spacer(Modifier.height(6.dp))
        Text("Todo listo", color = Navy, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Paso 3 de 3 - Revisa antes de enviar", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        BarraProgreso(1f)
        Spacer(Modifier.height(20.dp))
        Column(modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(20.dp)).padding(18.dp)) {
            Text("Resumen de recolecion", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            FilaResumen("Materiales", AppState.materiales.joinToString(", "))
            FilaResumen("Volumen", "${AppState.bolsas} bolsas medianas")
            FilaResumen("Horario", if (AppState.horario == "Hoy 3-5 p.m.") "Hoy,2:00-5:00.p.m" else AppState.horario)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                Text("Ubicacion", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("Residensial Las Flores", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                Text("Tu reporte se agregara a la bitacora del recolector mas cercano", color = TextoSecundario, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Superficie, RoundedCornerShape(26.dp))
                .clickable { autorizo = !autorizo }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(if (autorizo) VerdeCheck else GrisChip, CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White) }
            Spacer(Modifier.width(14.dp))
            Text("Autorizo conpartir esta ubicacion para la parada.", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(28.dp))
        BotonVerde("Enviar solicitud") {
            AppState.autorizoUbicacion = autorizo
            AppState.solicitudEnviada = true
            onEnviar()
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun PasoSeguimiento(titulo: String, hora: String, completado: Boolean, ultimo: Boolean = false) {
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(38.dp).background(if (completado) VerdeCheck else Color(0xFFD4D6DA), CircleShape),
                contentAlignment = Alignment.Center
            ) { if (completado) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            if (!ultimo) Box(Modifier.width(5.dp).height(58.dp).background(if (completado) VerdeBoton else Color(0xFFD4D6DA)))
        }
        Spacer(Modifier.width(18.dp))
        Column {
            Text(titulo, color = TextoPrincipal, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text(hora, color = GrisTexto, fontSize = 14.sp)
        }
    }
}

@Composable
fun EstadoSolicitudScreen(onBack: () -> Unit, onNav: (String) -> Unit) {
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
            Column(modifier = Modifier.fillMaxWidth().background(VerdeClaro, RoundedCornerShape(20.dp)).padding(20.dp)) {
                Text("EN CAMINO", color = VerdeBoton, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Llegada estimada: 18min", color = TextoPrincipal, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier.size(56.dp).background(Superficie, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.Route, contentDescription = null, tint = Navy, modifier = Modifier.size(26.dp)) }
                }
            }
            Spacer(Modifier.height(26.dp))
            Text("Seguimiento", color = TextoPrincipal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            PasoSeguimiento("Reportado", "2:18.p.m", true)
            PasoSeguimiento("Recolector asignado", "2:18.p.m", true)
            PasoSeguimiento("En camino", "2:18.p.m", true)
            PasoSeguimiento("Recolectado", "2:18.p.m", false, ultimo = true)
            Spacer(Modifier.height(30.dp))
            Column(modifier = Modifier.fillMaxWidth().background(Superficie, RoundedCornerShape(20.dp)).padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(64.dp).background(Color(0xFF7C9C4A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("MR", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Marcos R.", color = TextoPrincipal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Recolector local -4.9", color = GrisTexto, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(Superficie, RoundedCornerShape(22.dp))
                        .clickable { }
                        .padding(horizontal = 34.dp, vertical = 12.dp)
                ) {
                    Text("Enviar mensaje", color = VerdeBoton, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        BarraInferior(tabsVecino, "estado", onNav)
    }
}

data class Recoleccion(val fecha: String, val material: String, val bolsas: String, val estado: String, val icono: ImageVector)

@Composable
fun HistorialScreen(onBack: () -> Unit, onNav: (String) -> Unit) {
    var filtro by remember { mutableStateOf("Todos") }
    val items = listOf(
        Recoleccion("15 julio", "Plastico y papel", "2 bolsas", "En camino", Icons.Outlined.LocalDrink),
        Recoleccion("10 julio", "Vidrio", "1 bolsa", "Completado", Icons.Outlined.LocalDrink),
        Recoleccion("2 julio", "Papel y metal", "3 bolsas", "Completado", Icons.Outlined.Description),
        Recoleccion("24 julio", "Plastico", "2 bolsas", "Completado", Icons.Outlined.LocalDrink)
    )
    val visibles = when (filtro) {
        "Completados" -> items.filter { it.estado == "Completado" }
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
            BarraTitulo("Historial", onBack = onBack)
            Spacer(Modifier.height(6.dp))
            Text("Tus recolecciones", color = Navy, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Todos", "Completados", "Pendientes").forEach { f ->
                    Box(
                        modifier = Modifier
                            .background(if (filtro == f) VerdeClaro else Superficie, RoundedCornerShape(20.dp))
                            .clickable { filtro = f }
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(f, color = if (filtro == f) Color(0xFF7BA07F) else TextoPrincipal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            visibles.forEach { r ->
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
                        Text(r.estado, color = if (r.estado == "En camino") Color(0xFF7BA07F) else TextoPrincipal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = GrisTexto)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        BarraInferior(tabsVecino, "historial", onNav)
    }
}

@Composable
fun PerfilScreen(onNav: (String) -> Unit, onCerrarSesion: () -> Unit) {
    val tabs = if (AppState.rol == "vecino") tabsVecino else tabsRecolector
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
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
            Text(if (AppState.rol == "vecino") "Vecino" else "Recolector local -4.9", color = GrisTexto, fontSize = 15.sp)
            Spacer(Modifier.height(30.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Superficie, RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(GrisChip, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = Navy)
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Modo oscuro", color = Navy, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("Usar colores oscuros en toda la app", color = GrisTexto, fontSize = 13.sp)
                }
                Switch(
                    checked = AppState.modoOscuro,
                    onCheckedChange = { LocalStorage.guardarModoOscuro(it) }
                )
            }
            Spacer(Modifier.height(24.dp))
            BotonVerde("Cerrar sesión") { onCerrarSesion() }
        }
        BarraInferior(tabs, "perfil", onNav)
    }
}
