package com.martinez.gananciaalvolante.di.util

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.DrawableCompat.applyTheme
import androidx.preference.PreferenceManager
import com.martinez.gananciaalvolante.data.local.entity.Vehiculo
import com.martinez.gananciaalvolante.data.repository.VehiculoRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GananciaAlVolanteApp: Application()  {
    // Hilt inyectará el repositorio aquí después de que se cree.
    // Usamos @Inject lateinit var para la inyección de campos.
    @Inject
    lateinit var repository: VehiculoRepository

    override fun onCreate() {
        super.onCreate()
        // Usamos una coroutine para no bloquear el hilo principal.
        CoroutineScope(Dispatchers.IO).launch {
            crearVehiculoPorDefectoSiNoExiste()
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themeValue = sharedPreferences.getString("theme_preference", "system")
        applyTheme(themeValue)
    }

    private suspend fun crearVehiculoPorDefectoSiNoExiste() {
        // Obtenemos la lista de vehículos. Usamos firstOrNull para tomar solo el primer valor del Flow.
        val vehiculos = repository.getVehiculos().firstOrNull()

        // Si la lista está vacía o es nula, creamos el vehículo por defecto.
        if (vehiculos.isNullOrEmpty()) {
            val vehiculoPorDefecto = Vehiculo(
                nombre = "Vehículo Principal",
                marca = "Sin especificar",
                modelo = "Sin especificar",
                odometro = 0.0,
            )
            repository.insertVehiculo(vehiculoPorDefecto)
        }
    }
    companion object {
        fun applyTheme(themeValue: String?) {
            when (themeValue) {
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
}