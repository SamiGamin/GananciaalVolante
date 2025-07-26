package com.martinez.gananciaalvolante.ui.gastos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinez.gananciaalvolante.data.local.entity.Gasto
import com.martinez.gananciaalvolante.data.repository.VehiculoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
private const val TAG = "GastosVM_DEBUG"
@HiltViewModel
class GastosViewModel @Inject constructor(
    private val repository: VehiculoRepository
) : ViewModel() {


    // Función que el DialogFragment llamará al hacer clic en "Guardar"
    fun guardarGasto(
        categoria: String,
        subcategoria: String?,
        costoTotal: Double,
        descripcion: String?,
        cantidadCombustible: Double?,
        unidadCombustible: String?
    ) {
        Log.d(TAG, "Función guardarGasto() llamada con Costo: $costoTotal") // <-- LOG 5
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Obtener el ID del vehículo por defecto
            val vehiculoId = repository.getFirstVehiculo()?.id

            if (vehiculoId == null) {
                Log.e(TAG, "ERROR: No se pudo obtener el ID del vehículo. El gasto NO se guardará.") // <-- LOG 6
                // Podrías emitir un evento de error a la UI
                return@launch
            }

            // 2. Crear el objeto Gasto
            val nuevoGasto = Gasto(
                vehiculoId = vehiculoId,
                monto = costoTotal,
                fecha = System.currentTimeMillis(), // Usamos la fecha y hora actual
                categoria = categoria,
                subcategoria = subcategoria, // Nuevo campo en la entidad
                descripcion = descripcion,
                cantidad = cantidadCombustible, // Nuevo campo
                unidad = unidadCombustible // Nuevo campo
            )
            Log.d(TAG, "Objeto Gasto creado: $nuevoGasto") // <-- LOG 7

            // 3. Llamar al repositorio para guardarlo
            repository.saveGasto(nuevoGasto)
            Log.d(TAG, "Llamada a repository.saveGasto() realizada.") // <-- LOG 8
        }
    }
}