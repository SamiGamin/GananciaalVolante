package com.martinez.gananciaalvolante.ui.historial.util

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.martinez.gananciaalvolante.ui.historial.hijos.HistorialGananciasFragment
import com.martinez.gananciaalvolante.ui.historial.hijos.HistorialGastosFragment
import com.martinez.gananciaalvolante.ui.historial.hijos.HistorialRecorridosFragment

class HistorialStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3 // Tenemos 3 pestañas

    override fun createFragment(position: Int): Fragment {
        // Devuelve el fragmento correspondiente a la posición de la pestaña
        return when (position) {
            0 -> HistorialRecorridosFragment()
            1 -> HistorialGastosFragment()
            2 -> HistorialGananciasFragment()
            else -> throw IllegalStateException("Posición de pestaña inválida")
        }
    }
}