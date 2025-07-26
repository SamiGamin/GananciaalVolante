package com.martinez.gananciaalvolante.ui.dashboard

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.martinez.gananciaalvolante.R
import com.martinez.gananciaalvolante.databinding.FragmentDashboardBinding
import com.martinez.gananciaalvolante.di.util.DistanceUnit
import com.martinez.gananciaalvolante.di.util.Formatters
import com.martinez.gananciaalvolante.ui.turnos.FinalizarTurnoDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private val viewModel: DashboardViewModel by activityViewModels()
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private var mainActivityFab: FloatingActionButton? = null

    companion object {
        fun newInstance() = DashboardFragment()
    }

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val postNotificationsGranted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
                } else {
                    true
                }

            if (fineLocationGranted && postNotificationsGranted) {
                checkBackgroundLocationPermission()
            } else {
                Snackbar.make(
                    binding.root,
                    "Los permisos de ubicación y notificación son necesarios.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    private val requestBackgroundLocationLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.onStartStopTrip(requireContext())
            } else {
                Snackbar.make(
                    binding.root,
                    "El permiso de ubicación en segundo plano es necesario para un seguimiento fiable.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivityFab = activity?.findViewById(R.id.fab_global_add)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        setupClickListeners()
        observeState()
        observeUiEvents()
    }

    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "keep_screen_on") {
            updateKeepScreenOn()
        }
        if (key == "time_format") {
            updateTimeFormat()
        }
    }

    private fun updateKeepScreenOn() {
        val keepOn = sharedPreferences.getBoolean("keep_screen_on", true)
        activity?.window?.let { window ->
            if (keepOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    private fun updateModoTrabajoUI(isActive: Boolean) {
        binding.switchModoTrabajo.isChecked = isActive
        binding.switchModoTrabajo.text = if (isActive) "Finalizar Turno" else "Modo Trabajo"

        binding.cardTurnoActivoBanner.visibility = if (isActive) View.VISIBLE else View.GONE

        val fabColorRes =
            if (isActive) R.color.md_theme_outlineVariant_mediumContrast else R.color.md_theme_inversePrimary
        val fabColor = ContextCompat.getColor(requireContext(), fabColorRes)
        mainActivityFab?.backgroundTintList = ColorStateList.valueOf(fabColor)
    }

    private fun updateTimeFormat() {
        when (sharedPreferences.getString("time_format", "system")) {
            "12h" -> binding.textClockCurrentTime.format12Hour = "hh:mm:ss a"
            "24h" -> binding.textClockCurrentTime.format12Hour = "HH:mm:ss"
            else -> {
                binding.textClockCurrentTime.format12Hour = null
                binding.textClockCurrentTime.format24Hour = null
            }
        }
    }

    private fun setupClickListeners() {
        binding.switchModoTrabajo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != viewModel.isModoTrabajo.value) {
                viewModel.onModoTrabajoChanged(isChecked)
            }
        }
        binding.velocimetroView.setOnClickListener {
            if (viewModel.isTracking.value) {
                viewModel.onStartStopTrip(requireContext())
            } else {
                checkPermissionsAndStart()
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isTracking.collect { updateAllUI() }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentSpeed.collect { updateAllUI() }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeTripData.collect { updateAllUI() }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estadisticasDiarias.collect { updateAllUI() }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isModoTrabajo.collect { isActive ->
                    Log.d("Turno_DEBUG", "Fragment del Dashboard: isModoTrabajo ha cambiado a: $isActive. Actualizando UI.")
                    updateModoTrabajoUI(isActive)
                }
            }
        }
        viewModel.currentUnit.observe(viewLifecycleOwner) { updateAllUI() }
    }

    private fun observeUiEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        is DashboardUIEvent.ShowFinalizarTurnoDialog -> {
                            FinalizarTurnoDialogFragment().show(
                                parentFragmentManager,
                                FinalizarTurnoDialogFragment.TAG
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateAllUI() {
        if (_binding == null) return

        val isTracking = viewModel.isTracking.value
        val currentSpeed = viewModel.currentSpeed.value
        val tripData = viewModel.activeTripData.value
        val statsDiarias = viewModel.estadisticasDiarias.value
        val currentUnit = viewModel.currentUnit.value ?: DistanceUnit.KM

        binding.velocimetroView.setRecordingState(isTracking)
        binding.velocimetroView.setVelocidad(currentSpeed)
        binding.velocimetroView.setDistanceUnit(currentUnit)

        binding.layoutActiveTripStats.visibility = if (isTracking) View.VISIBLE else View.GONE
        if (isTracking) {
            val (distanciaStr, unidadStr) = formatDistance(
                tripData?.distanceInMeters ?: 0f,
                currentUnit
            )
            binding.textViewActiveTripTime.text =
                Formatters.getFormattedStopWatchTime(tripData?.timeInMillis ?: 0L)
            binding.textViewActiveTripDistance.text = "$distanciaStr $unidadStr"
        }

        val distanciaTotalKm = statsDiarias.distanciaTotalKm
        if (distanciaTotalKm > 0) {
            binding.layoutTotalTripStats.visibility = View.VISIBLE
            val (distanciaStr, unidadStr) = formatDistance(
                (distanciaTotalKm * 1000).toFloat(),
                currentUnit
            )
            val (tiempoStr, unidadTiempoStr) = Formatters.getFormattedTimeWithUnit(statsDiarias.tiempoTotalMs)

            binding.textViewTotalDistanceValue.text = "$distanciaStr $unidadStr"
            binding.textViewTotalTimeValue.text = tiempoStr
            binding.textViewTotalTimeDayUnit.text = unidadTiempoStr
        } else {
            binding.layoutTotalTripStats.visibility = View.GONE
        }
        // La llamada a updateModoTrabajoUI se elimina de aquí,
        // ya que tiene su propio collector dedicado en observeState
    }

    private fun formatDistance(distanceInMeters: Float, unit: DistanceUnit): Pair<String, String> {
        val unitLabel = if (unit == DistanceUnit.MI) "mi" else "km"
        val conversionFactor = if (unit == DistanceUnit.MI) 0.000621371 else 0.001
        val convertedDistance = distanceInMeters * conversionFactor.toFloat()
        return String.format("%.1f", convertedDistance) to unitLabel
    }

    private fun checkPermissionsAndStart() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            checkBackgroundLocationPermission()
        }
    }

    private fun checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBackgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                viewModel.onStartStopTrip(requireContext())
            }
        } else {
            viewModel.onStartStopTrip(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        updateKeepScreenOn()
        updateTimeFormat()
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
