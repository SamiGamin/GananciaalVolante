package com.martinez.gananciaalvolante.ui.historial.hijos.detallesturno

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.martinez.gananciaalvolante.R
import com.martinez.gananciaalvolante.databinding.FragmentDetalleTurnoBinding
import com.martinez.gananciaalvolante.di.util.Formatters
import com.martinez.gananciaalvolante.ui.historial.util.HistorialAdapter
import com.martinez.gananciaalvolante.ui.historial.util.HistorialItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class DetalleTurnoFragment : Fragment(R.layout.fragment_detalle_turno) {

    private var _binding: FragmentDetalleTurnoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetalleTurnoViewModel by viewModels()
    private lateinit var recorridosAdapter: HistorialAdapter
    private lateinit var gastosAdapter: HistorialAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetalleTurnoBinding.bind(view)

        setupAdapters()
        setupToolbar()
        observeData()
    }

    private fun setupAdapters() {
        // Creamos adaptadores. El callback de clic puede estar vacío aquí
        // porque no queremos navegar a ningún otro lugar desde esta pantalla.
        recorridosAdapter = HistorialAdapter {}
        gastosAdapter = HistorialAdapter {}

        binding.recyclerViewDetalleRecorridos.adapter = recorridosAdapter
        binding.recyclerViewDetalleGastos.adapter = gastosAdapter
    }

    private fun setupToolbar() {
        binding.toolbarDetalle.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observador para los datos principales del turno
                launch {
                    viewModel.turno.filterNotNull().collect { turno ->
                        // Rellenar Tarjeta 1: Resumen Financiero
                        binding.textViewDetalleGananciaNeta.text = Formatters.toColombianPesos(turno.gananciaNeta)
                        binding.textViewDetalleGananciaBruta.text = Formatters.toColombianPesos(turno.gananciaBruta)
                        val gastosTotales = turno.gananciaBruta - turno.gananciaNeta
                        binding.textViewDetalleGastosTotales.text = "- ${Formatters.toColombianPesos(gastosTotales)}"
                      /*  // Necesitamos el total de ingresos extra para este cálculo
                        launch {
                            viewModel.ingresosDelTurno.collect { ingresos ->
                                val totalIngresosExtra = ingresos.sumOf { it.monto }
                                binding.textViewDetalleIngresosExtra.text = Formatters.toColombianPesos(totalIngresosExtra)

                                // Gastos = Bruta - Neta (si los ingresos extra se incluyen en bruta)
                                val gastosTotales = turno.gananciaBruta - turno.gananciaNeta
                                binding.textViewDetalleGastosTotales.text = "- ${Formatters.toColombianPesos(gastosTotales)}"
                            }
                        }*/

                        // Rellenar Tarjeta 2: Métricas de Rendimiento
                        binding.textViewDetalleGananciaHora.text = Formatters.toColombianPesos(turno.gananciaPorHora)
                        binding.textViewDetalleCostoKm.text = Formatters.toColombianPesos(turno.costoPorKm)
                        val gananciaPorKm = if (turno.totalKm > 0) turno.gananciaNeta / turno.totalKm else 0.0
                        binding.textViewDetalleGananciaKm.text = Formatters.toColombianPesos(gananciaPorKm)

                        // Rellenar Tarjeta 3: Resumen de Actividad
                        val (tiempoStr, unidadTiempoStr) = Formatters.getFormattedTimeWithUnit(turno.tiempoTotalTrabajadoMs)
                        binding.textViewDetalleTiempoTurno.text = "$tiempoStr $unidadTiempoStr"
                        binding.textViewDetalleKmTurno.text = String.format(Locale.getDefault(), "%.1f km", turno.totalKm)/*
                        launch {
                            viewModel.recorridosDelTurno.collect { recorridos ->
                                binding.textViewDetalleNumRecorridos.text = recorridos.size.toString()
                            }
                        }*/
                    }
                }

                // --- OBSERVADOR PARA LA LISTA DE RECORRIDOS ---
                launch {
                    viewModel.recorridosDelTurno.collect { listaRecorridos ->
                        recorridosAdapter.submitList(listaRecorridos.map { HistorialItem.RecorridoItem(it) })
                    }
                }

                // --- OBSERVADOR PARA LA LISTA DE GASTOS ---
                launch {
                    viewModel.gastosDelTurno.collect { listaGastos ->
                        gastosAdapter.submitList(listaGastos.map { HistorialItem.GastoItem(it) })
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}