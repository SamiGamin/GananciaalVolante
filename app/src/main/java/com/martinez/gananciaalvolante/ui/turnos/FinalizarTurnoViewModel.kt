package com.martinez.gananciaalvolante.ui.turnos

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinez.gananciaalvolante.data.local.entity.ResumenTurno
import com.martinez.gananciaalvolante.data.repository.VehiculoRepository
import com.martinez.gananciaalvolante.ui.dashboard.DashboardViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinalizarTurnoViewModel @Inject constructor(
    private val repository: VehiculoRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _resumenData = MutableStateFlow<ResumenTurno?>(null)
    val resumenData = _resumenData.asStateFlow()

    private val turnoInicioTimestamp: Long = sharedPreferences.getLong(DashboardViewModel.KEY_TURNO_INICIO_TS, 0L)

    init {
        if (turnoInicioTimestamp > 0) {
            viewModelScope.launch {
                // Pedimos al repositorio que calcule el resumen
                _resumenData.value = repository.getDatosParaResumenTurno(turnoInicioTimestamp)
            }
        }
    }

    fun onFinalizarTurno(gananciaBrutaStr: String, onTurnoFinalizado: () -> Unit) {
        val gananciaBruta = gananciaBrutaStr.toDoubleOrNull() ?: 0.0
        if (gananciaBruta > 0) {
            viewModelScope.launch {
                Log.d("Turno_DEBUG", "VM del Diálogo: Iniciando guardado de turno...")
                try {
                    repository.finalizarYGuardarTurno(turnoInicioTimestamp, System.currentTimeMillis(), gananciaBruta)
                    Log.d("Turno_DEBUG", "VM del Diálogo: Guardado en repositorio COMPLETO.")

                    Log.d("Turno_DEBUG", "VM del Diálogo: Timestamp de turno eliminado.")

                    onTurnoFinalizado() // Llamamos al callback
                    Log.d("Turno_DEBUG", "VM del Diálogo: Callback onTurnoFinalizado() EJECUTADO.")

                } catch (e: Exception) {
                    Log.e("Turno_DEBUG", "VM del Diálogo: ERROR al guardar el turno.", e)
                }
            }
        }
    }
}