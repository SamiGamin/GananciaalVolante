package com.martinez.gananciaalvolante.di.util.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.martinez.gananciaalvolante.R
import com.martinez.gananciaalvolante.data.local.entity.Recorrido
import com.martinez.gananciaalvolante.data.repository.VehiculoRepository
import com.martinez.gananciaalvolante.di.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.martinez.gananciaalvolante.di.util.Constants.ACTION_STOP_SERVICE
import com.martinez.gananciaalvolante.di.util.Constants.NOTIFICATION_CHANNEL_ID
import com.martinez.gananciaalvolante.di.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.martinez.gananciaalvolante.di.util.Constants.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripData(
    val timeInMillis: Long,
    val distanceInMeters: Float
)
 const val TAG = "TrackingService_DEBUG" // Etiqueta para filtrar en Logcat

@AndroidEntryPoint
class TrackingService : LifecycleService() {
    private var isAutoPaused = false
    private var timeWhenStopped = 0L
    private var lastSpeedCheckTime = 0L
    private val AUTO_PAUSE_THRESHOLD_MS = 3 * 60 * 1000
    private lateinit var sharedPreferences: SharedPreferences

    @Inject lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject lateinit var repository: VehiculoRepository

    companion object {
        val isTracking = MutableStateFlow(false)
        val activeTripData = MutableStateFlow<TripData?>(null)
        val currentSpeedKmh = MutableStateFlow(0f)
    }

    // --- Variables de estado del servicio ---
    private val timeRunInMillis = MutableLiveData<Long>()
    private var timeStarted = 0L
    private var totalDistanceInMeters = 0f
    private var lastLocation: Location? = null
    private lateinit var locationRequest: LocationRequest

    // --- Ciclo de Vida del Servicio ---

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        postInitialValues()
        Log.d(TAG, "Servicio CREADO. Inicializando valores.")

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500)
            .build()

        // El observador del tiempo ahora solo tiene una responsabilidad: actualizar los datos del viaje.
        timeRunInMillis.observe(this) {
            if (isTracking.value) {
                activeTripData.value = TripData(it, totalDistanceInMeters)
            }
        }
        updateLocationRequest()
        startLocationUpdates()
    }
    private fun updateLocationRequest() {
        val accuracy = when(sharedPreferences.getString("gps_accuracy", "balanced")) {
            "high" -> Priority.PRIORITY_HIGH_ACCURACY
            else -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        Log.d(TAG, "Actualizando precisión del GPS a: $accuracy")
        locationRequest = LocationRequest.Builder(accuracy, 1000)
            .setMinUpdateIntervalMillis(500)
            .build()
    }
    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "gps_accuracy") {
            // Si la precisión cambia, actualizamos el request y reiniciamos la escucha de ubicación
            updateLocationRequest()
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            startLocationUpdates()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
        intent?.action?.let { action ->
            Log.d(TAG, "Comando recibido: $action")
            when (action) {
                ACTION_START_OR_RESUME_SERVICE -> startTracking()
                ACTION_STOP_SERVICE -> stopTracking()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        Log.d(TAG, "Servicio DESTRUIDO. Deteniendo actualizaciones de ubicación.")
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    // --- Lógica Principal de Seguimiento ---

    private fun startTracking() {
        if (isTracking.value) {
            Log.d(TAG, "startTracking llamado, pero ya se está grabando. Ignorando.")
            return
        }
        Log.d(TAG, "--- INICIANDO SEGUIMIENTO ---")
        isTracking.value = true
        postInitialValuesForTrip() // Resetea los contadores para un nuevo viaje
        startTimer()
        startForegroundService()
    }

    private fun stopTracking() {
        if (!isTracking.value) {
            Log.d(TAG, "stopTracking llamado, pero no se estaba grabando. Ignorando.")
            return
        }
        Log.d(TAG, "--- DETENIENDO SEGUIMIENTO ---")
        isTracking.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        saveTripToDatabase()
    }

    // --- Lógica del GPS ---

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)


            result.lastLocation?.let { location ->
                Log.d(TAG, "Nueva Ubicación -> Lat: ${location.latitude}, Lon: ${location.longitude}, Speed: ${location.speed} m/s")
                currentSpeedKmh.value = location.speed * 3.6f
                 if (sharedPreferences.getBoolean("auto_pause_tracking", false)&& isTracking.value) {
                     if (currentSpeedKmh.value < 1.0f) { // Consideramos parado si la velocidad es casi cero
                         if (lastSpeedCheckTime == 0L) {
                             lastSpeedCheckTime = System.currentTimeMillis()
                         }
                         if (System.currentTimeMillis() - lastSpeedCheckTime > AUTO_PAUSE_THRESHOLD_MS && !isAutoPaused) {
                             pauseTracking()
                         }
                     } else {
                         if (isAutoPaused) {
                             resumeTracking()
                         }
                         lastSpeedCheckTime = 0L // Resetear el contador si hay movimiento
                     }
                }

                if (isTracking.value) {
                    if (lastLocation != null) {
                        // Usamos !! porque ya comprobamos que no es nulo.
                        val distance = lastLocation!!.distanceTo(location)
                        Log.d(TAG, "Distancia calculada: $distance metros")
                        if (distance > 1.0f) { // Filtro anti-ruido
                            totalDistanceInMeters += distance
                        }
                    }
                    lastLocation = location // Establecer o actualizar el punto de referencia
                }
            }
        }
    }
    private fun pauseTracking() {
        Log.d(TAG, "AUTO-PAUSA: El recorrido se ha pausado.")
        isAutoPaused = true
        // Guardamos el tiempo transcurrido hasta ahora
        timeWhenStopped = timeRunInMillis.value ?: 0L
    }

    private fun resumeTracking() {
        Log.d(TAG, "AUTO-REANUDACIÓN: El recorrido continúa.")
        isAutoPaused = false
        // Re-calculamos el tiempo de inicio para que el cronómetro continúe
        timeStarted = System.currentTimeMillis() - timeWhenStopped
        lastSpeedCheckTime = 0L
    }

    private fun startTimer() {
        isAutoPaused = false
        lastSpeedCheckTime = 0L
        timeStarted = System.currentTimeMillis()
        lifecycleScope.launch {
            while (isTracking.value) {
                if (!isAutoPaused) { // Solo actualizamos el tiempo si no está en pausa
                    val elapsedTime = System.currentTimeMillis() - timeStarted
                    timeRunInMillis.postValue(elapsedTime)
                }
                delay(1000L)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        Log.d(TAG, "Solicitando actualizaciones de ubicación del GPS...")
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }


    private fun saveTripToDatabase() {
        lifecycleScope.launch(Dispatchers.IO) { // Usamos IO para operaciones de base de datos
            val vehiculoActivo = repository.getVehiculos().firstOrNull()?.firstOrNull()
            if (vehiculoActivo == null) {
                Log.e(TAG, "No se pudo guardar el recorrido: No se encontró ningún vehículo en la base de datos.")
                return@launch
            }

            val recorrido = Recorrido(
                fechaInicio = timeStarted,
                fechaFin = System.currentTimeMillis(),
                distanciaKm = (totalDistanceInMeters / 1000.0),
                vehiculoId = vehiculoActivo.id
            )
            repository.saveRecorridoAndUpdateOdometro(recorrido)
            Log.d(TAG, "Recorrido guardado en la base de datos: ${recorrido.distanciaKm} km")
        }
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        // TODO: Actualizar la notificación con datos en tiempo real
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_car)
            .setContentTitle("Recorrido en Progreso")
            .setContentText("Calculando...")

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        Log.d(TAG, "Servicio pasado a primer plano con notificación ID: $NOTIFICATION_ID")
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun postInitialValues() {
        currentSpeedKmh.value = 0f
        isTracking.value = false
        postInitialValuesForTrip()
    }

    private fun postInitialValuesForTrip() {
        timeRunInMillis.postValue(0L)
        totalDistanceInMeters = 0f
        lastLocation = null
        activeTripData.value = TripData(0L, 0f)
    }
}