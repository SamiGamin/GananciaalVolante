package com.martinez.gananciaalvolante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinez.gananciaalvolante.data.local.entity.Tanqueada

@Dao
interface TanqueadaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tanqueada: Tanqueada)

    // Consulta para sumar el costo total de las tanqueadas en un rango de fechas (para el gasto del mes)
    @Query("SELECT SUM(costoTotal) FROM tanqueadas WHERE fecha BETWEEN :inicioDelRango AND :finDelRango")
    suspend fun getGastoEnRango(inicioDelRango: Long, finDelRango: Long): Double?
}