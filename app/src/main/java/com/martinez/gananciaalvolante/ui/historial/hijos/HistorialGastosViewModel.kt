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
class HistorialGastosViewModel @Inject constructor(
    private val repository: VehiculoRepository
) : ViewModel() {

    val gastosItems: StateFlow<List<HistorialItem.GastoItem>> = repository.getAllGastos()
        .map { listaDeGastos ->
            listaDeGastos.map { gasto -> HistorialItem.GastoItem(gasto) }
        }
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())
}