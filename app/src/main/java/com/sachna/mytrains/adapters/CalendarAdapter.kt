package com.sachna.mytrains.adapters

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sachna.mytrains.R
import com.sachna.mytrains.databinding.ItemCalendarDayBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarAdapter(
    private val items: List<CalendarDay>,
    private val onDateSelected: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = items[position]
        val b = holder.binding

        // tvDayLetter: Usamos el nombre del día (L, M, X...) o "SEM"
        val formatter = DateTimeFormatter.ofPattern("E", Locale("es", "ES"))
        b.tvDayLetter.text = if (day.isSemanaModo) "SEM" else day.date.format(formatter).first().toString().uppercase()

        // tvDayNumber: El número del día o el número de la semana
        b.tvDayNumber.text = if (day.isSemanaModo) day.semanaNumero.toString() else day.date.dayOfMonth.toString()

        // Cambiar color si está seleccionado (Captura 2)
        if (day.isSelected) {
            b.tvDayNumber.setBackgroundResource(R.drawable.bg_date_selected) // El círculo naranja
            b.tvDayNumber.setTextColor(Color.WHITE)
        } else {
            b.tvDayNumber.setBackgroundResource(R.drawable.bg_date_unselected)
            b.tvDayNumber.setTextColor(Color.parseColor("#80FFFFFF"))
        }

        b.root.setOnClickListener { onDateSelected(day.date) }
    }

    override fun getItemCount() = items.size
}

// Clase de apoyo
data class CalendarDay(
    val date: LocalDate,
    val isSelected: Boolean,
    val isSemanaModo: Boolean = false,
    val semanaNumero: Int = 0
)