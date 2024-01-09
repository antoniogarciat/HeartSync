package com.example.dashboard.pantallas

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.example.heartsync.HeartRate.HeartRateActivity
import com.example.heartsync.R
import com.example.heartsync.Settings.SettingsActivity
import com.example.heartsync.Statistics.StatsActivity
import com.example.heartsync.User.UserInfo
import com.example.heartsync.User.UserLogin
import com.example.heartsync.User.UserSignup
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Forzamos modo claro para evitar problemas de compatibilidad
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val botonHeart = findViewById<CardView>(R.id.botonHeart)
        val botonUser = findViewById<CardView>(R.id.botonUser)
        val botonStats = findViewById<CardView>(R.id.botonStats)
        val botonAjustes = findViewById<CardView>(R.id.botonAjustes)

        botonHeart.setOnClickListener {
            val intent = Intent(this, HeartRateActivity::class.java)
            startActivity(intent)
        }

        botonUser.setOnClickListener {
            val intent = Intent(this, UserInfo::class.java)
            startActivity(intent)

        }

        botonStats.setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }

        botonAjustes.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Comprueba si el usuario ya está autenticado
        if (FirebaseAuth.getInstance().currentUser != null) {
            // Usuario ya autenticado, redigirido a pantalla principal

        } else {
            // Usuario no autenticado, mostrar diálogo bienvenida
            checkFirstRun()
        }
    }

    private fun checkFirstRun() {
        val sharedPref = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        val isFirstRun = sharedPref.getBoolean("isFirstRun", true)

        if (isFirstRun) {
            // Mostrar diálogo si es primera vez
            showFirstRunDialog()

            // Después de mostrar diálogo, isFirstRun=false
            sharedPref.edit().putBoolean("isFirstRun", false).apply()
            requestPermissions()
        }
    }

    private fun showFirstRunDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Welcome")
            .setMessage("Before start measuring your heart rate, you must set your profile.")
            .setPositiveButton("Sign Up") { dialog, which ->
                val intent = Intent(this, UserSignup::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Log In") { dialog, which ->
                val intent = Intent(this, UserLogin::class.java)
                startActivity(intent)
            }
            .setCancelable(false)
            .show()
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }

}