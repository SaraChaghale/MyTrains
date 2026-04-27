package com.sachna.mytrains.activities.solicitarDatos

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.sachna.mytrains.R
import com.sachna.mytrains.databinding.ActivitySolicitar2Binding
import kotlinx.coroutines.launch

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitar2Binding
    private val db = Firebase.firestore
    private lateinit var credentialManager: CredentialManager
    private var planElegido: String = "General" // Variable global para la actividad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitar2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Forzamos que el CheckBox no sea clicable manualmente
        binding.cbTerms.isClickable = false
        binding.cbTerms.isFocusable = false

        credentialManager = CredentialManager.create(this)
        planElegido = intent.getStringExtra("PLAN_ELEGIDO") ?: "General"

        // Configuración de clics
        binding.tvViewTerms.setOnClickListener { mostrarBottomSheetTerminos() }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnGoogle.setOnClickListener { solicitarDatosGoogle() }

        setupValidation()

        binding.btnSubmit.setOnClickListener {
            val nombre = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val tlf = binding.etPhone.text.toString().trim()

            // Verificación extra de seguridad antes de disparar Firebase
            if (binding.cbTerms.isChecked && tlf.length >= 9) {
                binding.btnSubmit.isEnabled = false
                binding.btnSubmit.text = "PROCESANDO..."
                enviarAFirebase(nombre, email, tlf, planElegido)
            } else {
                Toast.makeText(this, "Completa el teléfono y acepta los términos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarBottomSheetTerminos(autoSend: Boolean = false) {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_terms, null)
        val btnAceptar = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCloseTerms)

        btnAceptar.setOnClickListener {
            binding.cbTerms.isChecked = true
            dialog.dismiss()
            validarFormulario()

            if (autoSend) {
                val nombre = binding.etName.text.toString()
                val email = binding.etEmail.text.toString()
                val tlf = binding.etPhone.text.toString()

                // Si al aceptar términos el formulario ya es válido, enviamos de una
                if (nombre.isNotEmpty() && email.isNotEmpty() && tlf.length >= 9) {
                    enviarAFirebase(nombre, email, tlf, planElegido)
                }
            }
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun solicitarDatosGoogle() {

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("157770662049-o5u01f9146ar2c79fm59sq501it6brvi.apps.googleusercontent.com")
                .build()

            val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(this@ContactActivity, request)
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

                    val nombreGoogle = googleIdTokenCredential.displayName
                    val emailGoogle = googleIdTokenCredential.id
                    val tlfGoogle = googleIdTokenCredential.phoneNumber

                    binding.etName.setText(nombreGoogle)
                    binding.etEmail.setText(emailGoogle)

                    if (!tlfGoogle.isNullOrEmpty()) {
                        binding.etPhone.setText(tlfGoogle)

                        if (binding.cbTerms.isChecked) {
                            // Si ya aceptó antes, enviamos directo
                            enviarAFirebase(nombreGoogle ?: "", emailGoogle, tlfGoogle, planElegido)
                        } else {
                            // SI TIENE TELÉFONO PERO NO TÉRMINOS: Abrimos el diálogo para que al aceptar se envíe solo
                            mostrarBottomSheetTerminos(autoSend = true)
                        }
                    } else {
                        // NO TIENE TELÉFONO: Flujo normal manual
                        Toast.makeText(this@ContactActivity, "Datos cargados. Por favor, añade tu teléfono.", Toast.LENGTH_LONG).show()
                        binding.etPhone.requestFocus()
                        validarFormulario()
                    }

                } catch (e: Exception) {
                    Log.e("GOOGLE_AUTH", "Error: ${e.localizedMessage}")
                }
            }
    }

    private fun setupValidation() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validarFormulario() }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etName.addTextChangedListener(watcher)
        binding.etPhone.addTextChangedListener(watcher)
        binding.etEmail.addTextChangedListener(watcher)
        // El checkbox ya llama a validarFormulario() desde el BottomSheet, pero lo dejamos por si acaso
        binding.cbTerms.setOnCheckedChangeListener { _, _ -> validarFormulario() }
    }

    private fun validarFormulario() {
        val nombre = binding.etName.text.toString().trim()
        val telefono = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val aceptoTerminos = binding.cbTerms.isChecked

        val esValido = nombre.isNotEmpty() &&
                telefono.length >= 9 &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                aceptoTerminos

        binding.btnSubmit.isEnabled = esValido
        binding.btnSubmit.alpha = if (esValido) 1.0f else 0.5f
    }

    private fun enviarAFirebase(nombre: String, email: String, tlf: String, plan: String) {
        val solicitud = hashMapOf(
            "nombre" to nombre,
            "correo" to email,
            "telefono" to tlf,
            "plan" to plan,
            "fecha" to Timestamp.now()
        )
        val emailData = hashMapOf(
            "to" to "sarichan34@gmail.com",
            "message" to hashMapOf(
                "subject" to "Nueva Solicitud Gains: $nombre",
                "text" to "El usuario $nombre ha solicitado el plan $plan.\nContacto: $tlf / $email"
            )
        )

        db.collection("solicitudes").add(solicitud)
            .addOnSuccessListener {
                db.collection("mail").add(emailData)
                    .addOnSuccessListener {
                        startActivity(Intent(this, ExitoActivity::class.java))
                        finishAffinity()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al conectar con la nube", Toast.LENGTH_SHORT).show()
                binding.btnSubmit.isEnabled = true
                binding.btnSubmit.text = "ENVIAR SOLICITUD"
            }
    }
}