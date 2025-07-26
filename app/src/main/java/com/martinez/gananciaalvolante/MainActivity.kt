package com.martinez.gananciaalvolante

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.martinez.gananciaalvolante.databinding.ActivityMainBinding
import com.martinez.gananciaalvolante.di.util.MenuPosition
import com.martinez.gananciaalvolante.di.util.MenuPositionPreference
import com.martinez.gananciaalvolante.ui.gastos.RegistrarGastoDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.cos
import kotlin.math.sin

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var menuPositionPreference: MenuPositionPreference
    private var currentMenuPosition = MenuPosition.IZQUIERDA

    private var isMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        menuPositionPreference = MenuPositionPreference(this)
        menuPositionPreference.position.observe(this) { newPosition ->
            currentMenuPosition = newPosition
            updateFabPosition() // Función para mover el botón físicamente
        }
        // Obtener el NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Listener para el FAB global
        binding.fabGlobalAdd.setOnClickListener {
            toggleMenu()
        }
        binding.scrimView.setOnClickListener {
            toggleMenu()
        }
        binding.fabHome.setOnClickListener {
            toggleMenu() // Cierra el menú
            navController.navigate(R.id.action_global_to_dashboardFragment)
        }
        binding.fabHistory.setOnClickListener {
            toggleMenu() // Cierra el menú
            navController.navigate(R.id.action_global_to_historialFragment)
        }
        binding.fabAddGanancia1.setOnClickListener {
            toggleMenu() // Cierra el menú
            navController.navigate(R.id.action_global_to_vehiculoFragment)
        }

        binding.fabAddGasto.setOnClickListener {
            toggleMenu() // Cierra el menú
            RegistrarGastoDialogFragment().show(supportFragmentManager, RegistrarGastoDialogFragment.TAG)
        }
        binding.fabAddGanancia3.setOnClickListener {
            toggleMenu() // Cierra el menú
            navController.navigate(R.id.action_global_to_vehiculoFragment)
        }
        binding.fabAddGanancia4.setOnClickListener {
            toggleMenu() // Cierra el menú
            navController.navigate(R.id.action_global_to_vehiculoFragment)
        }
        binding.fabSettings.setOnClickListener {
            toggleMenu() // Cierra el menú
            navController.navigate(R.id.action_global_to_settingsFragment)
        }

    }

    private fun updateFabPosition() {
        val params = binding.fabGlobalAdd.layoutParams as CoordinatorLayout.LayoutParams
        if (currentMenuPosition == MenuPosition.DERECHA) {
            params.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        } else {
            params.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        }
        binding.fabGlobalAdd.layoutParams = params
    }

    private fun toggleMenu() {
        val open = !isMenuOpen
        isMenuOpen = open
        if (open) {
            binding.fabGlobalAdd.setImageResource(R.drawable.ic_add) // Icono de 'X' para cerrar
        } else {
            binding.fabGlobalAdd.setImageResource(R.drawable.ic_menu) // Icono de menú
        }

        // Animar el botón principal (rotación)
        binding.fabGlobalAdd.animate().rotation(if (open) 45f else 0f).setDuration(300).start()

        // Animar el fondo oscuro (aparecer/desaparecer)
        binding.scrimView.visibility = View.VISIBLE
        binding.scrimView.animate().alpha(if (open) 1f else 0f).setDuration(300).withEndAction {
            if (!open) {
                binding.scrimView.visibility = View.GONE
            }
        }.start()

        // Animar los ítems del menú
        val menuItems = listOf(
            binding.fabHome,
            binding.fabHistory,
            binding.fabAddGanancia1,
            binding.fabAddGasto,
            binding.fabAddGanancia3,
            binding.fabAddGanancia4,
            binding.fabSettings
        )

        // Los ángulos y el radio ahora dependen de la posición actual
        val startAngleRad: Double
        val endAngleRad: Double
        val radius = 280f

        if (currentMenuPosition == MenuPosition.DERECHA) {
            // Arco para el lado derecho: de -90° (arriba) a -270° (abajo, pasando por la izquierda)
            startAngleRad = Math.toRadians(-90.0)
            endAngleRad = Math.toRadians(-270.0)
        } else {
            // Arco para el lado izquierdo (el que ya teníamos)
            startAngleRad = Math.toRadians(-90.0)
            endAngleRad = Math.toRadians(90.0)
        }

        for (i in menuItems.indices) {
            // El resto de la lógica del bucle es EXACTAMENTE LA MISMA
            val angle = if (menuItems.size > 1) {
                startAngleRad + i * (endAngleRad - startAngleRad) / (menuItems.size - 1)
            } else {
                (startAngleRad + endAngleRad) / 2 // Centro del arco
            }

            val translationX = radius * cos(angle).toFloat()
            val translationY = radius * sin(angle).toFloat()

            val fab = menuItems[i]

            // El resto de la lógica de animación es idéntica
            if (open) {
                fab.visibility = View.VISIBLE
                fab.animate()
                    .translationX(translationX)
                    .translationY(translationY)
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            } else {
                fab.animate()
                    .translationX(0f)
                    .translationY(0f)
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        fab.visibility = View.INVISIBLE
                    }
                    .start()
            }
        }
    }


}