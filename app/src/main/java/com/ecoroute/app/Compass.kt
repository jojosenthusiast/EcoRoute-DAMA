package com.ecoroute.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPoint(val latitude: Double, val longitude: Double)

data class CompassGuidance(val rotation: Float, val angleText: String)

sealed interface LocationResult {
    data object PermissionRequired : LocationResult
    data object Unavailable : LocationResult
    data class Available(
        val point: GeoPoint,
        val accuracyMeters: Float? = null,
        val timestampMillis: Long = System.currentTimeMillis()
    ) : LocationResult
}

data class CompassUiState(val message: String, val guidanceReady: Boolean)

fun normalizeDegrees(angle: Float): Float = ((angle % 360f) + 360f) % 360f

fun bearingDegrees(origin: GeoPoint, destination: GeoPoint): Float {
    val originLatitude = Math.toRadians(origin.latitude)
    val destinationLatitude = Math.toRadians(destination.latitude)
    val longitudeDelta = Math.toRadians(destination.longitude - origin.longitude)
    val y = sin(longitudeDelta) * cos(destinationLatitude)
    val x = cos(originLatitude) * sin(destinationLatitude) -
        sin(originLatitude) * cos(destinationLatitude) * cos(longitudeDelta)

    return normalizeDegrees(Math.toDegrees(atan2(y, x)).toFloat())
}

private const val EARTH_RADIUS_KM = 6371.0

fun distanceKm(origin: GeoPoint, destination: GeoPoint): Double {
    val originLatitude = Math.toRadians(origin.latitude)
    val destinationLatitude = Math.toRadians(destination.latitude)
    val latitudeDelta = Math.toRadians(destination.latitude - origin.latitude)
    val longitudeDelta = Math.toRadians(destination.longitude - origin.longitude)
    val a = sin(latitudeDelta / 2).let { it * it } +
        cos(originLatitude) * cos(destinationLatitude) * sin(longitudeDelta / 2).let { it * it }
    return 2 * EARTH_RADIUS_KM * atan2(sqrt(a), sqrt(1 - a))
}

fun formatDistanceKm(km: Double): String =
    if (km < 1.0) "${(km * 1000).roundToInt()} m" else String.format(Locale.US, "%.1f km", km)

fun needleRotationDegrees(bearing: Float, heading: Float): Float =
    normalizeDegrees(bearing - heading)

fun displayAngle(angle: Float): String =
    "${normalizeDegrees(angle).roundToInt() % 360}°"

fun destinationGuidance(origin: GeoPoint, destination: GeoPoint, heading: Float): CompassGuidance {
    val rotation = needleRotationDegrees(bearingDegrees(origin, destination), heading)
    return CompassGuidance(rotation, displayAngle(rotation))
}

fun compassUiState(
    location: LocationResult,
    sensorsAvailable: Boolean,
    sensorRegistered: Boolean?,
    heading: Float?,
    destinationName: String,
    necesitaCalibracion: Boolean = false
): CompassUiState = when {
    !sensorsAvailable -> CompassUiState("Este dispositivo no tiene los sensores requeridos.", false)
    location == LocationResult.PermissionRequired -> CompassUiState(
        "Activa el permiso de ubicación para orientar la brújula hacia esta parada.",
        false
    )
    location == LocationResult.Unavailable -> CompassUiState(
        "No hay una ubicación disponible. Activa la ubicación y vuelve a intentarlo.",
        false
    )
    sensorRegistered == false -> CompassUiState("No se pudo iniciar la brújula.", false)
    sensorRegistered != true || heading == null -> CompassUiState("Esperando orientación de los sensores.", false)
    // El magnetómetro reporta su propia confianza (SensorEventListener.onAccuracyChanged); una
    // lectura con precisión baja/no confiable es ruido de verdad (interferencia magnética), no
    // algo que un filtro pueda arreglar. Mostrar la aguja igual sería mostrar un dato falso.
    necesitaCalibracion -> CompassUiState(
        "Brújula descalibrada: mové el celular en forma de 8, lejos de imanes o metal.",
        false
    )
    else -> CompassUiState("La flecha apunta hacia $destinationName.", true)
}

private const val LOCATION_TIMEOUT_MS = 10_000L
private const val SIGNIFICANT_LOCATION_AGE_MS = 120_000L
private const val ACCEPTABLE_ACCURACY_LOSS_METERS = 50f

fun shouldAcceptLocation(
    previous: LocationResult.Available?,
    candidate: LocationResult.Available
): Boolean {
    if (previous == null) return true

    val timeDelta = candidate.timestampMillis - previous.timestampMillis
    if (timeDelta > SIGNIFICANT_LOCATION_AGE_MS) return true
    if (timeDelta < -SIGNIFICANT_LOCATION_AGE_MS) return false

    val previousAccuracy = previous.accuracyMeters ?: Float.MAX_VALUE
    val candidateAccuracy = candidate.accuracyMeters ?: Float.MAX_VALUE
    val accuracyDelta = candidateAccuracy - previousAccuracy
    return accuracyDelta <= 0f || (timeDelta >= 0L && accuracyDelta <= ACCEPTABLE_ACCURACY_LOSS_METERS)
}

private fun Context.hasLocationPermission(): Boolean =
    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun Location.toLocationResult(): LocationResult.Available =
    LocationResult.Available(
        point = GeoPoint(latitude, longitude),
        accuracyMeters = accuracy.takeIf { hasAccuracy() },
        timestampMillis = time.takeIf { it > 0L } ?: System.currentTimeMillis()
    )

private fun LocationManager.bestLastKnownLocation(): Location? =
    getProviders(true)
        .mapNotNull { provider -> runCatching { getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull { it.time }

fun lastKnownLocationResult(context: Context): LocationResult {
    if (!context.hasLocationPermission()) return LocationResult.PermissionRequired

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        ?: return LocationResult.Unavailable
    return try {
        locationManager.bestLastKnownLocation()
            ?.toLocationResult()
            ?: LocationResult.Unavailable
    } catch (_: SecurityException) {
        LocationResult.PermissionRequired
    }
}

/**
 * The passive cache read by [lastKnownLocationResult] is frequently empty on a real device
 * unless some other app recently requested a fix. This requests one active fix from the best
 * enabled provider and reports it once via [onLocation]. Returns a cancel callback that must be
 * invoked when the caller stops observing (e.g. onPause) to release the platform callback.
 */
fun requestFreshLocation(
    context: Context,
    timeoutMillis: Long = LOCATION_TIMEOUT_MS,
    onLocation: (LocationResult) -> Unit
): () -> Unit {
    if (!context.hasLocationPermission()) {
        onLocation(LocationResult.PermissionRequired)
        return {}
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (locationManager == null) {
        onLocation(LocationResult.Unavailable)
        return {}
    }

    // La red suele responder antes dentro de edificios; el GPS queda como respaldo.
    val provider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        .firstOrNull { runCatching { locationManager.isProviderEnabled(it) }.getOrDefault(false) }
    if (provider == null) {
        onLocation(LocationResult.Unavailable)
        return {}
    }

    val handler = Handler(Looper.getMainLooper())
    var finished = false
    var listener: LocationListener? = null
    var timeout: Runnable? = null

    fun removeCallbacks() {
        listener?.let { activeListener ->
            runCatching { locationManager.removeUpdates(activeListener) }
        }
        timeout?.let(handler::removeCallbacks)
    }

    fun finish(result: LocationResult) {
        if (finished) return
        finished = true
        removeCallbacks()
        onLocation(result)
    }

    listener = LocationListener { location: Location ->
        finish(location.toLocationResult())
    }
    timeout = Runnable {
        val cached = runCatching { locationManager.bestLastKnownLocation() }.getOrNull()
        finish(cached?.toLocationResult() ?: LocationResult.Unavailable)
    }

    return try {
        val activeListener = requireNotNull(listener)
        val activeTimeout = requireNotNull(timeout)
        locationManager.requestSingleUpdate(provider, activeListener, Looper.getMainLooper())
        if (!finished) {
            handler.postDelayed(activeTimeout, timeoutMillis)
        }
        val cancel: () -> Unit = {
            if (!finished) {
                finished = true
                removeCallbacks()
            }
        }
        cancel
    } catch (_: SecurityException) {
        finish(LocationResult.PermissionRequired)
        val noop: () -> Unit = {}
        noop
    } catch (_: Exception) {
        finish(LocationResult.Unavailable)
        val noop: () -> Unit = {}
        noop
    }
}

/**
 * Observa la ubicación solo mientras la pantalla que devuelve el callback permanezca activa.
 * Emite el último valor conocido de inmediato cuando existe y continúa solicitando posiciones
 * nuevas. Si no llega ninguna posición en el tiempo indicado comunica [LocationResult.Unavailable],
 * pero mantiene los listeners vivos para recuperarse si el GPS responde después.
 */
fun observeLocationUpdates(
    context: Context,
    minTimeMillis: Long = 1_000L,
    minDistanceMeters: Float = 1f,
    timeoutMillis: Long = LOCATION_TIMEOUT_MS,
    onLocation: (LocationResult) -> Unit
): () -> Unit {
    if (!context.hasLocationPermission()) {
        onLocation(LocationResult.PermissionRequired)
        return {}
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (locationManager == null) {
        onLocation(LocationResult.Unavailable)
        return {}
    }

    val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        .filter { runCatching { locationManager.isProviderEnabled(it) }.getOrDefault(false) }
    if (providers.isEmpty()) {
        onLocation(LocationResult.Unavailable)
        return {}
    }

    val handler = Handler(Looper.getMainLooper())
    var active = true
    var deliveredLocation = false
    var lastAcceptedLocation: LocationResult.Available? = null
    val timeout = Runnable {
        if (active && !deliveredLocation) onLocation(LocationResult.Unavailable)
    }
    val listener = LocationListener { location ->
        if (active) {
            val candidate = location.toLocationResult()
            if (shouldAcceptLocation(lastAcceptedLocation, candidate)) {
                lastAcceptedLocation = candidate
                deliveredLocation = true
                handler.removeCallbacks(timeout)
                onLocation(candidate)
            }
        }
    }

    return try {
        runCatching { locationManager.bestLastKnownLocation() }.getOrNull()?.let {
            val cachedLocation = it.toLocationResult()
            lastAcceptedLocation = cachedLocation
            deliveredLocation = true
            onLocation(cachedLocation)
        }

        var registered = false
        var permissionFailure = false
        providers.forEach { provider ->
            try {
                locationManager.requestLocationUpdates(
                    provider,
                    minTimeMillis,
                    minDistanceMeters,
                    listener,
                    Looper.getMainLooper()
                )
                registered = true
            } catch (_: SecurityException) {
                permissionFailure = true
            } catch (_: Exception) {
                // Si un proveedor falla, el otro todavía puede mantener el seguimiento.
            }
        }

        if (!registered) {
            active = false
            onLocation(if (permissionFailure) LocationResult.PermissionRequired else LocationResult.Unavailable)
            val noop: () -> Unit = {}
            noop
        } else {
            if (!deliveredLocation) {
                handler.postDelayed(timeout, timeoutMillis)
            }
            val cancel: () -> Unit = {
                if (active) {
                    active = false
                    handler.removeCallbacks(timeout)
                    runCatching { locationManager.removeUpdates(listener) }
                }
            }
            cancel
        }
    } catch (_: SecurityException) {
        active = false
        handler.removeCallbacks(timeout)
        onLocation(LocationResult.PermissionRequired)
        val noop: () -> Unit = {}
        noop
    } catch (_: Exception) {
        active = false
        handler.removeCallbacks(timeout)
        onLocation(LocationResult.Unavailable)
        val noop: () -> Unit = {}
        noop
    }
}

class SensorLifecycle(
    private val register: () -> Boolean,
    private val unregister: () -> Unit
) {
    private var active = false
    internal val isActive: Boolean get() = active

    fun onResume(): Boolean {
        if (!active) active = register()
        return active
    }

    fun onPause() {
        if (!active) return
        active = false
        unregister()
    }
}

internal class SensorFusionState {
    val gravity = FloatArray(3)
    val magnetic = FloatArray(3)
    private var hasGravity = false
    private var hasMagnetic = false
    val ready: Boolean get() = hasGravity && hasMagnetic

    /**
     * Al caminar o sacudir el celular, la aceleración lineal del movimiento se suma a la
     * gravedad en la lectura cruda: su magnitud deja de rondar los ~9.8 m/s². getRotationMatrix()
     * asume que recibe gravedad pura, así que alimentarlo con una lectura contaminada por
     * movimiento hace que la inclinación calculada (y por lo tanto el rumbo) salte de forma
     * errática — esto es un problema distinto de la interferencia magnética. Sin giroscopio
     * (el laboratorio solo pide acelerómetro + magnetómetro) no hay forma de separar gravedad
     * de aceleración lineal en una sola lectura, así que la mitigación es descartar las muestras
     * que se alejan demasiado de la gravedad esperada y quedarse con el último vector confiable
     * hasta que el aparato vuelva a estar razonablemente quieto.
     */
    fun updateGravity(values: FloatArray) {
        val magnitude = sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
        if (abs(magnitude - SensorManager.STANDARD_GRAVITY) > MOVEMENT_TOLERANCE) return
        lowPass(values, gravity, hasGravity)
        hasGravity = true
    }

    fun updateMagnetic(values: FloatArray) {
        lowPass(values, magnetic, hasMagnetic)
        hasMagnetic = true
    }

    fun reset() {
        gravity.fill(0f)
        magnetic.fill(0f)
        hasGravity = false
        hasMagnetic = false
    }

    /**
     * Un camión de basura vibra todo el tiempo; sin este filtro paso-bajo cada
     * microvibración se traduce en un salto visible de la aguja. ALPHA bajo pesa
     * más el historial que la lectura nueva, suavizando el movimiento a costa de
     * un pequeño retraso. El primer valor se toma tal cual porque no hay historial
     * previo con el que promediar.
     */
    private fun lowPass(input: FloatArray, output: FloatArray, hasPrevious: Boolean) {
        if (!hasPrevious) {
            input.copyInto(output, endIndex = output.size)
            return
        }
        for (i in output.indices) {
            output[i] += ALPHA * (input[i] - output[i])
        }
    }

    private companion object {
        const val ALPHA = 0.15f
        const val MOVEMENT_TOLERANCE = 2.5f
    }
}

internal enum class SensorKind {
    ACCELEROMETER,
    MAGNETIC_FIELD
}

private class AndroidCompassSensors(val manager: SensorManager) {
    val accelerometer: Sensor? = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magnetometer: Sensor? = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
}

class CompassSensorController internal constructor(
    val sensorsAvailable: Boolean,
    private val registerAccelerometer: (SensorEventListener) -> Boolean,
    private val registerMagnetometer: (SensorEventListener) -> Boolean,
    private val unregister: (SensorEventListener) -> Unit,
    private val calculateHeading: (FloatArray, FloatArray) -> Float?,
    private val onHeading: (Float) -> Unit,
    private val onCalibrationNeeded: (Boolean) -> Unit = {}
) : SensorEventListener {
    private constructor(
        sensors: AndroidCompassSensors,
        delay: Int,
        onCalibrationNeeded: (Boolean) -> Unit,
        onHeading: (Float) -> Unit
    ) : this(
        sensorsAvailable = sensors.accelerometer != null && sensors.magnetometer != null,
        registerAccelerometer = { listener ->
            sensors.accelerometer?.let {
                sensors.manager.registerListener(listener, it, delay)
            } ?: false
        },
        registerMagnetometer = { listener ->
            sensors.magnetometer?.let {
                sensors.manager.registerListener(listener, it, delay)
            } ?: false
        },
        unregister = sensors.manager::unregisterListener,
        calculateHeading = { gravity, magnetic ->
            val rotation = FloatArray(9)
            if (!SensorManager.getRotationMatrix(rotation, null, gravity, magnetic)) {
                null
            } else {
                // Un celular usado como brújula se sostiene vertical (parado en frente
                // del usuario), no acostado sobre una mesa. getOrientation() asume que
                // el eje Y del dispositivo es el "frente"; remapCoordinateSystem lo
                // remapea para que el eje Z (la parte trasera del teléfono) sea el
                // nuevo frente, que es lo que realmente apunta hacia la parada.
                val remapped = FloatArray(9)
                SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapped)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(remapped, orientation)
                Math.toDegrees(orientation[0].toDouble()).toFloat()
            }
        },
        onHeading = onHeading,
        onCalibrationNeeded = onCalibrationNeeded
    )

    constructor(
        sensorManager: SensorManager,
        delay: Int = SensorManager.SENSOR_DELAY_UI,
        onCalibrationNeeded: (Boolean) -> Unit = {},
        onHeading: (Float) -> Unit
    ) : this(AndroidCompassSensors(sensorManager), delay, onCalibrationNeeded, onHeading)

    private val fusion = SensorFusionState()
    internal var headingReady = false
        private set
    private val lifecycle = SensorLifecycle(
        register = {
            headingReady = false
            if (!sensorsAvailable) {
                false
            } else {
                fusion.reset()
                if (!registerAccelerometer(this)) {
                    unregister(this)
                    false
                } else if (!registerMagnetometer(this)) {
                    unregister(this)
                    false
                } else {
                    true
                }
            }
        },
        unregister = { unregister(this) }
    )
    internal val isActive: Boolean get() = lifecycle.isActive

    fun onResume(): Boolean = lifecycle.onResume()

    fun onPause() {
        lifecycle.onPause()
        fusion.reset()
        headingReady = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        val kind = when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> SensorKind.ACCELEROMETER
            Sensor.TYPE_MAGNETIC_FIELD -> SensorKind.MAGNETIC_FIELD
            else -> return
        }
        onSample(kind, event.values)
    }

    internal fun onSample(kind: SensorKind, values: FloatArray) {
        if (!lifecycle.isActive) return
        when (kind) {
            SensorKind.ACCELEROMETER -> fusion.updateGravity(values)
            SensorKind.MAGNETIC_FIELD -> fusion.updateMagnetic(values)
        }
        if (!fusion.ready) return
        val heading = calculateHeading(fusion.gravity, fusion.magnetic) ?: return
        headingReady = true
        onHeading(normalizeDegrees(heading))
    }

    // Interferencia magnética (metal, imanes, electrónica cerca) hace que accel+mag crudo
    // "gire como una pelota": no es ruido que un filtro pueda promediar, es una lectura mala
    // de origen. El propio magnetómetro reporta cuándo esto pasa vía este callback; en vez de
    // dibujar una aguja poco confiable, se lo comunicamos a la UI para que avise en vez de mentir.
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            onCalibrationNeeded(accuracy in SensorManager.SENSOR_STATUS_UNRELIABLE..SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        }
    }
}
