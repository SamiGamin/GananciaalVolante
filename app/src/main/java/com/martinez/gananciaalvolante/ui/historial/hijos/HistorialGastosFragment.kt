package com.martinez.gananciaalvolante.ui.historial.hijos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.martinez.gananciaalvolante.R
import com.martinez.gananciaalvolante.databinding.FragmentHistorialGastosBinding
import com.martinez.gananciaalvolante.ui.historial.util.HistorialAdapter
import com.martinez.gananciaalvolante.ui.historial.hijos.HistorialGastosViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistorialGastosFragment : Fragment(R.layout.fragment_historial_gastos) {

    private val viewModel: HistorialGastosViewModel by viewModels()
    private lateinit var binding: FragmentHistorialGastosBinding
    private lateinit var historialAdapter: HistorialAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHistorialGastosBinding.bind(view)

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gastosItems.collect { items ->
                    historialAdapter.submitList(items)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        historialAdapter = HistorialAdapter{
            // Aquí puedes definir la acción al hacer clic en un gasto.
            // Por ejemplo, navegar a un detalle del gasto.
            // Si no necesitas navegación, puedes dejarlo vacío.
        }
        binding.recyclerViewGastos.apply {
            adapter = historialAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
}