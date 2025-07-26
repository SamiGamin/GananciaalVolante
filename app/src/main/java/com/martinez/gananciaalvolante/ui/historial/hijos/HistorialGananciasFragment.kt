package com.martinez.gananciaalvolante.ui.historial.hijos


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.martinez.gananciaalvolante.R
import com.martinez.gananciaalvolante.databinding.FragmentHistorialGananciasBinding
import com.martinez.gananciaalvolante.ui.historial.HistorialFragmentDirections
import com.martinez.gananciaalvolante.ui.historial.util.HistorialAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistorialGananciasFragment : Fragment(R.layout.fragment_historial_ganancias) {

    private var _binding: FragmentHistorialGananciasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistorialGananciasViewModel by viewModels()
    private lateinit var historialAdapter: HistorialAdapter // Reutilizamos el adaptador universal

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistorialGananciasBinding.bind(view)

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.turnoItems.collect { items ->
                    historialAdapter.submitList(items)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        historialAdapter = HistorialAdapter { turnoId ->
            // 2. Buscamos el NavController. Usamos el del fragmento padre (HistorialFragment)
            //    porque la acción de navegación está definida a ese nivel.
            val navController = requireParentFragment().requireParentFragment().findNavController()

            Log.d("DetalleTurno_DEBUG", "Clic en turno con ID: $turnoId. Navegando...")
            val action = HistorialFragmentDirections.actionHistorialFragmentToDetalleTurnoFragment(turnoId)

            // 4. Navegamos.
            navController.navigate(action)
        }
        binding.recyclerViewGanancias.adapter = historialAdapter
        binding.recyclerViewGanancias.layoutManager = LinearLayoutManager(requireContext())
        }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}