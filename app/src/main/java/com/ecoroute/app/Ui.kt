package com.ecoroute.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Navy: Color get() = TextoPrincipal
val VerdeBoton: Color get() = if (AppState.modoOscuro) Color(0xFF6ED8A3) else Color(0xFF197A55)
val SobreVerdeBoton: Color get() = if (AppState.modoOscuro) Color(0xFF003824) else Color(0xFFFFFFFF)
val VerdeCheck = Color(0xFF34B44A)
val VerdeClaro: Color get() = if (AppState.modoOscuro) Color(0xFF20382B) else Color(0xFFD9EFDE)
val VerdeSuave: Color get() = if (AppState.modoOscuro) Color(0xFF1B3024) else Color(0xFFE8F4EA)
val Fondo: Color get() = if (AppState.modoOscuro) Color(0xFF0D1511) else Color(0xFFF7FAF7)
val Amarillo: Color get() = if (AppState.modoOscuro) Color(0xFF332F1D) else Color(0xFFF6F1DA)
val GrisTexto: Color get() = if (AppState.modoOscuro) Color(0xFFAAB7AE) else Color(0xFF647168)
val GrisCampo: Color get() = if (AppState.modoOscuro) Color(0xFF222C27) else Color(0xFFE4E6E9)
val AmarilloChip: Color get() = if (AppState.modoOscuro) Color(0xFF3C351E) else Color(0xFFFCEBB6)
val GrisChip: Color get() = if (AppState.modoOscuro) Color(0xFF2A332F) else Color(0xFFE7E8EA)
val Superficie: Color get() = if (AppState.modoOscuro) Color(0xFF151F19) else Color(0xFFFFFFFF)
val TextoPrincipal: Color get() = if (AppState.modoOscuro) Color(0xFFE8F0EA) else Color(0xFF17211B)
val TextoSecundario: Color get() = GrisTexto
val Borde: Color get() = if (AppState.modoOscuro) Color(0xFF3B4D42) else Color(0xFFC9D4CC)

private val ColoresClaros = lightColorScheme(
    primary = Color(0xFF197A55),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFF7FAF7),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF17211B),
    onSurface = Color(0xFF17211B),
    outline = Color(0xFFC9D4CC)
)

private val ColoresOscuros = darkColorScheme(
    primary = Color(0xFF6ED8A3),
    onPrimary = Color(0xFF003824),
    background = Color(0xFF0D1511),
    surface = Color(0xFF151F19),
    onBackground = Color(0xFFE8F0EA),
    onSurface = Color(0xFFE8F0EA),
    outline = Color(0xFF3B4D42)
)

@Composable
fun EcoRouteTheme(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (AppState.modoOscuro) ColoresOscuros else ColoresClaros,
        content = contenido
    )
}

@Composable
fun BarraTitulo(titulo: String, onBack: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(GrisChip, CircleShape)
                    .semantics { role = Role.Button }
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Navy)
            }
            Spacer(Modifier.width(14.dp))
        }
        Text(titulo, color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}

@Composable
fun BotonVerde(texto: String, modifier: Modifier = Modifier, icono: ImageVector? = null, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(VerdeBoton, RoundedCornerShape(28.dp))
            .semantics { role = Role.Button }
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, tint = SobreVerdeBoton)
            Spacer(Modifier.width(10.dp))
        }
        Text(texto, color = SobreVerdeBoton, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BotonBlanco(texto: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Superficie, RoundedCornerShape(14.dp))
            .semantics { role = Role.Button }
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
                modifier = Modifier
                    .semantics {
                        role = Role.Button
                        selected = activo
                    }
                    .clickable { onNav(item.ruta) }
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 52.dp, height = 36.dp)
                        .background(if (activo) VerdeClaro else Color.Transparent, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icono, contentDescription = null, tint = if (activo) VerdeBoton else TextoSecundario, modifier = Modifier.size(24.dp))
                }
                Text(item.nombre, fontSize = 12.sp, color = if (activo) VerdeBoton else TextoSecundario, fontWeight = FontWeight.Medium)
            }
        }
    }
}
