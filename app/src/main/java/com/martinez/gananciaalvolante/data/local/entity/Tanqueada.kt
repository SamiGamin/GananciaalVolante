package com.martinez.gananciaalvolante.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "tanqueadas",
    foreignKeys = [ForeignKey(entity = Vehiculo::class, parentColumns = ["id"], childColumns = ["vehiculoId"], onDelete = ForeignKey.CASCADE)])
data class Tanqueada(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehiculoId: Long,
    val fecha: Long,
    val costoTotal: Double,
    val litros: Double,
    val precioPorLitro: Double,
    val odometro: Double // Odómetro en el momento de la tanqueada.

    // --- Campos para Fase 2 (Sincronización) ---
    // var firestoreId: String? = null,
    // var needsSync: Boolean = true
)