package com.martinez.gananciaalvolante.ui.settings.vehiculo

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.martinez.gananciaalvolante.MainActivity
import com.martinez.gananciaalvolante.R
import com.martinez.gananciaalvolante.data.local.entity.Vehiculo
import com.martinez.gananciaalvolante.databinding.DialogEditVehiculoBinding
import com.martinez.gananciaalvolante.di.util.DistanceUnit
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditVehiculoDialogFragment : DialogFragment() {

    private var _binding: DialogEditVehiculoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VehiculoViewModel by viewModels()

    /* override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         // Aplicamos nuestro estilo personalizado al diálogo
         setStyle(STYLE_NORMAL, R.style.Theme_GananciaAlVolante_Dialog_Alert)
     }

     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
         _binding = DialogEditVehiculoBinding.inflate(inflater, container, false)
         return binding.root
     }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)

         // Observamos el vehículo desde el ViewModel para poblar los campos
         viewLifecycleOwner.lifecycleScope.launch {
             viewModel.vehiculo.collect { vehiculo ->
                 vehiculo?.let {
                     binding.editTextNombreVehiculo.setText(it.nombre)
                     binding.editTextMarcaVehiculo.setText(it.marca)
                     binding.editTextModeloVehiculo.setText(it.modelo)
                 }
             }
         }

         setupListeners()
     }

     private fun setupListeners() {
         binding.buttonCancelar.setOnClickListener { dismiss() }

         binding.buttonGuardar.setOnClickListener {
             val nombre = binding.editTextNombreVehiculo.text.toString()
             val marca = binding.editTextMarcaVehiculo.text.toString()
             val modelo = binding.editTextModeloVehiculo.text.toString()

             if (nombre.isNotBlank()) {
                 viewModel.guardarVehiculo(nombre, marca, modelo)
                 dismiss()
             } else {
                 binding.editTextNombreVehiculo.error = "El nombre es requerido"
             }
         }
     }
 */

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = DialogEditVehiculoBinding.inflate(LayoutInflater.from(requireContext()))
        observeVehicleData()
        // Usamos MaterialAlertDialogBuilder para crear el diálogo
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_directions_car)
            .setTitle("Datos del Vehículo")
            .setView(binding.root)
            .setPositiveButton("Guardar", null) // El listener se sobreescribe abajo
            .setNegativeButton("Cancelar", null) // El listener por defecto es cerrar
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                saveData()
            }
        }

        return dialog
    }

    private fun observeVehicleData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.vehiculo.filterNotNull().collect { vehiculo ->
                    _binding?.let { b ->
                        b.editTextNombreVehiculo.setText(vehiculo.nombre)
                        b.editTextMarcaVehiculo.setText(vehiculo.marca)
                        b.editTextModeloVehiculo.setText(vehiculo.modelo)

                        // ### LÓGICA CLAVE PARA EL ODÓMETRO ###
                        if (vehiculo.odometro > 0) {
                            // Si el odómetro ya tiene un valor, lo mostramos como no editable.
                            b.editTextOdometroVehiculo.setText(
                                String.format(
                                    "%.1f",
                                    vehiculo.odometro
                                )
                            )
                            b.editTextOdometroVehiculo.isEnabled = false // Deshabilitar edición
                            b.layoutOdometroVehiculo.helperText =
                                "Se actualiza automáticamente con cada recorrido."
                            b.layoutOdometroVehiculo.setEndIconDrawable(0) // Quitar cualquier ícono
                        } else {
                            // Si el odómetro es 0, permitimos la edición.
                            b.editTextOdometroVehiculo.setText("") // Dejar vacío para que el hint se vea
                            b.editTextOdometroVehiculo.isEnabled = true
                            b.layoutOdometroVehiculo.helperText =
                                "Introduce el kilometraje actual de tu vehículo."
                            b.layoutOdometroVehiculo.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT) // Añadir 'x' para borrar
                        }
                    }
                }
            }
        }
    }

    private fun saveData() {
        _binding?.let { b ->
            val nombre = b.editTextNombreVehiculo.text.toString()
            val marca = b.editTextMarcaVehiculo.text.toString()
            val modelo = b.editTextModeloVehiculo.text.toString()
            val odometroStr = b.editTextOdometroVehiculo.text.toString()

            if (nombre.isBlank()) {
                b.editTextNombreVehiculo.error = "El nombre es requerido"
                return
            }

            // Validamos el odómetro solo si es editable
            val odometroInicial = if (b.editTextOdometroVehiculo.isEnabled) {
                odometroStr.toDoubleOrNull() ?: 0.0
            } else {
                null // No lo cambiaremos si no es editable
            }

            viewModel.guardarVehiculo(nombre, marca, modelo, odometroInicial)
            dismiss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditVehiculoDialog"
    }
}
