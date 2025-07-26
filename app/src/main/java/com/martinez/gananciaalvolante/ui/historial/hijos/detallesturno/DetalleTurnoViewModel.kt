package com.martinez.gananciaalvolante.ui.historial.hijos.detallesturno

import android.util.Log
import androidx.lifecycle.ViewModel

import androidx.lifecycle.SavedStateHandle

import com.martinez.gananciaalvolante.data.local.entity.Gasto

import com.martinez.gananciaalvolante.data.local.entity.Recorrido
import com.martinez.gananciaalvolante.data.local.entity.Turno
import com.martinez.gananciaalvolante.data.repository.VehiculoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class DetalleTurnoViewModel @Inject constructor(
    private val repository: VehiculoRepository,
    savedStateHandle: SavedStateHandle // Hilt inyecta esto automáticamente
) : ViewModel() {


    private val turnoId: Long = savedStateHandle.get<Long>("turnoId")!!
    init {
        Log.d("DetalleTurno_DEBUG", "ViewModel inicializado para turnoId: $turnoId")
    }


    // 2. Exponemos los datos como Flows que la UI puede observar.
    // El ViewModel pide al repositorio todos los datos relacionados con ESE turno específico.
    val turno: Flow<Turno?> = repository.getTurnoById(turnoId)
    val recorridosDelTurno: Flow<List<Recorrido>> = repository.getRecorridosByTurnoId(turnoId)
    val gastosDelTurno: Flow<List<Gasto>> = repository.getGastosByTurnoId(turnoId)
//    val ingresosDelTurno: Flow<List<IngresoManual>> = repository.getIngresosByTurnoId(turnoId)
}