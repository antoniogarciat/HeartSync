package com.example.heartsync.Statistics

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.heartsync.HeartRate.HeartRateData
import com.example.heartsync.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class StatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        cargarDatosHeartRate()
    }

    private fun cargarDatosHeartRate() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val databaseReference = Firebase.database.getReference("users/${currentUser.uid}/heartRateData")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val heartRates = ArrayList<HeartRateData>()
                val fechaLimite = System.currentTimeMillis() - 24 * 60 * 60 * 1000  // 24 horas en milisegundos

                snapshot.children.forEach { dataSnapshot ->
                    val heartRateData = dataSnapshot.getValue(HeartRateData::class.java)
                    if (heartRateData != null && convertirFechaAHora(heartRateData.date, heartRateData.time) > fechaLimite) {
                        heartRates.add(heartRateData)
                    }
                }
                actualizarGrafico(heartRates)
                actualizarMaximoYMedia(heartRates)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StatsActivity, "Error loading data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun actualizarGrafico(heartRates: List<HeartRateData>) {
        val entries = ArrayList<Entry>()
        var contador = 1f  //contador iniciado en 1

        heartRates.forEach { heartRateData ->
            //contador como valor x y frec cardíaca como valor y
            entries.add(Entry(contador, heartRateData.heartRate.toFloat()))
            contador++  // Incrementar el contador para la siguiente medición
        }

        val dataSet = LineDataSet(entries, "Frecuencia Cardíaca").apply {
            color = Color.RED
            lineWidth = 3f
        }

        val lineData = LineData(dataSet)

        val lineChart = findViewById<LineChart>(R.id.lineChart).apply {
            setPinchZoom(false)
            setScaleEnabled(false)
            isDragEnabled = true
            axisLeft.setDrawGridLines(false)
            xAxis.apply {
                setDrawGridLines(false)
                setDrawAxisLine(true)
                setDrawLabels(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()  //muestra número de la medición
                    }
                }
            }
            axisRight.isEnabled = false
            description.isEnabled = false
            data = lineData
            invalidate()
        }
    }

    private fun actualizarMaximoYMedia(heartRates: List<HeartRateData>) {
        val maxHeartRate = heartRates.maxOfOrNull { it.heartRate } ?: 0
        val averageHeartRate = heartRates.map { it.heartRate }.average()

        findViewById<TextView>(R.id.textViewMaximum).text = "Maximum heartrate: $maxHeartRate"
        findViewById<TextView>(R.id.textViewAverage).text = "Average heartrate: ${averageHeartRate.toInt()}"
    }

    private fun convertirFechaAHora(date: String, time: String): Long {
        val dateTime = "$date,$time"
        val dateFormat = SimpleDateFormat("dd-MM-yyyy,HH:mm:ss", Locale.getDefault())
        return try {
            val parsedDate = dateFormat.parse(dateTime)
            parsedDate?.time ?: 0L
        } catch (e: ParseException) {
            0L
        }
    }

}




