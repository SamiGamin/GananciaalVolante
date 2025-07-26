package com.martinez.gananciaalvolante.ui.settings.vehiculo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinez.gananciaalvolante.data.local.entity.Vehiculo
import com.martinez.gananciaalvolante.data.repository.VehiculoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehiculoViewModel @Inject constructor(
    private val repository: VehiculoRepository
) : ViewModel() {

    private val _vehiculo = MutableStateFlow<Vehiculo?>(null)
    val vehiculo = _vehiculo.asStateFlow()

    init {
        viewModelScope.launch {
            // Cargar el vehículo actual al iniciar el ViewModel
            _vehiculo.value = repository.getFirstVehiculo()
        }
    }

    fun guardarVehiculo(nombre: String, marca: String, modelo: String, odometroInicial: Double?) {
        viewModelScope.launch {
            // Obtenemos el vehículo actual, lo actualizamos y lo guardamos
            val vehiculoActual = _vehiculo.value
            if (vehiculoActual != null) {
                val odometroFinal = odometroInicial ?: vehiculoActual.odometro
                val vehiculoActualizado = vehiculoActual.copy(
                    nombre = nombre,
                    marca = marca,
                    modelo = modelo,
                    odometro = odometroFinal
                )
                repository.insertVehiculo(vehiculoActualizado)
                _vehiculo.value = vehiculoActualizado // Actualizar el estado
            }
        }
    }
}