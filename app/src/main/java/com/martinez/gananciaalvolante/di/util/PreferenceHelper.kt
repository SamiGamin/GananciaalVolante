package com.martinez.gananciaalvolante.di.util

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager

// Definimos los valores posibles para que sea más seguro que usar strings
enum class MenuPosition { IZQUIERDA, DERECHA }

// Esta clase observará los cambios en una preferencia específica
class MenuPositionPreference(context: Context) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // LiveData que emitirá el nuevo valor cuando cambie
    val position: LiveData<MenuPosition> = object : LiveData<MenuPosition>() {
        private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "posicion_menu") {
                // Si la clave que cambió es la nuestra, actualizamos el valor del LiveData
                postValue(getCurrentPosition())
            }
        }

        override fun onActive() {
            super.onActive()
            // Cuando alguien empieza a observar, emitimos el valor actual
            value = getCurrentPosition()
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        }

        override fun onInactive() {
            super.onInactive()
            // Cuando nadie observa, dejamos de escuchar para ahorrar recursos
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    private fun getCurrentPosition(): MenuPosition {
        val positionString = sharedPreferences.getString("posicion_menu", "izquierda")
        return if (positionString == "derecha") MenuPosition.DERECHA else MenuPosition.IZQUIERDA
    }
}

enum class DistanceUnit { KM, MI }

class UnitPreference(context: Context) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // LiveData que emitirá la nueva unidad cuando cambie
    val unit: LiveData<DistanceUnit> = object : LiveData<DistanceUnit>() {
        private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "unidad_distancia") {
                postValue(getCurrentUnit())
            }
        }

        override fun onActive() {
            super.onActive()
            value = getCurrentUnit()
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        }

        override fun onInactive() {
            super.onInactive()
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    private fun getCurrentUnit(): DistanceUnit {
        val unitString = sharedPreferences.getString("unidad_distancia", "km")
        return if (unitString == "mi") DistanceUnit.MI else DistanceUnit.KM
    }
}