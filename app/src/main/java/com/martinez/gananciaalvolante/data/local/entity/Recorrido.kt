package com.martinez.gananciaalvolante.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "recorridos",
    foreignKeys = [ForeignKey(entity = Vehiculo::class, parentColumns = ["id"], childColumns = ["vehiculoId"], onDelete = ForeignKey.CASCADE)])
data class Recorrido(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehiculoId: Long, // Vincula este recorrido a un vehículo
    val fechaInicio: Long, // Usar System.currentTimeMillis()
    val fechaFin: Long,
    val distanciaKm: Double,
    var turnoId: Long? = null

    // --- Campos para Fase 2 (Sincronización) ---
    // var firestoreId: String? = null,
    // var needsSync: Boolean = true
)