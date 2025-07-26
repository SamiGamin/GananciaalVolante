package com.martinez.gananciaalvolante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinez.gananciaalvolante.data.local.entity.Ganancia
import kotlinx.coroutines.flow.Flow

@Dao
interface GananciaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ganancia: Ganancia)

    // Consulta para sumar el monto de las ganancias en un rango de fechas (para la ganancia del día)
    @Query("SELECT SUM(monto) FROM ganancias WHERE fecha BETWEEN :inicioDelRango AND :finDelRango")
    suspend fun getGananciaEnRango(inicioDelRango: Long, finDelRango: Long): Double?
    @Query("SELECT * FROM ganancias ORDER BY fecha DESC")
    fun getAllGanancias(): Flow<List<Ganancia>>
}