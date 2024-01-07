package com.example.heartsync.Extras

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboard.pantallas.MainActivity
import com.example.heartsync.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashDuration = 2000 //Duración de la pantalla Splash en milisegundos (2 segundos en este ejemplo)

        Handler().postDelayed({
            // Después del tiempo especificado, inicia la MainActivity y cierra SplashActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, splashDuration.toLong())
    }
}
