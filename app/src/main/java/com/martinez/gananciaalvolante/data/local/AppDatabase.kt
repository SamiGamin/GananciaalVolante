package com.martinez.gananciaalvolante.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.martinez.gananciaalvolante.data.local.dao.GananciaDao
import com.martinez.gananciaalvolante.data.local.dao.GastoDao
import com.martinez.gananciaalvolante.data.local.dao.RecorridoDao
import com.martinez.gananciaalvolante.data.local.dao.TanqueadaDao
import com.martinez.gananciaalvolante.data.local.dao.TurnoDao
import com.martinez.gananciaalvolante.data.local.dao.VehiculoDao
import com.martinez.gananciaalvolante.data.local.entity.Ganancia
import com.martinez.gananciaalvolante.data.local.entity.Gasto
import com.martinez.gananciaalvolante.data.local.entity.Recorrido
import com.martinez.gananciaalvolante.data.local.entity.Tanqueada
import com.martinez.gananciaalvolante.data.local.entity.Turno
import com.martinez.gananciaalvolante.data.local.entity.Vehiculo

@Database(
    entities = [
        Vehiculo::class,
        Recorrido::class,
        Gasto::class ,
        Ganancia::class,
        Turno::class
    ],
    version = 6, // <-- INCREMENTA ESTE NÚMERO (si estaba en 2, pon 3)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun recorridoDao(): RecorridoDao
    abstract fun gananciaDao(): GananciaDao
    abstract fun gastoDao(): GastoDao

    abstract fun turnoDao(): TurnoDao
}