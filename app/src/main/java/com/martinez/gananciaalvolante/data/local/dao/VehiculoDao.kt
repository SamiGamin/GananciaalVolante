package com.martinez.gananciaalvolante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.martinez.gananciaalvolante.data.local.entity.Vehiculo
import kotlinx.coroutines.flow.Flow

@Dao
interface VehiculoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehiculo(vehiculo: Vehiculo)

    // Usamos Flow para que si se añade un coche, la lista se actualice sola en la UI.
    @Query("SELECT * FROM vehiculos")
    fun getAllVehiculos(): Flow<List<Vehiculo>>
    @Query("SELECT * FROM vehiculos LIMIT 1")
    suspend fun getFirstVehiculo(): Vehiculo?

    @Query("SELECT * FROM vehiculos WHERE id = :vehiculoId LIMIT 1")
    suspend fun getVehiculoById(vehiculoId: Long): Vehiculo?
    @Update
    suspend fun updateVehiculo(vehiculo: Vehiculo)
}