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
class HistorialRecorridosViewModel @Inject constructor(
    private val repository: VehiculoRepository
) : ViewModel() {

    // Obtenemos el Flow de Recorridos del repositorio
    // y lo transformamos en un Flow de HistorialItem.RecorridoItem
    val recorridoItems: StateFlow<List<HistorialItem.RecorridoItem>> = repository.getAllRecorridos()
        .map { listaDeRecorridos ->
            listaDeRecorridos.map { recorrido ->
                HistorialItem.RecorridoItem(recorrido)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}