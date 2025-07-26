package com.martinez.gananciaalvolante.ui.gastos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.martinez.gananciaalvolante.R
import com.martinez.gananciaalvolante.databinding.DialogRegistrarGastoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrarGastoDialogFragment : DialogFragment() {
    private val viewModel: GastosViewModel by viewModels()
    private var _binding: DialogRegistrarGastoBinding? = null
    private val binding get() = _binding!!

    // Suponiendo que tienes un ViewModel para esto
    // private val viewModel: GastosViewModel by viewModels()

    // --- Definición de categorías y subcategorías ---
    private val categorias = mapOf(
        "Combustible" to listOf("Gasolina", "Gas", "Electricidad"),
        "Limpieza" to listOf("Lavada", "Detailing"),
        "Mantenimiento" to listOf("Cambio de Aceite", "Líquidos", "Revisión Mecánica", "Llantas"),
        "Documentos" to listOf("Seguro", "Impuestos", "Revisión Técnica"),
        "Otros" to listOf("Accesorios", "Peajes", "Estacionamiento")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogRegistrarGastoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // Hacer que el diálogo ocupe un ancho razonable
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategorias()
        setupListeners()
    }

    private fun setupCategorias() {
        val categoriasAdapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item,
            categorias.keys.toList())
        binding.autoCompleteCategoria.setAdapter(categoriasAdapter)
    }

    private fun setupListeners() {
        // Listener para la CATEGORÍA PRINCIPAL (sin cambios)
        (binding.autoCompleteCategoria as? AutoCompleteTextView)?.setOnItemClickListener { parent, _, position, _ ->
            val categoriaSeleccionada = parent.getItemAtPosition(position) as String
            // Limpiamos los campos inferiores para evitar inconsistencias
            (binding.autoCompleteSubcategoria as? AutoCompleteTextView)?.text = null
            (binding.autoCompleteUnidadCombustible as? AutoCompleteTextView)?.text = null
            updateUIForCategoria(categoriaSeleccionada)
        }

        // ### NUEVO LISTENER PARA LA SUBCATEGORÍA ###
        // Este listener se activa cuando el usuario elige "Gasolina", "Gas", etc.
        (binding.autoCompleteSubcategoria as? AutoCompleteTextView)?.setOnItemClickListener { parent, _, position, _ ->
            val subcategoriaSeleccionada = parent.getItemAtPosition(position) as String
            // Comprobamos si la subcategoría es un tipo de combustible
            if (unidadesPorCombustible.containsKey(subcategoriaSeleccionada)) {
                updateUnidadesParaCombustible(subcategoriaSeleccionada)
            }
        }

        binding.buttonCancelar.setOnClickListener {
            dismiss()
        }

        binding.buttonGuardar.setOnClickListener {
            Log.d(TAG, "Botón 'Guardar' presionado.")
            // Recoger todos los datos de los campos
            val categoria = binding.autoCompleteCategoria.text.toString()
            val subcategoria = binding.autoCompleteSubcategoria.text.toString()
            val costoTotalStr = binding.editTextCostoTotal.text.toString()
            val cantidadStr = binding.editTextCantidadCombustible.text.toString()
            val unidad = binding.autoCompleteUnidadCombustible.text.toString()
            val descripcion = binding.editTextDescripcion.text.toString()

            // Validación simple
            if (categoria.isEmpty() || costoTotalStr.isEmpty()) {
                Log.e(TAG, "Validación fallida: Categoría o Costo están vacíos.")
                return@setOnClickListener
            }
            val costoTotal = costoTotalStr.toDoubleOrNull() ?: 0.0

            Log.d(TAG, "Datos a guardar -> Categoría: $categoria, Costo: $costoTotal") // <-- LOG 3

            // Llamar al ViewModel
            viewModel.guardarGasto(
                categoria = categoria,
                subcategoria = binding.autoCompleteSubcategoria.text.toString().ifEmpty { null },
                costoTotal = costoTotal,
                descripcion = binding.editTextDescripcion.text.toString().ifEmpty { null },
                cantidadCombustible = binding.editTextCantidadCombustible.text.toString().toDoubleOrNull(),
                unidadCombustible = binding.autoCompleteUnidadCombustible.text.toString().ifEmpty { null }
            )

            Log.d(TAG, "Llamada a viewModel.guardarGasto() realizada.") // <-- LOG 4
            dismiss()
        }

    }

    private fun updateUIForCategoria(categoria: String) {
        val esCombustible = (categoria == "Combustible")
        binding.layoutCombustibleFields.isVisible = esCombustible
        val subcategorias = categorias[categoria]

//        // Mostrar u ocultar campos de combustible
//        binding.layoutCombustibleFields.isVisible = (categoria == "Combustible")
        if (subcategorias != null && subcategorias.isNotEmpty()) {
            binding.layoutSubcategoria.isVisible = true
            val subcategoriasAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, subcategorias)
            (binding.autoCompleteSubcategoria as? AutoCompleteTextView)?.setAdapter(subcategoriasAdapter)
        } else {
            binding.layoutSubcategoria.isVisible = false
        }
    }
    private val unidadesPorCombustible = mapOf(
        "Gasolina" to listOf("Litros", "Galones"),
        "Gas" to listOf("Metros cúbicos (m³)", "Litros"),
        "Electricidad" to listOf("Kilovatios-hora (kWh)")
    )
    private fun updateUnidadesParaCombustible(tipoCombustible: String) {
        // Obtenemos la lista de unidades correcta de nuestro nuevo mapa
        val unidades = unidadesPorCombustible[tipoCombustible] ?: emptyList()

        val unidadesAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            unidades
        )
        (binding.autoCompleteUnidadCombustible as? AutoCompleteTextView)?.setAdapter(unidadesAdapter)

        // Opcional: Seleccionar automáticamente la primera unidad de la lista
        if (unidades.isNotEmpty()) {
            (binding.autoCompleteUnidadCombustible as? AutoCompleteTextView)?.setText(unidades[0], false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RegistrarGastoDialog"
    }
}