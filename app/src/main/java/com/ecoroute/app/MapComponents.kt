package com.ecoroute.app

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.TilesOverlay

data class PuntoMapa(
    val titulo: String,
    val latitud: Double,
    val longitud: Double
)

private fun iconoMiUbicacion(contexto: Context): GradientDrawable =
    GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(AndroidColor.rgb(26, 115, 232))
        setStroke((3 * contexto.resources.displayMetrics.density).toInt(), AndroidColor.WHITE)
        setSize((18 * contexto.resources.displayMetrics.density).toInt(), (18 * contexto.resources.displayMetrics.density).toInt())
    }

@Composable
fun MapaReal(
    puntos: List<PuntoMapa>,
    modifier: Modifier = Modifier,
    miUbicacion: GeoPoint? = null,
    expandible: Boolean = false,
    onTocarMapa: ((Double, Double) -> Unit)? = null
) {
    val contexto = LocalContext.current
    val modoOscuro = AppState.modoOscuro
    var expandido by remember { mutableStateOf(false) }
    val mapa = remember(contexto) {
        Configuration.getInstance().apply {
            userAgentValue = contexto.packageName
            load(contexto, contexto.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        }
        MapView(contexto).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 4.0
            maxZoomLevel = 20.0
            controller.setZoom(15.0)
        }
    }

    DisposableEffect(mapa) {
        mapa.onResume()
        onDispose {
            mapa.onPause()
            mapa.onDetach()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapa },
            update = { vista ->
                vista.overlayManager.tilesOverlay.setColorFilter(
                    if (modoOscuro) TilesOverlay.INVERT_COLORS else null
                )
                vista.overlays.clear()

                val posiciones = puntos.map { GeoPoint(it.latitud, it.longitud) }

                puntos.forEach { punto ->
                    vista.overlays.add(
                        Marker(vista).apply {
                            position = GeoPoint(punto.latitud, punto.longitud)
                            title = punto.titulo
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                    )
                }

                miUbicacion?.let { posicion ->
                    vista.overlays.add(
                        Marker(vista).apply {
                            position = posicion
                            title = "Tu ubicación"
                            icon = iconoMiUbicacion(vista.context)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        }
                    )
                }

                if (onTocarMapa != null) {
                    vista.overlays.add(
                        MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                onTocarMapa(p.latitude, p.longitude)
                                return true
                            }
                            override fun longPressHelper(p: GeoPoint): Boolean = false
                        })
                    )
                }

                when (posiciones.size) {
                    0 -> Unit
                    1 -> {
                        vista.controller.setZoom(16.0)
                        vista.controller.setCenter(posiciones.first())
                    }
                    else -> vista.post {
                        vista.zoomToBoundingBox(BoundingBox.fromGeoPoints(posiciones), true, 72)
                    }
                }
                vista.invalidate()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(if (expandido) 420.dp else 170.dp)
                .clip(RoundedCornerShape(20.dp))
        )
        if (expandible) {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .size(36.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.White, CircleShape)
                    .semantics { role = Role.Button }
                    .clickable { expandido = !expandido },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (expandido) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                    contentDescription = if (expandido) "Contraer mapa" else "Expandir mapa",
                    tint = Color(0xFF14603A)
                )
            }
        }
    }
}
