package com.sachna.mytrains.activities.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.sachna.mytrains.activities.Conexiones
import com.sachna.mytrains.activities.MainActivity

import com.sachna.mytrains.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

        private lateinit var binding: ActivityLoginBinding
        private val conex = Conexiones()
        private val db = FirebaseFirestore.getInstance()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

            binding.btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            binding.btnLogin.setOnClickListener {
                val email = binding.etUser.text.toString().trim()
                val password = binding.etPassword.text.toString()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Por favor, introduce tus credenciales", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Llamamos a tu clase de conexiones
                conex.login(email, password) { exito, uid ->
                    if (exito && uid != null) {
                        // --- PASO 1: BUSCAMOS EL NOMBRE EN FIRESTORE ---
                        db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener { document ->
                                val nombre = document.getString("nombre") ?: "Usuario"

                                // --- PASO 2: GUARDAMOS TODO EN PREFS ---
                                prefs.edit().apply {
                                    putBoolean("loggedIn", true)
                                    putString("user_uid", uid)
                                    putString("user_name", nombre)
                                    apply()
                                }

                                // --- PASO 3: FEEDBACK Y SALTO ---
                                Toast.makeText(this, "Acceso concedido, ¡hola $nombre!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finishAffinity()
                            }
                            .addOnFailureListener {
                                // Si falla Firestore, entramos igual pero con nombre genérico
                                Toast.makeText(this, "Error al recuperar datos, entrando...", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finishAffinity()
                            }
                    } else {
                        Toast.makeText(this, "Acceso denegado: Revisa email o contraseña", Toast.LENGTH_LONG).show()
                    }
                } // Aquí cierra el bloque de conex.login
            } // Aquí cierra el bloque del btnLogin
        } // Aquí cierra el onCreate

        override fun finish() {
            super.finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
//        binding.btnLogin.setOnClickListener {
//            val user = binding.etUser.text.toString().trim()
//            val password = binding.etPassword.text.toString()
//
//            if (user.isEmpty() || password.isEmpty()) {
//                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            // Simulación de credenciales (Admin)
//            if (user == "admin" && password == "123") {
//                prefs.edit().apply {
//                    putBoolean("loggedIn", true)
//                    putString("user_nif", user)
//                    apply()
//                }
//
//                Toast.makeText(this, "¡Bienvenido, $user!", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this, MainActivity::class.java))
//                finishAffinity() // Limpia el stack para entrar a la App
//            } else {
//                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
//            }
//        }
   // }

