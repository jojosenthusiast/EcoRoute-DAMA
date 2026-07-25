package com.ecoroute.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Integrante(val nombre: String, val rol: String)

private val equipoEcoRoute = listOf(
    Integrante("Milton Josue Ramirez Gongora", "Desarrollo — scaffold inicial"),
    Integrante("Leonel Alexander Munguia Noyola", "Desarrollo — persistencia local"),
    Integrante("Carlos Gabriel Murgas Juarez", "Desarrollo — sensores y verificación"),
    Integrante("Angel Eduardo Coto Beltran", "Diseño — Figma y evidencia visual"),
    Integrante("Walter Leonel Linares Martinez", "Video y documentación")
)

@Composable
fun CreditosScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            BarraTitulo("Créditos", onBack = onBack)
            Spacer(Modifier.height(8.dp))
            Text(
                "EcoRoute — Laboratorio 1 DAMA",
                color = TextoPrincipal,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(20.dp))
            equipoEcoRoute.forEach { integrante ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Superficie, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Text(integrante.nombre, color = TextoPrincipal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(integrante.rol, color = TextoSecundario, fontSize = 14.sp)
                }
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
