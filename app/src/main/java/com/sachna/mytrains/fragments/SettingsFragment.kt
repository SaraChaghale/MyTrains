package com.sachna.mytrains.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.sachna.mytrains.R
import com.sachna.mytrains.activities.MainActivity
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Cargar el archivo XML de preferencias
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = listView
        val params = recyclerView.layoutParams as ViewGroup.MarginLayoutParams
       // params.topMargin =
         //   resources.getDimensionPixelSize(R.dimen.margin_top) // Define el margen superior
      //  recyclerView.layoutParams = params

        recyclerView.setBackgroundResource(R.drawable.ejercicio)
        val languagePreference = findPreference<ListPreference>("language")

        languagePreference?.setOnPreferenceChangeListener { _, newValue ->
            val selectedLanguage = newValue as String
            changeAppLanguage(selectedLanguage) // Cambiar el idioma
            true
        }


        // Inicializar el summary con el valor actual


    } private fun changeAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        // Configura la configuración de la aplicación
        val config = resources.configuration
        config.setLocale(locale)

        // Aplica la nueva configuración de idioma
        resources.updateConfiguration(config, resources.displayMetrics)

        // Guarda el idioma seleccionado en SharedPreferences
        val preferences = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        val editor = preferences?.edit()
        editor?.putString("language", languageCode)
        editor?.apply()

        // Re-crea la actividad para aplicar los cambios sin reiniciar la app
        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
        activity?.finish() // Cierra la actividad actual
    }}
