package com.martinez.gananciaalvolante.ui.dashboard

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinez.gananciaalvolante.data.local.entity.EstadisticasDiarias
import com.martinez.gananciaalvolante.data.repository.VehiculoRepository
import com.martinez.gananciaalvolante.di.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.martinez.gananciaalvolante.di.util.Constants.ACTION_STOP_SERVICE
import com.martinez.gananciaalvolante.di.util.DistanceUnit
import com.martinez.gananciaalvolante.di.util.UnitPreference
import com.martinez.gananciaalvolante.di.util.services.TrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: VehiculoRepository,
    application: Application,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    val isTracking = TrackingService.isTracking
    val activeTripData = TrackingService.activeTripData
    val currentSpeed = TrackingService.currentSpeedKmh
    val unitPreference = UnitPreference(application)
    val currentUnit: LiveData<DistanceUnit> = unitPreference.unit
    private val _isModoTrabajo = MutableStateFlow(false)
    val isModoTrabajo = _isModoTrabajo.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DashboardUIEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val estadisticasDiarias: StateFlow<EstadisticasDiarias> = repository.getEstadisticasDelDia()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EstadisticasDiarias(0.0, 0L)
        )

    init {
        _isModoTrabajo.value = sharedPreferences.getLong(KEY_TURNO_INICIO_TS, 0L) > 0L
    }
    fun onStartStopTrip(context: Context) {
        if (isTracking.value) {
            sendCommandToService(context, ACTION_STOP_SERVICE)
        } else {
            sendCommandToService(context, ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun sendCommandToService(context: Context, action: String) {
        Intent(context, TrackingService::class.java).also {
            it.action = action
            context.startService(it)
        }
    }
    fun onModoTrabajoChanged(isActive: Boolean) {
        if (isActive) {
            sharedPreferences.edit().putLong(KEY_TURNO_INICIO_TS, System.currentTimeMillis()).apply()
            _isModoTrabajo.value = true
        } else {
            viewModelScope.launch {
                _uiEvent.emit(DashboardUIEvent.ShowFinalizarTurnoDialog)
            }
        }

    }
    fun turnoFinalizado() {
        Log.d("Turno_DEBUG", "VM del Dashboard: turnoFinalizado() llamado. Actualizando isModoTrabajo a false.")
        sharedPreferences.edit().remove(KEY_TURNO_INICIO_TS).apply()
        _isModoTrabajo.value = false

        Log.d("Turno_DEBUG", "VM del Dashboard: isModoTrabajo actualizado a false.")
    }

    companion object {
        const val KEY_TURNO_INICIO_TS = "turno_inicio_timestamp"
    }

}
sealed class DashboardUIEvent {
    object ShowFinalizarTurnoDialog : DashboardUIEvent()
}