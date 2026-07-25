package com.ecoroute.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Navy: Color get() = if (AppState.modoOscuro) Color(0xFFF2F5F3) else Color(0xFF1E2A56)
val VerdeBoton: Color get() = if (AppState.modoOscuro) Color(0xFF198754) else Color(0xFF14603A)
val VerdeCheck = Color(0xFF34B44A)
val VerdeClaro: Color get() = if (AppState.modoOscuro) Color(0xFF20382B) else Color(0xFFD9EFDE)
val VerdeSuave: Color get() = if (AppState.modoOscuro) Color(0xFF1B3024) else Color(0xFFE8F4EA)
val Fondo: Color get() = if (AppState.modoOscuro) Color(0xFF101713) else Color(0xFFF4F5F3)
val Amarillo: Color get() = if (AppState.modoOscuro) Color(0xFF332F1D) else Color(0xFFF6F1DA)
val GrisTexto: Color get() = if (AppState.modoOscuro) Color(0xFFAAB4AE) else Color(0xFF8A93A6)
val GrisCampo: Color get() = if (AppState.modoOscuro) Color(0xFF222C27) else Color(0xFFE4E6E9)
val AmarilloChip: Color get() = if (AppState.modoOscuro) Color(0xFF3C351E) else Color(0xFFFCEBB6)
val GrisChip: Color get() = if (AppState.modoOscuro) Color(0xFF2A332F) else Color(0xFFE7E8EA)
val Superficie: Color get() = if (AppState.modoOscuro) Color(0xFF18211D) else Color.White
val TextoPrincipal: Color get() = if (AppState.modoOscuro) Color(0xFFF2F5F3) else Color(0xFF111827)
val TextoSecundario: Color get() = if (AppState.modoOscuro) Color(0xFFCBD5CF) else Color(0xFF374151)
val Borde: Color get() = if (AppState.modoOscuro) Color(0xFF3A4741) else Color(0xFFD5D8DD)

private val ColoresClaros = lightColorScheme(
    primary = Color(0xFF14603A),
    background = Color(0xFFF4F5F3),
    surface = Color.White,
    onBackground = Color(0xFF1E2A56),
    onSurface = Color(0xFF111827)
)

private val ColoresOscuros = darkColorScheme(
    primary = Color(0xFF34B879),
    background = Color(0xFF101713),
    surface = Color(0xFF18211D),
    onBackground = Color(0xFFF2F5F3),
    onSurface = Color(0xFFF2F5F3)
)

@Composable
fun EcoRouteTheme(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (AppState.modoOscuro) ColoresOscuros else ColoresClaros,
        content = contenido
    )
}

@Composable
fun BarraTitulo(titulo: String, onBack: (() -> Unit)? = null, mostrarMenu: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(GrisChip, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Navy)
            }
            Spacer(Modifier.width(14.dp))
        }
        Text(titulo, color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (mostrarMenu) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Superficie, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MoreHoriz, contentDescription = null, tint = Navy)
            }
        }
    }
}

@Composable
fun BotonVerde(texto: String, modifier: Modifier = Modifier, icono: ImageVector? = null, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(VerdeBoton, RoundedCornerShape(28.dp))
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(10.dp))
        }
        Text(texto, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BotonBlanco(texto: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Superficie, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(texto, color = VerdeBoton, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

data class TabItem(val nombre: String, val icono: ImageVector, val ruta: String)

@Composable
fun BarraInferior(items: List<TabItem>, rutaActual: String, onNav: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Superficie)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            val activo = item.ruta == rutaActual
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNav(item.ruta) }
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 52.dp, height = 36.dp)
                        .background(if (activo) VerdeClaro else Color.Transparent, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icono, contentDescription = item.nombre, tint = if (activo) VerdeBoton else TextoSecundario, modifier = Modifier.size(24.dp))
                }
                Text(item.nombre, fontSize = 12.sp, color = if (activo) VerdeBoton else TextoSecundario, fontWeight = FontWeight.Medium)
            }
        }
    }
}
