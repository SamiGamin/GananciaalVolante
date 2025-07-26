package com.martinez.gananciaalvolante.ui.recorridos

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.martinez.gananciaalvolante.R

class recorridoFragment : Fragment() {

    companion object {
        fun newInstance() = recorridoFragment()
    }

    private val viewModel: RecorridoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recorrido, container, false)
    }
}