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
import com.martinez.gananciaalvolante.databinding.FragmentHistorialRecorridosBinding
import com.martinez.gananciaalvolante.ui.historial.util.HistorialAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistorialRecorridosFragment : Fragment(R.layout.fragment_historial_recorridos) {

    private var _binding: FragmentHistorialRecorridosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistorialRecorridosViewModel by viewModels()
    private lateinit var historialAdapter: HistorialAdapter // Reutilizamos el adaptador universal

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistorialRecorridosBinding.bind(view)

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recorridoItems.collect { items ->
                    // Le pasamos la lista de recorridos al mismo adaptador
                    historialAdapter.submitList(items)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        historialAdapter = HistorialAdapter {
            // Aquí puedes definir la acción al hacer clic en un recorrido.
            // Por ejemplo, navegar a un detalle del recorrido.
            // Si no necesitas navegación, puedes dejarlo vacío.
        }
        binding.recyclerViewRecorridos.apply {
            adapter = historialAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}