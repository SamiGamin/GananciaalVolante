package com.martinez.gananciaalvolante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinez.gananciaalvolante.data.local.entity.Recorrido
import kotlinx.coroutines.flow.Flow

@Dao
interface RecorridoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recorrido: Recorrido)

    // ### CAMBIO IMPORTANTE: AHORA DEVUELVE UN FLOW ###
    @Query("SELECT SUM(distanciaKm) FROM recorridos WHERE fechaInicio BETWEEN :inicioDelDia AND :finDelDia")
    fun getDistanciaEnRangoFlow(inicioDelDia: Long, finDelDia: Long): Flow<Double?>

    // ### NUEVA CONSULTA PARA EL TIEMPO TOTAL ###
    @Query("SELECT * FROM recorridos WHERE fechaInicio BETWEEN :inicioDelDia AND :finDelDia")
    fun getRecorridosEnRangoFlow(inicioDelDia: Long, finDelDia: Long): Flow<List<Recorrido>>

    @Query("SELECT * FROM recorridos ORDER BY fechaInicio DESC")
    fun getAllRecorridos(): Flow<List<Recorrido>>

    @Query("UPDATE recorridos SET turnoId = :turnoId WHERE fechaInicio BETWEEN :inicio AND :fin AND turnoId IS NULL")
    suspend fun asociarRecorridosAlTurno(turnoId: Long, inicio: Long, fin: Long)

    @Query("SELECT * FROM recorridos WHERE fechaInicio BETWEEN :inicio AND :fin AND turnoId IS NULL")
    suspend fun getRecorridosSinTurno(inicio: Long, fin: Long): List<Recorrido>

    @Query("SELECT * FROM recorridos WHERE fechaInicio >= :timestampInicio AND turnoId IS NULL")
    suspend fun getRecorridosDesde(timestampInicio: Long): List<Recorrido>

    @Query("SELECT * FROM recorridos WHERE turnoId = :turnoId ORDER BY fechaInicio DESC")
    fun getRecorridosByTurnoId(turnoId: Long): Flow<List<Recorrido>>

}