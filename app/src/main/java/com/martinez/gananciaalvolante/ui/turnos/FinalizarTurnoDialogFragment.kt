package com.martinez.gananciaalvolante.ui.turnos

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.martinez.gananciaalvolante.databinding.DialogFinalizarTurnoBinding
import com.martinez.gananciaalvolante.di.util.Formatters
import com.martinez.gananciaalvolante.ui.dashboard.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FinalizarTurnoDialogFragment : DialogFragment() {

    private var _binding: DialogFinalizarTurnoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FinalizarTurnoViewModel by viewModels()
    // Obtenemos el ViewModel del Dashboard para poder comunicarnos con él
    private val dashboardViewModel: DashboardViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogFinalizarTurnoBinding.inflate(LayoutInflater.from(requireContext()))

        observeResumenData()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton("Finalizar y Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val gananciaStr = binding.editTextGananciaBruta.text.toString()
                val gananciaValue = gananciaStr.toDoubleOrNull() ?: 0.0

                if (gananciaValue > 0) {
                 positiveButton.isEnabled = false
                    Log.d(TAG, "Ganancia ingresada válida: $gananciaStr")

                 binding.editTextGananciaBruta.isEnabled = false
                    viewModel.onFinalizarTurno(gananciaStr){
                        dashboardViewModel.turnoFinalizado()
                        Log.d(TAG, "Turno finalizado y guardado correctamente.")
                        dismiss()
                    }
                }else{
                    binding.editTextGananciaBruta.error = "La ganancia bruta debe ser mayor a 0"
                    Log.d(TAG, "Ganancia ingresada no válida: $gananciaStr")
                }
            }
        }

        return dialog
    }

    private fun observeResumenData() {
        lifecycleScope.launch {
            viewModel.resumenData.filterNotNull().collect { resumen ->
                // Actualizar los TextViews del diálogo con los datos calculados
                binding.textViewResumenDistancia.text = String.format("%.1f km", resumen.distanciaKm)
                val (tiempo, unidad) = Formatters.getFormattedTimeWithUnit(resumen.tiempoMs)
                binding.textViewResumenTiempo.text = "$tiempo $unidad"
                binding.textViewResumenGastos.text = Formatters.toColombianPesos(resumen.gastoTotal)
            }
        }
    }
    override fun onStart() {
        super.onStart()
        // Opcional: Para evitar que el diálogo ocupe toda la pantalla
        // si el contenido es muy grande.
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(), // Ocupar el 90% del ancho
            ViewGroup.LayoutParams.WRAP_CONTENT // La altura se ajusta al contenido
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FinalizarTurnoDialog"
    }
}