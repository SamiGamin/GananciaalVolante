package com.martinez.gananciaalvolante.ui.historial.util

import com.martinez.gananciaalvolante.data.local.entity.Ganancia
import com.martinez.gananciaalvolante.data.local.entity.Gasto
import com.martinez.gananciaalvolante.data.local.entity.Recorrido
import com.martinez.gananciaalvolante.data.local.entity.Turno

// Esta clase sellada representa los posibles tipos de ítems en nuestra lista.
sealed class HistorialItem {
    abstract val id: Long
    abstract val fecha: Long

    data class RecorridoItem(val recorrido: Recorrido) : HistorialItem() {
        override val id = recorrido.id
        override val fecha = recorrido.fechaInicio
    }
    data class GastoItem(val gasto: Gasto) : HistorialItem() {
        override val id = gasto.id
        override val fecha = gasto.fecha
    }
    data class TurnoItem(val turno: Turno) : HistorialItem() {
        override val id = turno.id
        override val fecha = turno.fechaInicio
    }
}