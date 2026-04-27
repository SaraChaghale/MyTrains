package com.sachna.mytrains.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.sachna.mytrains.models.Ejercicio
import com.sachna.mytrains.databinding.ItemStatsExerciseBinding
import java.util.Date
import androidx.core.view.isVisible

class StatsExerciseAdapter(
    private var ejercicios: List<Ejercicio>,
    private val estaBloqueada: Boolean,
    private val onSaveClick: (Ejercicio, Int, Int, Double, String) -> Unit
) : RecyclerView.Adapter<StatsExerciseAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemStatsExerciseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStatsExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ej = ejercicios[position]
        val b = holder.binding

        // 1. ESTADO VISUAL (Check y Borde)
        b.tvStatExerciseName.text = ej.nombre
        if (ej.completado) {
            b.ivDoneBadge.visibility = View.VISIBLE
            b.cardExercise.strokeWidth = 6 // Activa borde verde (asegúrate de tener strokeColor en XML)
            b.btnCompleteTask.text = "EDITAR"
            b.btnCompleteTask.setBackgroundColor(android.graphics.Color.GRAY)
        } else {
            b.ivDoneBadge.visibility = View.GONE
            b.cardExercise.strokeWidth = 0
            b.btnCompleteTask.text = "GUARDAR"
            b.btnCompleteTask.setBackgroundColor(android.graphics.Color.parseColor("#EE6723"))
        }

        // 2. LÓGICA DE BLOQUEO
        if (estaBloqueada && !ej.completado) {
            b.btnCompleteTask.isEnabled = false
            b.btnCompleteTask.alpha = 0.5f
            b.btnCompleteTask.text = "SESIÓN BLOQUEADA"
        } else {
            b.btnCompleteTask.isEnabled = true
            b.btnCompleteTask.alpha = 1.0f
        }

        // 3. DATOS TÉCNICOS
        b.tvStatCoachComment.text = ej.comentario_entrenador
        b.tvTargetSeries.text = ej.series.toString()
        b.tvTargetReps.text = ej.rangoReps.toString()
        b.tvTargetRIR.text = ej.rir.toString()
        b.tvTargetRest.text = "${ej.rest}'"

        // 4. RELLENAR CAMPOS SI YA EXISTEN
        b.etWeight.setText(if(ej.pesoRealizado > 0) ej.pesoRealizado.toString() else "")
        b.etRepReal.setText(if(ej.repsRealizadas > 0) ej.repsRealizadas.toString() else "")
        b.etRepPos.setText(if(ej.repsPosibles > 0) ej.repsPosibles.toString() else "")
        b.etUserNotes.setText(ej.comentario_usuario)

        // 5. EXPANDIR / COLAPSAR
        b.layoutHeader.setOnClickListener {
            val expanded = b.layoutExpandable.isVisible
            b.layoutExpandable.visibility = if (expanded) View.GONE else View.VISIBLE
            b.ivExpandIcon.rotation = if (expanded) 0f else 180f
        }

        // 6. CLICK GUARDAR
        b.btnCompleteTask.setOnClickListener {
            val peso = b.etWeight.text.toString().toDoubleOrNull() ?: 0.0
            val rReal = b.etRepReal.text.toString().toIntOrNull() ?: 0
            val rPos = b.etRepPos.text.toString().toIntOrNull() ?: 0
            val notas = b.etUserNotes.text.toString()

            onSaveClick(ej, rReal, rPos, peso, notas)
        }
    }

    override fun getItemCount() = ejercicios.size
}