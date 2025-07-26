package com.martinez.gananciaalvolante.ui.historial.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.martinez.gananciaalvolante.R
import com.martinez.gananciaalvolante.databinding.ListItemHistorialBinding
import com.martinez.gananciaalvolante.databinding.ListItemTurnoBinding
import com.martinez.gananciaalvolante.di.util.Formatters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
private const val TIPO_TURNO = 1
private const val TIPO_GENERICO = 2
class HistorialAdapter (    private val onTurnoClick: (turnoId: Long) -> Unit ): ListAdapter<HistorialItem, RecyclerView.ViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Inflamos el layout correcto según el tipo de vista
        return when (viewType) {
            TIPO_TURNO -> {
                val binding = ListItemTurnoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TurnoViewHolder(binding)
            }
            else -> { // TIPO_GENERICO
                val binding = ListItemHistorialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                GenericoViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Hacemos el binding al ViewHolder correcto
        when (holder) {
            is TurnoViewHolder -> holder.bind(getItem(position) as HistorialItem.TurnoItem, onTurnoClick)
            is GenericoViewHolder -> holder.bind(getItem(position)) // Asumiendo que esta función ya existe
        }
    }

    class GenericoViewHolder(private val binding: ListItemHistorialBinding) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

        fun bind(item: HistorialItem) {
            binding.apply {
                // Formatear la fecha (común a todos)
                textViewDate.text = dateFormat.format(Date(item.fecha))

                // Usar un 'when' para manejar cada tipo de ítem
                when (item) {
                    is HistorialItem.RecorridoItem -> {
                        val recorrido = item.recorrido
                        imageViewIcon.setImageResource(R.drawable.ic_directions_car)
                        textViewTitle.text = "Recorrido"
                        textViewDetailPrimary.text = String.format("%.1f km", recorrido.distanciaKm)
                        // Calcular y formatear la duración
                        val duracionMs = recorrido.fechaFin - recorrido.fechaInicio
                        val (tiempo, unidad) = Formatters.getFormattedTimeWithUnit(duracionMs)
                        textViewDetailSecondary.text = "$tiempo $unidad"
                        textViewDetailSecondary.visibility = View.VISIBLE
                    }
                    is HistorialItem.GastoItem -> {
                        val gasto = item.gasto
                        imageViewIcon.setImageResource(R.drawable.ic_receipt) // Icono de recibo/gasto
                        textViewTitle.text = gasto.subcategoria ?: gasto.categoria
                        textViewDetailPrimary.text = Formatters.toColombianPesos( gasto.monto)

                        if (gasto.cantidad != null && gasto.unidad != null) {
                            textViewDetailSecondary.text = Formatters.toColombianPesos(gasto.monto)
                            textViewDetailSecondary.visibility = View.VISIBLE
                        } else {
                            textViewDetailSecondary.visibility = View.GONE
                        }
                    }
                    is HistorialItem.TurnoItem -> {
                        val turno = item.turno
                        imageViewIcon.setImageResource(R.drawable.ic_attach_money) // Ícono de ganancia
                        textViewTitle.text = "Turno de Trabajo"

                        // Detalle primario: Ganancia NETA
                        textViewDetailPrimary.text = Formatters.toColombianPesos(turno.gananciaNeta)

                        // Detalle secundario: Distancia y duración del turno
                        val (tiempo, unidad) = Formatters.getFormattedTimeWithUnit(turno.tiempoTotalTrabajadoMs)
                        textViewDetailSecondary.text = String.format(Locale.getDefault(), "%.1f km / %s %s", turno.totalKm, tiempo, unidad)

                        textViewDetailSecondary.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    class TurnoViewHolder(private val binding: ListItemTurnoBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

        fun bind(turnoItem: HistorialItem.TurnoItem, clickListener: (Long) -> Unit) {
            val turno = turnoItem.turno
            binding.apply {
                textViewDate.text = dateFormat.format(Date(turno.fechaInicio))

                // Resumen financiero
                textViewGananciaNetaValue.text = Formatters.toColombianPesos(turno.gananciaNeta)
                textViewGananciaHoraValue.text = Formatters.toColombianPesos(turno.gananciaPorHora)

                // Para la ganancia por KM, necesitamos calcularla aquí (bruta o neta)
                val gananciaPorKm = if (turno.totalKm > 0) turno.gananciaNeta / turno.totalKm else 0.0
                textViewGananciaKmValue.text = Formatters.toColombianPesos(gananciaPorKm)

                // Detalles del turno
                val (tiempoStr, unidadTiempoStr) = Formatters.getFormattedTimeWithUnit(turno.tiempoTotalTrabajadoMs)
                val gastoTotalStr = Formatters.toColombianPesos(turno.gananciaBruta - turno.gananciaNeta)

                textViewDetallesTurno.text = String.format(
                    Locale.getDefault(),
                    "Resumen: %.1f km en %s %s con gastos de %s",
                    turno.totalKm,
                    tiempoStr,
                    unidadTiempoStr,
                    gastoTotalStr
                )
            }
            itemView.setOnClickListener {
                clickListener(turnoItem.turno.id)
            }
        }
    }
    override fun getItemViewType(position: Int): Int {
        // Decidimos qué tipo de vista usar basándonos en el tipo de ítem
        return when (getItem(position)) {
            is HistorialItem.TurnoItem -> TIPO_TURNO
            else -> TIPO_GENERICO
        }
    }
    class DiffCallback : DiffUtil.ItemCallback<HistorialItem>() {
        override fun areItemsTheSame(oldItem: HistorialItem, newItem: HistorialItem): Boolean {
            return oldItem.id == newItem.id && oldItem.javaClass == newItem.javaClass
        }
        override fun areContentsTheSame(oldItem: HistorialItem, newItem: HistorialItem): Boolean {
            return oldItem == newItem
        }
    }
}