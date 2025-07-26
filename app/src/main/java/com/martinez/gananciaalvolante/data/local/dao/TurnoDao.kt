package com.martinez.gananciaalvolante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.martinez.gananciaalvolante.data.local.entity.Turno
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {
    @Insert
    suspend fun insert(turno: Turno): Long // Devolver el ID del turno insertado
    @Query("SELECT * FROM turnos ORDER BY fechaInicio DESC")
    fun getAllTurnos(): Flow<List<Turno>>
    @Query("SELECT * FROM turnos WHERE id = :turnoId")
    fun getTurnoById(turnoId: Long): Flow<Turno?>
}