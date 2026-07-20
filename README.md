# EcoRoute - Prototipo funcional en Kotlin

Prototipo Android (Jetpack Compose) basado en el diseño de Figma.

## Cómo correrlo
1. Abrir la carpeta `EcoRoute` en Android Studio (Hedgehog o superior).
2. Dejar que sincronice Gradle. El proyecto ya incluye el wrapper completo.
3. Run en un dispositivo o emulador con Android 7.0+ (API 24).

En el primer uso, pulsa **Regístrate**, crea una cuenta local y luego elige el rol. El mapa necesita conexión a Internet para descargar las calles.

## Flujo
- Registro o login → Configura tu experiencia (vecino / recolector) → Permisos (pide permisos reales de ubicación y notificaciones).
- **Vecino:** Home → Registrar reciclaje (paso 1 materiales/volumen) → Detalles (paso 2 dirección/horario/referencia) → Confirmar (paso 3) → Enviar → Estado de solicitud. Tabs: Inicio, Solicitudes, Historial (con filtros), Perfil.
- **Recolector:** Ruta de hoy → Iniciar ruta → Paradas (buscador funcional) → Detalle de parada → Navegar con brújula → Llegué → Confirmar recolección → siguiente parada. Al completar las 4 paradas de la lista sale la pantalla de Ruta completada.

## Funciones agregadas

- Registro y login locales con validaciones.
- Botón de ojo para mostrar u ocultar las contraseñas.
- Las contraseñas no se guardan como texto: se protegen con sal y PBKDF2.
- Mapas reales e interactivos de OpenStreetMap, con calles, zoom, marcadores y recorrido de paradas.
- Modo oscuro para toda la interfaz, configurable desde **Perfil** y guardado entre aperturas.

## Notas
- La brújula usa el sensor real del teléfono (TYPE_ROTATION_VECTOR); en emulador se puede mover desde Extended Controls → Virtual sensors.
- Los mapas usan OpenStreetMap mediante osmdroid y no necesitan una clave de Google Maps.
- Las cuentas y la preferencia de modo oscuro se guardan solo en el dispositivo. El resto del estado del prototipo permanece en memoria y no usa backend.
