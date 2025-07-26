package com.martinez.gananciaalvolante.data.repository

import com.martinez.gananciaalvolante.data.local.entity.Recorrido
import com.martinez.gananciaalvolante.data.local.entity.Vehiculo
import kotlinx.coroutines.flow.Flow

interface IRepository {
    // Métodos para Vehículos
    fun getVehiculos(): Flow<List<Vehiculo>>
    suspend fun insertVehiculo(vehiculo: Vehiculo)
    // ... otros métodos

    // Métodos para Recorridos
    suspend fun saveRecorrido(recorrido: Recorrido)
    suspend fun getKilometrosTotalesDelDia(): Double
    // ... etc.
}
