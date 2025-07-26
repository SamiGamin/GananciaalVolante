package com.martinez.gananciaalvolante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "turnos")
data class Turno(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fechaInicio: Long,
    val fechaFin: Long,
    val gananciaBruta: Double, // El total que el usuario introduce
    val gananciaNeta: Double,  // Calculado: Bruta - Gastos
    val totalKm: Double,
    val tiempoTotalTrabajadoMs: Long,
    val costoPorKm: Double,    // Calculado
    val gananciaPorHora: Double,
    val gananciaPorKm: Double,// Calculado
)