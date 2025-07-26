package com.martinez.gananciaalvolante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehiculos")
data class Vehiculo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String, // Ej: "Coche del Trabajo"
    val marca: String,
    val modelo: String,
    val odometro: Double // Se actualiza con cada recorrido/tanqueada.

    // --- Campos para Fase 2 (Sincronización) ---
    // var firestoreId: String? = null,
    // var needsSync: Boolean = true
)
