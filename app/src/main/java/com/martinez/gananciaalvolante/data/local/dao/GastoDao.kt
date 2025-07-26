package com.martinez.gananciaalvolante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinez.gananciaalvolante.data.local.entity.Gasto
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gasto: Gasto)

    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    fun getAllGastos(): Flow<List<Gasto>>

    @Query("UPDATE gastos SET turnoId = :turnoId WHERE fecha BETWEEN :inicio AND :fin AND turnoId IS NULL")
    suspend fun asociarGastosAlTurno(turnoId: Long, inicio: Long, fin: Long)

    @Query("SELECT SUM(monto) FROM gastos WHERE turnoId = :turnoId")
    suspend fun getGastoTotalPorTurno(turnoId: Long): Double?

    @Query("SELECT * FROM gastos WHERE fecha BETWEEN :inicio AND :fin AND turnoId IS NULL")
    suspend fun getGastosSinTurno(inicio: Long, fin: Long): List<Gasto>

    @Query("SELECT * FROM gastos WHERE fecha >= :timestampInicio AND turnoId IS NULL")
    suspend fun getGastosDesde(timestampInicio: Long): List<Gasto>

    @Query("SELECT * FROM gastos WHERE turnoId = :turnoId ORDER BY fecha DESC")
    fun getGastosByTurnoId(turnoId: Long): Flow<List<Gasto>>

}