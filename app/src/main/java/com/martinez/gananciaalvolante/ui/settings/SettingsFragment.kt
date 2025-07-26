package com.martinez.gananciaalvolante.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import com.martinez.gananciaalvolante.R
import androidx.preference.PreferenceFragmentCompat
import com.martinez.gananciaalvolante.BuildConfig
import com.martinez.gananciaalvolante.di.util.GananciaAlVolanteApp
import com.martinez.gananciaalvolante.ui.settings.vehiculo.EditVehiculoDialogFragment

class SettingsFragment : PreferenceFragmentCompat() , SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val appVersionPreference: Preference? = findPreference("app_version")
        appVersionPreference?.summary = BuildConfig.VERSION_NAME

        findPreference<Preference>("terms_conditions")?.setOnPreferenceClickListener {
            // Abrir una URL en el navegador
            openUrl("https://tu-sitio-web.com/terminos")
            true // Indica que el clic ha sido manejado
        }
        findPreference<Preference>("privacy_policy")?.setOnPreferenceClickListener {
            openUrl("https://tu-sitio-web.com/privacidad")
            true
        }
        findPreference<Preference>("contact_us")?.setOnPreferenceClickListener {
            // Abrir el cliente de correo electrónico
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // Solo apps de email
                putExtra(Intent.EXTRA_EMAIL, arrayOf("soporte@tuapp.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Sugerencia para Ganancia al Volante v${BuildConfig.VERSION_NAME}")
            }
            startActivity(Intent.createChooser(intent, "Enviar correo..."))
            true
        }
        findPreference<Preference>("rate_app")?.setOnPreferenceClickListener {
            // Abrir la página de la app en la Play Store
            openUrl("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
            true
        }
        findPreference<Preference>("edit_vehicle")?.setOnPreferenceClickListener {
            EditVehiculoDialogFragment().show(parentFragmentManager, EditVehiculoDialogFragment.TAG)
            true // Indicar que el clic fue manejado
        }
    }
    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        // Registramos el listener cuando el fragment es visible
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        // Quitamos el registro cuando el fragment ya no es visible para evitar memory leaks
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    // Este método se llamará automáticamente cada vez que una preferencia cambie
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "theme_preference") {
            val themeValue = sharedPreferences?.getString(key, "system")
            // Llamamos a nuestra función estática para aplicar el cambio inmediatamente
            GananciaAlVolanteApp.applyTheme(themeValue)
        }
    }
}