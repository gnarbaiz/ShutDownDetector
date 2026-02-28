# Guía de Configuración

## 📧 Configuración de Email

Para habilitar el envío automático de emails, edita el archivo:
`app/src/main/java/com/shutdowndetector/data/remote/EmailSenderImpl.kt`

### Configuración para Gmail

1. **Habilita la verificación en 2 pasos** en tu cuenta de Google
2. **Genera una "Contraseña de aplicación"**:
   - Ve a tu cuenta de Google → Seguridad
   - Busca "Contraseñas de aplicaciones"
   - Genera una nueva contraseña para "Correo"
   - Copia la contraseña generada

3. **Edita EmailSenderImpl.kt**:
```kotlin
private val fromEmail: String = "tu-email@gmail.com"
private val fromPassword: String = "tu-contraseña-de-aplicacion"
private val toEmail: String = "destinatario@gmail.com"
```

### Configuración para otros proveedores SMTP

Si usas otro proveedor de email, ajusta:
```kotlin
private val smtpHost: String = "smtp.tu-proveedor.com"
private val smtpPort: String = "587" // o "465" para SSL
```

## 🔔 Notificaciones

Las notificaciones están habilitadas por defecto. Para Android 13+ (API 33+), el permiso `POST_NOTIFICATIONS` se solicita automáticamente.

## 🔋 Optimización de Batería

Para asegurar que el servicio funcione correctamente en segundo plano:

1. **Desactiva la optimización de batería** para esta app:
   - Configuración → Apps → ShutDownDetector → Batería
   - Selecciona "Sin restricciones"

2. **Mantén la app en la lista de "No optimizar"** en la configuración de batería del dispositivo

## 📱 Permisos Requeridos

La app solicita automáticamente los permisos necesarios:
- ✅ Estado de red (automático)
- ✅ Internet (automático)
- ✅ Notificaciones (solicitado en Android 13+)

## 🚀 Inicio Automático

El servicio se inicia automáticamente:
- Al abrir la app
- Al reiniciar el dispositivo (BootReceiver)
- Al cambiar el estado de conectividad
- Al conectar/desconectar el cargador

## 📊 Base de Datos

Los eventos se guardan localmente en Room Database:
- Ubicación: `/data/data/com.shutdowndetector/databases/power_events_database`
- Los datos persisten aunque la app sea cerrada
- Puedes exportar los datos a CSV desde la UI

## 🔧 Troubleshooting

### El servicio no se inicia
- Verifica que la optimización de batería esté desactivada
- Reinicia el dispositivo
- Abre la app manualmente

### Los emails no se envían
- Verifica la configuración de email en `EmailSenderImpl.kt`
- Asegúrate de usar una "Contraseña de aplicación" para Gmail
- Verifica la conexión a Internet
- Revisa los logs con `adb logcat | grep EmailSender`

### Los eventos no se detectan
- Verifica que el Wi-Fi esté conectado
- Verifica que el dispositivo esté cargando
- Revisa los logs con `adb logcat | grep PowerMonitoring`

## 📝 Logs

Para ver los logs de la aplicación:
```bash
adb logcat | grep -E "ShutDownDetector|PowerMonitoring|EmailSender"
```

Para ver logs de Timber (solo en modo debug):
```bash
adb logcat | grep Timber
```
