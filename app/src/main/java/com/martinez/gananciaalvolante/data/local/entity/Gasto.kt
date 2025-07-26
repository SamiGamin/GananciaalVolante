package com.martinez.gananciaalvolante.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "gastos",
    foreignKeys = [ForeignKey(
        entity = Vehiculo::class,
        parentColumns = ["id"],
        childColumns = ["vehiculoId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Gasto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,
    val monto: Double,
    val fecha: Long,
    val categoria: String,     // "Combustible", "Limpieza", etc.
    val subcategoria: String?, // "Gasolina", "Lavada", etc.
    val descripcion: String?,
    val cantidad: Double? = null, // Para litros, kWh, etc.
    val unidad: String? = null,
    var turnoId: Long? = null
)