package com.martinez.gananciaalvolante.ui.historial.hijos.detallesturno

import android.os.Bundle
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
                        // Poblar las tarjetas de resumen
                        binding.textViewDetalleGananciaNeta.text = Formatters.toColombianPesos(turno.gananciaNeta)
                        // ... poblar todos los demás TextViews de las tarjetas
                    }
                }

                // Observador para la lista de recorridos
                launch {
                    viewModel.recorridosDelTurno.collect { listaRecorridos ->
                        recorridosAdapter.submitList(listaRecorridos.map { HistorialItem.RecorridoItem(it) })
                    }
                }

                // Observador para la lista de gastos
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