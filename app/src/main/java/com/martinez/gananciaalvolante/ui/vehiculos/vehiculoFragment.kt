package com.martinez.gananciaalvolante.ui.vehiculos

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.martinez.gananciaalvolante.R

class vehiculoFragment : Fragment() {

    companion object {
        fun newInstance() = vehiculoFragment()
    }

    private val viewModel: VehiculoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_vehiculo, container, false)
    }
}