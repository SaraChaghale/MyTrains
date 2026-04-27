package com.sachna.mytrains.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sachna.mytrains.R
import com.sachna.mytrains.models.Ejercicio // Asegúrate de importar tu data class
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val items: List<Ejercicio>,
    private val esHistorial: Boolean = false,
    private val onItemClick: ((Ejercicio) -> Unit)? = null
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvWorkoutTitle)
        val tvCoach: TextView = itemView.findViewById(R.id.tvWorkoutCoach)
        val tvTime: TextView = itemView.findViewById(R.id.tvWorkoutTime)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivWorkoutIcon)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(items[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ejercicio = items[position]


        if (esHistorial) {
            // En el Perfil: "Nombre (80.0kg x 10)"
            holder.tvTitle.text = "${ejercicio.nombre} - ${ejercicio.pesoRealizado}kg x ${ejercicio.repsRealizadas}"

            // 2. SUBTÍTULO: ¡El Grupo Muscular!
            holder.tvCoach.text = "Grupo: ${ejercicio.grupo_muscular}"
        } else {
            holder.tvTitle.text = ejercicio.nombre
            holder.tvCoach.text = "Ejercicio de ${ejercicio.grupo_muscular}"
        }

// 3. FECHA
        ejercicio.fecha?.let {
            val formato = if (esHistorial) "dd/MM/yyyy HH:mm" else "dd/MM/yyyy"
            val sdf = SimpleDateFormat(formato, Locale.getDefault())
            holder.tvTime.text = sdf.format(it.toDate())
        } ?: run {
            holder.tvTime.text = "Pendiente"
        }

        holder.ivIcon.setImageResource(R.drawable.exercise)
    }

    override fun getItemCount(): Int = items.size
}