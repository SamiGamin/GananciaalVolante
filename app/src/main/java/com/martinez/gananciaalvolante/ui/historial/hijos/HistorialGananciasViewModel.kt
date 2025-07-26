package com.martinez.gananciaalvolante.ui.historial.hijos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinez.gananciaalvolante.data.repository.VehiculoRepository
import com.martinez.gananciaalvolante.ui.historial.util.HistorialItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistorialGananciasViewModel @Inject constructor(
    private val repository: VehiculoRepository
) : ViewModel() {

    // 1. Pide al Repositorio el Flow<List<Turno>>
    val turnoItems: StateFlow<List<HistorialItem.TurnoItem>> = repository.getAllTurnos()
        // 2. Transforma cada objeto 'Turno' en un 'HistorialItem.TurnoItem'
        .map { listaDeTurnos ->
            listaDeTurnos.map { turno ->
                HistorialItem.TurnoItem(turno)
            }
        }
        // 3. Lo expone como un StateFlow que la UI puede consumir
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}