package com.ecoroute.app

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

fun hasLocationPermission(fineGranted: Boolean, coarseGranted: Boolean) = fineGranted || coarseGranted

@Composable
fun LogoEcoRoute(tamano: Int = 44) {
    Box(
        modifier = Modifier
            .size(tamano.dp)
            .background(Color(0xFF1B7A2F), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((tamano * 0.4).dp)
                .background(Color(0xFFF4D525), RoundedCornerShape(topStart = 2.dp, topEnd = (tamano / 2).dp, bottomStart = (tamano / 2).dp, bottomEnd = 2.dp))
        )
    }
}

@Composable
fun BienvenidaScreen(onLogin: () -> Unit, onCrearCuenta: () -> Unit) {
    val scroll = rememberScrollState()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
                .heightIn(min = maxHeight)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(70.dp))
            LogoEcoRoute(86)
            Spacer(Modifier.height(18.dp))
            Text("EcoRoute", color = Navy, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(10.dp))
            Text(
                "Conecta vecinos y recolectores para recuperar materiales de forma ordenada.",
                color = GrisTexto,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(56.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(VerdeClaro, RoundedCornerShape(34.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Recycling, contentDescription = null, tint = VerdeBoton, modifier = Modifier.size(150.dp))
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))
            BotonVerde("Iniciar sesión") { onLogin() }
            Spacer(Modifier.height(14.dp))
            BotonBlanco("Crear cuenta") { onCrearCuenta() }
            Spacer(Modifier.height(34.dp))
        }
    }
}

@Composable
fun CampoTexto(
    valor: String,
    placeholder: String,
    esPassword: Boolean = false,
    icono: ImageVector = Icons.Filled.Person,
    onCambio: (String) -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = GrisTexto) },
        leadingIcon = { Icon(icono, contentDescription = null, tint = GrisTexto, modifier = Modifier.size(20.dp)) },
        trailingIcon = if (esPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = GrisTexto
                    )
                }
            }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (esPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (esPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextoPrincipal,
            unfocusedTextColor = TextoPrincipal,
            focusedContainerColor = Superficie,
            unfocusedContainerColor = Superficie,
            focusedBorderColor = VerdeBoton,
            unfocusedBorderColor = Borde
        )
    )
}

@Composable
fun LoginScreen(onEntrar: () -> Unit, onCrearCuenta: () -> Unit, onRecuperar: () -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogoEcoRoute(56)
            Spacer(Modifier.width(10.dp))
            Text("ECOROUTE", color = Navy, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(VerdeClaro, RoundedCornerShape(34.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Recycling, contentDescription = null, tint = VerdeBoton, modifier = Modifier.size(110.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Tu usuario", color = Navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        CampoTexto(usuario, "Ingrese su usuario") { usuario = it; error = "" }
        Spacer(Modifier.height(20.dp))
        Text("Contraseña", color = Navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        CampoTexto(contrasena, "Ingrese su contraseña", esPassword = true, icono = Icons.Filled.Lock) { contrasena = it; error = "" }
        if (error.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }
        Spacer(Modifier.height(28.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(56.dp)
                    .background(VerdeBoton, RoundedCornerShape(10.dp))
                    .semantics { role = Role.Button }
                    .clickable {
                        if (usuario.isBlank() || contrasena.isBlank()) {
                            error = "Escribe tu usuario y contraseña."
                        } else {
                            scope.launch {
                                val nombre = LocalStorage.iniciarSesion(usuario, contrasena)
                                if (nombre == null) {
                                    error = "El usuario o la contraseña no son correctos."
                                } else {
                                    AppState.usuario = nombre
                                    onEntrar()
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Iniciar sesión", color = SobreVerdeBoton, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .defaultMinSize(minHeight = 48.dp)
                .semantics { role = Role.Button }
                .clickable { onRecuperar() },
            contentAlignment = Alignment.Center
        ) {
            Text("¿Olvidaste tu contraseña?", color = VerdeBoton, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Aún no tienes cuenta? ", color = GrisTexto, fontSize = 15.sp)
            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .semantics { role = Role.Button }
                    .clickable { onCrearCuenta() },
                contentAlignment = Alignment.Center
            ) {
                Text("Regístrate", color = VerdeBoton, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun RecuperarContrasenaScreen(onBack: () -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var enviado by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        BarraTitulo("Recuperar contraseña", onBack = onBack)
        Spacer(Modifier.height(30.dp))
        Text("Restablece tu acceso", color = Navy, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Esta pantalla es una simulación: no se envía ningún mensaje real.", color = GrisTexto, fontSize = 15.sp, lineHeight = 22.sp)
        Spacer(Modifier.height(28.dp))
        Text("Usuario", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        CampoTexto(usuario, "Ingrese su usuario") { usuario = it; enviado = false }
        if (enviado) {
            Spacer(Modifier.height(14.dp))
            Text("Simulado. En una app real acá llegarían los pasos para recuperar el acceso.", color = VerdeBoton, fontSize = 14.sp)
        }
        Spacer(Modifier.height(28.dp))
        BotonVerde("Enviar instrucciones") { enviado = usuario.isNotBlank() }
    }
}

@Composable
fun RegistroScreen(onBack: () -> Unit, onRegistrado: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        BarraTitulo("Crear cuenta", onBack = onBack)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LogoEcoRoute(54)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Únete a EcoRoute", color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Tus datos se guardan en este dispositivo", color = GrisTexto, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(30.dp))
        Text("Nombre", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        CampoTexto(nombre, "Tu nombre") { nombre = it; error = "" }
        Spacer(Modifier.height(18.dp))
        Text("Usuario", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        CampoTexto(usuario, "Elige un usuario") { usuario = it; error = "" }
        Spacer(Modifier.height(18.dp))
        Text("Contraseña", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        CampoTexto(contrasena, "Mínimo 6 caracteres", esPassword = true, icono = Icons.Filled.Lock) { contrasena = it; error = "" }
        Spacer(Modifier.height(18.dp))
        Text("Confirmar contraseña", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        CampoTexto(confirmar, "Repite tu contraseña", esPassword = true, icono = Icons.Filled.Lock) { confirmar = it; error = "" }
        if (error.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }
        Spacer(Modifier.height(28.dp))
        BotonVerde("Crear cuenta") {
            val validacion = when {
                nombre.trim().length < 2 -> "Escribe tu nombre."
                usuario.trim().length < 3 -> "El usuario debe tener al menos 3 caracteres."
                contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
                contrasena != confirmar -> "Las contraseñas no coinciden."
                else -> ""
            }
            if (validacion.isNotBlank()) {
                error = validacion
            } else {
                scope.launch {
                    val creada = LocalStorage.crearCuenta(nombre, usuario, contrasena, AppState.rol)
                    if (!creada) {
                        error = "Ese usuario ya está registrado."
                    } else {
                        error = ""
                        AppState.usuario = nombre.trim()
                        onRegistrado()
                    }
                }
            }
        }
        Spacer(Modifier.height(36.dp))
    }
}

@Composable
fun TarjetaRol(titulo: String, descripcion: String, icono: ImageVector, colorIcono: Color, fondoIcono: Color, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (seleccionado) VerdeSuave else Superficie, RoundedCornerShape(24.dp))
            .semantics {
                role = Role.Button
                selected = seleccionado
            }
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(fondoIcono, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, color = Navy, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(descripcion, color = GrisTexto, fontSize = 14.sp, lineHeight = 20.sp)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Navy)
    }
}

@Composable
fun ConfiguraExperienciaScreen(onBack: () -> Unit, onContinuar: () -> Unit) {
    var rol by remember { mutableStateOf(AppState.rol) }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
                .heightIn(min = maxHeight)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            BarraTitulo("Configura tu experiencia", onBack = onBack)
            Spacer(Modifier.height(10.dp))
            Text("¿Cómo usarás EcoRoute?", color = Navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Elige el rol que usarás en EcoRoute para continuar con tu registro", color = GrisTexto, fontSize = 15.sp, lineHeight = 22.sp)
            Spacer(Modifier.height(32.dp))
            TarjetaRol(
                "Soy vecino",
                "Registrar bolsas, programar recolecciones y consultar el estado",
                Icons.Outlined.Home, Color(0xFF1B7A2F), VerdeClaro,
                rol == "vecino"
            ) { rol = "vecino" }
            Spacer(Modifier.height(20.dp))
            TarjetaRol(
                "Soy recolector",
                "Organizar paradas, navegar con brújula y completar la bitácora.",
                Icons.Outlined.Route, Color(0xFF8A8A1E), Amarillo,
                rol == "recolector"
            ) { rol = "recolector" }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))
            Text("Diseñado para funcionar con pocos datos y bajo consumo...", color = GrisTexto, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            BotonVerde("Continuar") {
                scope.launch {
                    LocalStorage.guardarRolUsuario(rol)
                    onContinuar()
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
fun TarjetaPermiso(titulo: String, descripcion: String, icono: ImageVector, concedido: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Superficie, RoundedCornerShape(28.dp))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(VerdeClaro, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = Color(0xFF1B7A2F), modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, color = Navy, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(descripcion, color = GrisTexto, fontSize = 14.sp, lineHeight = 20.sp)
        }
        if (concedido) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(VerdeBoton, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = SobreVerdeBoton, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun PermisosScreen(onBack: () -> Unit, onContinuar: () -> Unit) {
    var locationGranted by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        locationGranted = hasLocationPermission(
            fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true,
            coarseGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        )
        if (locationGranted == true) onContinuar()
    }
    val scroll = rememberScrollState()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
                .heightIn(min = maxHeight)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            BarraTitulo("Permisos necesarios", onBack = onBack)
            Spacer(Modifier.height(6.dp))
            Text("Prepara EcoRoute", color = Navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Estos permisos hacen posible la ruta y la navegación directa.", color = GrisTexto, fontSize = 15.sp, lineHeight = 24.sp)
            Spacer(Modifier.height(28.dp))
            TarjetaPermiso("Ubicación", "Para ubicar paradas y calcular la distancia", Icons.Outlined.Place, locationGranted == true)
            Spacer(Modifier.height(20.dp))
            TarjetaPermiso("Sensores de movimiento", "Para orientar la brújula hacia la parada", Icons.Outlined.RadioButtonChecked)
            Spacer(Modifier.height(20.dp))
            TarjetaPermiso("Notificaciones", "Para avisarte cambios de estado", Icons.Outlined.NotificationsNone)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))
            if (locationGranted == false) {
                Text(
                    "No se concedió el permiso de ubicación. Puedes continuar sin navegación por brújula.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(14.dp))
                BotonVerde("Continuar", onClick = onContinuar)
            } else {
                BotonVerde("Permitir y continuar") {
                    val permisos = mutableListOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    if (Build.VERSION.SDK_INT >= 33) permisos.add(Manifest.permission.POST_NOTIFICATIONS)
                    launcher.launch(permisos.toTypedArray())
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Puedes ajustar permisos desde Android",
                color = GrisTexto,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}
