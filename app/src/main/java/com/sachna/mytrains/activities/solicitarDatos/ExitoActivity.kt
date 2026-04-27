package com.sachna.mytrains.activities.solicitarDatos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sachna.mytrains.activities.BienvenidaActivity
import com.sachna.mytrains.activities.MainActivity
import com.sachna.mytrains.activities.login.LoginActivity

import com.sachna.mytrains.databinding.ActivitySolicitar3Binding

class ExitoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitar3Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitar3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackHome.setOnClickListener {
            // Volvemos a la MainActivity (o Login) limpiando el historial
            val intent = Intent(this, BienvenidaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}