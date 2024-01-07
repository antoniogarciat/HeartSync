package com.example.heartsync.HeartRate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboard.pantallas.MainActivity
import com.example.heartsync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiagnosisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        //get la frecuencia cardiaca de SharedPreferences
        val sharedPref = getSharedPreferences("AppData", Context.MODE_PRIVATE)
        val heartRate = sharedPref.getInt("HEART_RATE", 0)

        val sdnn = sharedPref.getFloat("SDNN", 0f)
        val rmssd = sharedPref.getFloat("RMSSD", 0f)
        val sdsd = sharedPref.getFloat("SDSD", 0f)
        val nn50 = sharedPref.getFloat("NN50", 0f)
        val pnn50 = sharedPref.getFloat("pNN50", 0f)
        val nn20 = sharedPref.getFloat("NN20", 0f)
        val pnn20 = sharedPref.getFloat("pNN20", 0f)


        // Inicializar REGISTERED_ACTIVITY a false
        val editor = sharedPref.edit()
        editor.putBoolean("REGISTERED_ACTIVITY", false)
        editor.apply()

        // Comprobar si viene de la pantalla HRV Data
        val comingFromHrvData = intent.getBooleanExtra("COMING_FROM_HRV_DATA", false)
        if (!comingFromHrvData) {
            showActivityStateDialog(heartRate, sdnn, rmssd, sdsd, nn50, pnn50, nn20, pnn20)
        } else {
            val activityState = sharedPref.getString("ACTIVITY_STATE", "Resting") ?: "Resting"
            proceedWithDiagnosis(heartRate, activityState, sdnn, rmssd, sdsd, nn50, pnn50, nn20, pnn20)
        }

        findViewById<Button>(R.id.buttonHRV).setOnClickListener {
            // mostrar la menu principal despues de subir dato
            val intent = Intent(this, HrvActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun showActivityStateDialog(heartRate: Int,sdnn: Float,rmssd: Float, sdsd: Float, nn50: Float, pnn50: Float, nn20: Float, pnn20: Float) {
        val activityStateChoices = arrayOf("Resting", "Active")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select your activity state: ")
        builder.setItems(activityStateChoices) { _, which ->
            val selectedState = activityStateChoices[which]
            proceedWithDiagnosis(heartRate, selectedState,sdnn,rmssd,sdsd,nn50,pnn50,nn20,pnn20)
        }
        builder.show()
    }

    private fun proceedWithDiagnosis(heartRate: Int, activityState: String,sdnn: Float,rmssd: Float, sdsd: Float, nn50: Float, pnn50: Float, nn20: Float, pnn20: Float) {
        val age = getSharedPreferences("UserInfo", MODE_PRIVATE).getInt("USER_AGE", 30) // default age
        val diagnosis = getDiagnosis(heartRate, age, activityState)
        Log.d("DiagnosisActivity", "valor leido: $heartRate")
        findViewById<TextView>(R.id.textViewHeartRate).text = "Heart Rate: $heartRate BPM"
        findViewById<TextView>(R.id.textViewDiagnosis).text = diagnosis

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            uploadDataToFirebase(heartRate, activityState,sdnn,rmssd,sdsd,nn50,pnn50,nn20,pnn20)

            // mostrar el menu principal despues de subir dato
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<ImageButton>(R.id.btn_repeat).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun getDiagnosis(heartRate: Int, age: Int, activityState: String): String {
        val maxHeartRate = 220 - age
        val lowerBound = maxHeartRate * 50 / 100
        val upperBound = if (activityState == "active") maxHeartRate * 85 / 100
        else maxHeartRate * 70 / 100

        return when {
            heartRate < lowerBound -> {
                if (activityState == "active") {
                    "Your heart rate is below the target zone for physical activity. Consider increasing your exercise intensity."
                } else {
                    "Your heart rate is below the normal resting range. If you frequently experience a low resting heart rate, consult a healthcare professional."
                }
            }
            heartRate in lowerBound..upperBound -> {
                if (activityState == "active") {
                    "Your heart rate is within the target zone for physical activity. This is a good intensity level for your age."
                } else {
                    "Your resting heart rate is within a normal range."
                }
            }
            else -> {
                if (activityState == "active") {
                    "Your heart rate is above the target zone for physical activity. Consider slowing down a bit, especially if you feel any discomfort."
                } else {
                    "Your resting heart rate is higher than normal. If this occurs frequently, it's advisable to seek medical advice."
                }
            }
        }
    }

    private fun uploadDataToFirebase(heartRate: Int, activityState: String,sdnn: Float,rmssd: Float, sdsd: Float, nn50: Float, pnn50: Float, nn20: Float, pnn20: Float) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()

        val dateFormat = SimpleDateFormat("dd-MM-yyyy,HH:mm:ss", Locale.getDefault())
        val dateTime = dateFormat.format(Date())
        val data = HeartRateData(
            dateTime.split(",")[0],
            dateTime.split(",")[1],
            heartRate,
            activityState,
            sdnn,
            rmssd,
            sdsd,
            nn50,
            pnn50,
            nn20,
            pnn20
        )
        val databaseReference = Firebase.database.getReference("users/${currentUser.uid}/heartRateData")
        databaseReference.push().setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Data uploaded successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload data", Toast.LENGTH_SHORT).show()
            }
    }

}

