package com.sachna.mytrains

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sachna.mytrains.activities.login.LoginActivity
import com.sachna.mytrains.activities.solicitarDatos.SolicitarActivity
import com.sachna.mytrains.databinding.ActivityBienvenidaBinding


class BienvenidaActivity
//    : AppCompatActivity()
   {

//    private lateinit var binding: ActivityBienvenidaBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // 1. Comprobar sesión antes de cargar la vista
//        val prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
//        if (prefs.getBoolean("loggedIn", false)) {
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//            return
//        }
//
//        // 2. Inicializar Vista
//        binding = ActivityBienvenidaBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // 3. Listeners
//        binding.btnLogin.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//        }
//
//        binding.btnRequestPlan.setOnClickListener {
//            val intent = Intent(this, SolicitarActivity::class.java)
//            startActivity(intent)
//            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//        }
//    }
}