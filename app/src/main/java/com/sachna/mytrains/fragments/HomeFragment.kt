package com.sachna.mytrains.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.sachna.mytrains.activities.Conexiones
import com.sachna.mytrains.models.Ejercicio
import com.sachna.mytrains.adapters.NotificationAdapter
import com.sachna.mytrains.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Instanciamos nuestra nueva clase de lógica
    private val conexiones = Conexiones()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarEjerciciosDeLaSemana()
    }

    private fun cargarEjerciciosDeLaSemana() {
        // Recuperamos el UID de SharedPreferences
        val prefs = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val uid = prefs.getString("user_uid", "") ?: ""

        if (uid.isEmpty()) return

        // Mostramos un estado de carga si lo tienes (opcional)
        // binding.progressBar.visibility = View.VISIBLE

        // Usamos la nueva función de tu clase Conexiones
        conexiones.obtenerPendientesSemanaActual(uid) { ejercicios ->
            // Esta parte se ejecuta cuando Firebase responde
            actualizarUI(ejercicios)
        }
    }

    private fun actualizarUI(ejercicios: List<Ejercicio>) {
        if (_binding == null) return

        // binding.progressBar.visibility = View.GONE

        if (ejercicios.isEmpty()) {
            binding.tvNoWorkouts.visibility = View.VISIBLE
            binding.rvWorkouts.visibility = View.GONE
        } else {
            binding.tvNoWorkouts.visibility = View.GONE
            binding.rvWorkouts.visibility = View.VISIBLE
            binding.rvWorkouts.layoutManager = LinearLayoutManager(requireContext())

            // Configuramos el Adapter con la lista que nos llega de Conexiones
            binding.rvWorkouts.adapter = NotificationAdapter(ejercicios) { ejercicioClicado ->
                // Aquí irá la lógica para abrir el detalle del ejercicio
                println("Ejercicio seleccionado: ${ejercicioClicado.nombre}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}