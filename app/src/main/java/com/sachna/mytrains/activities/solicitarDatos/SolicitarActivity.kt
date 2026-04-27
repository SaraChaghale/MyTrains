package com.sachna.mytrains.activities.solicitarDatos

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.sachna.mytrains.R
import com.sachna.mytrains.activities.BienvenidaActivity
import com.sachna.mytrains.activities.login.LoginActivity
import com.sachna.mytrains.databinding.ActivitySolicitarBinding

class SolicitarActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitarBinding
    private var planSeleccionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Estado inicial del botón
        binding.btnContinue.isEnabled = false
        binding.btnContinue.alpha = 0.5f

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, BienvenidaActivity::class.java))
        }

        setupPlanSelection()

        binding.btnContinue.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            intent.putExtra("PLAN_ELEGIDO", planSeleccionado)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun setupPlanSelection() {
        val opciones = listOf(
            binding.optionBienestar to "Bienestar Integral",
            binding.optionRehabilitacion to "Rehabilitación Activa",
            binding.optionPeso to "Control de Peso",
            binding.optionEspecializado to "Entrenamientos Especializados"
        )

        opciones.forEach { (layout, nombre) ->
            layout.setOnClickListener {
                seleccionarTarjeta(layout, nombre)
            }
        }
    }

    private fun seleccionarTarjeta(selectedLayout: LinearLayout, nombre: String) {
        val layouts = listOf(
            binding.optionBienestar, binding.optionRehabilitacion,
            binding.optionPeso, binding.optionEspecializado
        )

        // Resetear fondos
        layouts.forEach { it.setBackgroundResource(R.drawable.bg_input_glass) }

        // Marcar seleccionado (Asegúrate de tener este drawable con borde naranja)
        selectedLayout.setBackgroundResource(R.drawable.bg_input_selected)

        planSeleccionado = nombre
        binding.btnContinue.isEnabled = true
        binding.btnContinue.alpha = 1.0f
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}