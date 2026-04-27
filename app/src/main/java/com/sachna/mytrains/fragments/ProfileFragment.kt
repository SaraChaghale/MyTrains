package com.sachna.mytrains.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import  com.sachna.mytrains.bdroom.bd.AppDatabase
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.sachna.mytrains.R
import com.sachna.mytrains.activities.login.LoginActivity
import com.sachna.mytrains.adapters.NotificationAdapter
import com.sachna.mytrains.bdroom.entity.WorkoutEntity
import com.sachna.mytrains.databinding.FragmentProfileBinding
import com.sachna.mytrains.models.Ejercicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val database by lazy { AppDatabase.getDatabase(requireContext()) }

    // Referencia a la base de datos
   // private val database by lazy { AppDatabase.getDatabase(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configurar Datos de Usuario
        setupUserData()

        // 2. Cargar Entrenamientos desde Room
        setupCompletedTrainings()

        // 3. Lógica del Menú Lateral e Interacciones
        setupNavigationDrawer()
    }

    private fun setupUserData() {
        val prefs = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val nombreRecuperado = prefs.getString("user_name", "Usuario")
        binding.tvUserName.text = nombreRecuperado
        binding.ivUser.setImageResource(R.drawable.exercise)
    }

    private fun setupCompletedTrainings() {
        binding.rvCompletedTrainings.layoutManager = LinearLayoutManager(requireContext())
        val prefs = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val uid = prefs.getString("user_uid", "") ?: ""

        viewLifecycleOwner.lifecycleScope.launch {
            val db = FirebaseFirestore.getInstance()

            // 1. OBTENER TODO DE FIREBASE (Para asegurar que no falte nada)
            // Buscamos en la colección de ejercicios donde 'completado' sea true
            db.collectionGroup("ejercicios") // Busca en todas las subcolecciones 'ejercicios'
                .whereEqualTo("completado", true)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    android.util.Log.d("FIREBASE_DEBUG", "Consulta exitosa: ${querySnapshot.size()} ejercicios")
                    val listaEjercicios = mutableListOf<Ejercicio>()

                    for (doc in querySnapshot.documents) {
                        val entity = docToWorkoutEntity(doc) // Convertimos el doc de Firebase a Entity

                        // Guardamos en Room en segundo plano para "rellenar" el hueco local
                        lifecycleScope.launch(Dispatchers.IO) {
                            database.workoutDao().insertWorkout(entity)
                        }

                        listaEjercicios.add(entityToEjercicio(entity))
                    }

                    // 2. ACTUALIZAR CONTADORES (Basado en los documentos reales de semanas)
                    actualizarContadoresDesdeFirebase(uid)

                    // 3. MOSTRAR EN EL ADAPTER
                    // Ordenamos por fecha descendente (lo más nuevo arriba)
                    val listaOrdenada = listaEjercicios.sortedByDescending { it.fecha }
                    binding.rvCompletedTrainings.adapter = NotificationAdapter(listaOrdenada, esHistorial = true)
                }
                .addOnFailureListener { e ->
                    // ESTO ES LO QUE BUSCAMOS
                    android.util.Log.e("FIREBASE_DEBUG", "Fallo en la consulta: ${e.message}")

                    // Si el error contiene el enlace, lo imprimimos solo (más fácil de copiar)
                    if (e.message?.contains("https://") == true) {
                        val link = e.message?.substringAfter("You can create it here: ")
                        android.util.Log.e("FIREBASE_LINK", "HAZ CLIC AQUÍ PARA EL ÍNDICE: $link")

                        // Opcional: Mostrar un Toast para avisarte
                        Toast.makeText(context, "Error de índice. Revisa el Logcat.", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
    private fun docToWorkoutEntity(doc: com.google.firebase.firestore.DocumentSnapshot): WorkoutEntity {
        val fechaTimestamp = doc.getTimestamp("fecha_realizado") ?: com.google.firebase.Timestamp.now()
        val path = doc.reference.path // usuarios/UID/entrenamientos/semana_2/sesiones/sesion1/...

        // Buscamos el número después de "semana_"
        val numSemana = try {
            path.substringAfter("semana_").substringBefore("/").toInt()
        } catch (e: Exception) { 1 }

        // Buscamos el nombre de la sesión en la ruta
        val nombreSesionReal = try {
            path.substringAfter("sesiones/").substringBefore("/")
        } catch (e: Exception) { doc.getString("nombreSesion") ?: "sesion1" }

        return WorkoutEntity(
            firebaseId = doc.id,
            exerciseName = doc.getString("nombre") ?: "",
            coachComment = doc.getString("comentario_entrenador") ?: "",
            targetSeries = doc.get("series").toString(),
            targetReps = doc.get("rangoReps").toString(),
            targetRIR = doc.get("rir").toString(),
            targetRest = doc.get("rest").toString(),
            weight = doc.get("pesoRealizado").toString(),
            repRealizada = doc.get("repsRealizadas").toString(),
            repPosible = doc.get("repsPosibles").toString(),
            userNotes = doc.getString("comentario_usuario") ?: "",

            nomSesion = nombreSesionReal,
            numSemana = numSemana, // Podrías extraerlo del parentPath si fuera necesario

            grupo_muscular = doc.getString("grupo_muscular") ?: "General",
            date = fechaTimestamp.toDate(),
            isCompleted = true,
            imageRes = 0
        )
    }
    private fun actualizarContadoresDesdeFirebase(uid: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(uid)
            .collection("entrenamientos")
            .get()
            .addOnSuccessListener { querySnapshot ->
                var semanasCompletas = 0
                var sesionesTotales = 0

                for (doc in querySnapshot.documents) {
                    // Si el documento de la semana tiene 'completada' = true
                    if (doc.getBoolean("completada") == true) {
                        semanasCompletas++
                    }
                    // Sumamos las sesiones completadas de esa semana
                    sesionesTotales += (doc.getLong("sesionesFinalizadas") ?: 0L).toInt()
                }

                binding.tvCountWeeks.text = semanasCompletas.toString()
                binding.tvCountSessions.text = sesionesTotales.toString()
            }
    }

    private fun entityToEjercicio(entity: WorkoutEntity): Ejercicio {
        val sem = "SEM ${entity.numSemana}"

        val soloNumeros = entity.nomSesion.filter { it.isDigit() }
        // Si por algún motivo no hay número, ponemos un "?" para no romper el diseño
        val sesFormateada = if (soloNumeros.isNotEmpty()) "S$soloNumeros" else "S1"
        android.util.Log.d("DEBUG_FORMATO", "Original: ${entity.nomSesion} -> Formateado: $sesFormateada")

        // Creamos un objeto Ejercicio y lo rellenamos con los datos de Room
        val ej = Ejercicio(
            id = entity.firebaseId,
            nombre = "${entity.exerciseName} ($sem-$sesFormateada)",
            nombreSesion = entity.nomSesion,
            grupo_muscular = entity.grupo_muscular,
            comentario_entrenador = entity.coachComment,
            series = entity.targetSeries.toIntOrNull() ?: 0,
            rangoReps = entity.targetReps.toIntOrNull() ?: 0,
            rir = entity.targetRIR.toIntOrNull() ?: 0,
            rest = entity.targetRest.toIntOrNull() ?: 0,
            // Datos del usuario (lo que completó)
            pesoRealizado = entity.weight.toDoubleOrNull() ?: 0.0,
            repsRealizadas = entity.repRealizada.toIntOrNull() ?: 0,
            completado = entity.isCompleted
        )
        // También puedes mapear el Timestamp si tu adapter usa la fecha:
        // ej.fecha_realizado = com.google.firebase.Timestamp(entity.date)
        ej.fecha = com.google.firebase.Timestamp(entity.date)
        return ej
    }

    private fun setupNavigationDrawer() {
        binding.ivMenuInstagram.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        val headerView = binding.navView.getHeaderView(0)
        val btnClose = headerView.findViewById<ImageView>(R.id.ivCloseMenu)
        btnClose.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> logoutUser()
                R.id.nav_settings -> {
//                    findNavController().navigate(R.id.nav_settings)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        binding.btnEdit.setOnClickListener { /* Editar */
//            val intent = Intent(requireContext(), EditarPerfil::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
        }
    }

    private fun logoutUser() {
        val prefs = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        prefs.edit { clear() }
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}