package com.martinez.gananciaalvolante.ui.historial

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.martinez.gananciaalvolante.R
import com.martinez.gananciaalvolante.databinding.FragmentHistorialBinding
import com.martinez.gananciaalvolante.ui.historial.util.HistorialStateAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistorialFragment : Fragment(R.layout.fragment_historial) {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistorialBinding.bind(view)

        setupViewPager()

        setupToolbar()
    }

    private fun setupViewPager() {
        val adapter = HistorialStateAdapter(this)
        binding.viewPagerHistorial.adapter = adapter

        // Conectar el TabLayout con el ViewPager2
        TabLayoutMediator(binding.tabLayoutHistorial, binding.viewPagerHistorial) { tab, position ->
            tab.text = when (position) {
                0 -> "Recorridos"
                1 -> "Gastos"
                2 -> "Ganancias"
                else -> null
            }
        }.attach()
    }

    private fun setupToolbar() {
        binding.toolbarHistorial.setNavigationOnClickListener {
            // Navegar hacia atrás
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}