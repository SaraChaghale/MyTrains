package com.sachna.mytrains.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sachna.mytrains.activities.Conexiones
import com.sachna.mytrains.models.Ejercicio
import com.sachna.mytrains.adapters.StatsExerciseAdapter
import com.sachna.mytrains.bdroom.bd.AppDatabase
import com.sachna.mytrains.bdroom.entity.WorkoutEntity
import com.sachna.mytrains.databinding.FragmentStatsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val conexiones = Conexiones()
    private var listaMaestra = listOf<Ejercicio>()
    private var sesionActual = 1 // Estado de la vista actual

    // NUEVO: Referencia a la BD de Room
    private lateinit var database: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        setupListeners()
        cargarDatos()
    }

    private fun setupListeners() {
        binding.prevArrow.setOnClickListener {
            if (sesionActual > 1) {
                sesionActual--
                actualizarInterfaz()
            }
        }
        binding.nextArrow.setOnClickListener {
            if (sesionActual < 3) {
                sesionActual++
                actualizarInterfaz()
            }
        }
    }

    private fun cargarDatos() {
        val prefs = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val uid = prefs.getString("user_uid", "") ?: ""
        if (uid.isEmpty()) {
            android.util.Log.e("DEBUG_STATS", "¡EL UID ESTÁ VACÍO!")
            return
        }
        // Usamos la nueva función que trae TODO (completados y no completados)
        conexiones.obtenerEjerciciosParaStats(uid) { lista ->
            listaMaestra = lista
            if (listaMaestra.isNotEmpty()) {
                // La primera vez, detectamos en qué sesión se quedó
                detectarSesionPorDefecto()
                actualizarInterfaz()
            }



        }
    }

    private fun detectarSesionPorDefecto() {
        val s1Hecha = listaMaestra.filter { it.nombreSesion == "sesion1" }.all { it.completado }
        val s2Hecha = listaMaestra.filter { it.nombreSesion == "sesion2" }.all { it.completado }

        sesionActual = when {
            !s1Hecha -> 1
            !s2Hecha -> 2
            else -> 3
        }
    }

    private fun actualizarInterfaz() {
        val idFiltro = "sesion$sesionActual"
        val listaFiltrada = listaMaestra.filter { it.nombreSesion == idFiltro }

        // Lógica de bloqueo: No puedes hacer la 2 si la 1 no está terminada
        val bloqueada = when (sesionActual) {
            2 -> !listaMaestra.filter { it.nombreSesion == "sesion1" }.all { it.completado }
            3 -> !listaMaestra.filter { it.nombreSesion == "sesion2" }.all { it.completado }
            else -> false
        }

        // UI de cabecera
        binding.tvLabelSemana.text = "SESIÓN $sesionActual"
        binding.prevArrow.alpha = if (sesionActual == 1) 0.3f else 1.0f
        binding.nextArrow.alpha = if (sesionActual == 3) 0.3f else 1.0f

        // ProgressBar
        val total = listaFiltrada.size
        val hechos = listaFiltrada.count { it.completado }
        val porcentaje = if (total > 0) (hechos * 100) / total else 0
        binding.pbSesionProgreso.progress = porcentaje
        binding.tvPorcentajeProgreso.text = "$porcentaje%"

        setupRecyclerView(listaFiltrada, bloqueada)
    }

    private fun setupRecyclerView(lista: List<Ejercicio>, bloqueada: Boolean) {
        val prefs = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val uid = prefs.getString("user_uid", "") ?: ""
       // val semanaId = "semana_1" // Esto puedes hacerlo dinámico luego

        binding.rvExerciseStats.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExerciseStats.adapter = StatsExerciseAdapter(lista, bloqueada) { ejercicio, repsR, repsP, peso, notas ->
            val fechaDate = java.util.Date()
            val momentoClick = com.google.firebase.Timestamp(java.util.Date())

            val rutaPartes = ejercicio.parentPath.split("/")
            val semanaIdActual = rutaPartes.find { it.contains("semana") } ?: "semana_1"

            val numSemanaReal = try {
                semanaIdActual.filter { it.isDigit() }.toInt()
            } catch (e: Exception) { 1 }

            val workoutLocal = WorkoutEntity(
                firebaseId = ejercicio.id, // Primary Key para que sobreescriba si editas
                exerciseName = ejercicio.nombre,
                coachComment = ejercicio.comentario_entrenador,
                targetSeries = ejercicio.series.toString(),
                targetReps = ejercicio.rangoReps.toString(),
                targetRIR = ejercicio.rir.toString(),
                targetRest = ejercicio.rest.toString(),
                weight = peso.toString(),
                grupo_muscular = ejercicio.grupo_muscular,
                repRealizada = repsR.toString(),
                repPosible = repsP.toString(),
                userNotes = notas,
                nomSesion = ejercicio.nombreSesion,
                numSemana = numSemanaReal,
                date = fechaDate,
                isCompleted = true,
                imageRes = 0
            )
            lifecycleScope.launch(Dispatchers.IO) {
                // A. Guardar en Room (Local)
                database.workoutDao().insertWorkout(workoutLocal)

            // 1. Guardar todos los datos técnicos + FECHA
            val updateData = hashMapOf(
                "repsRealizadas" to repsR,
                "repsPosibles" to repsP,
                "pesoRealizado" to peso,
                "comentario_usuario" to notas,
                "completado" to true,
                "fecha_realizado" to momentoClick
            )
                // B. Guardar en Firebase (Remoto)
            FirebaseFirestore.getInstance()
                .collection("usuarios").document(uid)
                .collection("entrenamientos").document(semanaIdActual)
                .collection("sesiones").document(ejercicio.nombreSesion)
                .collection("ejercicios").document(ejercicio.id)
                .update(updateData as Map<String, Any>)
                .addOnSuccessListener {
                    // 2. Disparar revisión de niveles (Sesión -> Semana)
                    conexiones.actualizarEjercicioYSubir(uid, semanaIdActual, ejercicio.nombreSesion, ejercicio.id, true)
// Volvemos al hilo principal para la UI
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "¡Sincronizado en local y nube!", Toast.LENGTH_SHORT).show()
                        cargarDatos()
                    }
                }
                .addOnFailureListener { e ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Error nube: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}