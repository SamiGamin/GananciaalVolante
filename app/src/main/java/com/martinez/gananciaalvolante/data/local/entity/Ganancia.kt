package com.martinez.gananciaalvolante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ganancias")
data class Ganancia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: Long,
    val monto: Double,
    val horasTrabajadas: Double, // Ej: 8.5 horas
    val descripcion: String?

    // --- Campos para Fase 2 (Sincronización) ---
    // var firestoreId: String? = null,
    // var needsSync: Boolean = true
)
