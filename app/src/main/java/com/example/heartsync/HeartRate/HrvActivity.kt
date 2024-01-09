package com.example.heartsync.HeartRate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.heartsync.R

class HrvActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hrvdata)
        retrieveAndDisplayHRVData()

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            // Inicia DiagnosisActivity
            val intent = Intent(this, DiagnosisActivity::class.java)
            intent.putExtra("COMING_FROM_HRV_DATA", true)
            startActivity(intent)
            // Opcionalmente, termina esta actividad si ya no es necesaria
            finish()
        }

    }

    private fun retrieveAndDisplayHRVData() {
        val sharedPref = getSharedPreferences("AppData", Context.MODE_PRIVATE)

        val sdnn = sharedPref.getFloat("SDNN", 0f)
        val rmssd = sharedPref.getFloat("RMSSD", 0f)
        val avnn = sharedPref.getFloat("AVNN", 0f)
        val pnn50 = sharedPref.getFloat("pNN50", 0f)
        val sdsd = sharedPref.getFloat("SDSD", 0f)
        val nn50 = sharedPref.getFloat("NN50", 0f)
        val nn20 = sharedPref.getFloat("NN20", 0f)
        val pnn20 = sharedPref.getFloat("pNN20", 0f)

        // Actualizamos la UI con los datos
        findViewById<TextView>(R.id.sdsd_info).text = "SDSD: ${roundToTwoDecimals(sdsd)} ms"
        findViewById<TextView>(R.id.sdnn_info).text = "SDNN: ${roundToTwoDecimals(sdnn)} ms"
        findViewById<TextView>(R.id.rmssd_info).text = "RMSSD: ${roundToTwoDecimals(rmssd)} ms"
        findViewById<TextView>(R.id.pnn50_info).text = "pNN50: ${roundToTwoDecimals(pnn50)}%"
        findViewById<TextView>(R.id.pnn20_info).text = "pNN20: ${roundToTwoDecimals(pnn20)}%"
        findViewById<TextView>(R.id.nn20_info).text = "NN20: ${roundToTwoDecimals(nn20)} ms"
        findViewById<TextView>(R.id.nn50_info).text = "NN50: ${roundToTwoDecimals(nn50)} ms"
    }

    fun roundToTwoDecimals(value: Float): String {
        return "%.2f".format(value)
    }

}
