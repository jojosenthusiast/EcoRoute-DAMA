package com.ecoroute.app

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay

data class PuntoMapa(
    val titulo: String,
    val latitud: Double,
    val longitud: Double
)

@Composable
fun MapaReal(
    puntos: List<PuntoMapa>,
    modifier: Modifier = Modifier,
    mostrarRuta: Boolean = false
) {
    val contexto = LocalContext.current
    val modoOscuro = AppState.modoOscuro
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

    AndroidView(
        factory = { mapa },
        update = { vista ->
            vista.overlayManager.tilesOverlay.setColorFilter(
                if (modoOscuro) TilesOverlay.INVERT_COLORS else null
            )
            vista.overlays.clear()

            val posiciones = puntos.map { GeoPoint(it.latitud, it.longitud) }
            if (mostrarRuta && posiciones.size > 1) {
                vista.overlays.add(
                    Polyline().apply {
                        setPoints(posiciones)
                        outlinePaint.color = AndroidColor.rgb(20, 96, 58)
                        outlinePaint.strokeWidth = 8f
                    }
                )
            }

            puntos.forEach { punto ->
                vista.overlays.add(
                    Marker(vista).apply {
                        position = GeoPoint(punto.latitud, punto.longitud)
                        title = punto.titulo
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
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
        modifier = modifier
            .height(170.dp)
            .clip(RoundedCornerShape(20.dp))
    )
}
