package com.example.heartsync.HeartRate

import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.example.heartsync.R
import java.util.concurrent.atomic.AtomicBoolean
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class HeartRateActivity : AppCompatActivity() {

    private val TAG = "HeartRateMonitor"
    private val processing = AtomicBoolean(false)
    private var preview: SurfaceView? = null
    private var previewHolder: SurfaceHolder? = null
    private var camera: Camera? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var isMeasuring = false

    private lateinit var progressBar: ProgressBar
    private var startTime: Long = 0

    private var timeQueue = ArrayList<Long>()
    private var redAvgList = ArrayList<Double>()
    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate)

        // Inicialización de la ProgressBar
        progressBar = findViewById(R.id.circularProgressBar)
        progressBar.max = 30_000  // 30 segundos en milisegundos


        preview = findViewById(R.id.surfaceView)
        previewHolder = preview!!.holder
        previewHolder!!.addCallback(surfaceCallback)
        previewHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)


        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "myapp:HeartRateMonitorWakeLock")

        findViewById<Button>(R.id.buttonStartMeasure).setOnClickListener {
            isMeasuring = true
            startTime =
                System.currentTimeMillis() // reinicia contador de tiempo
            progressBar.progress = 0 // reinicia barra de progreso
            startCameraPreview() // inicia vista previa de la camara
        }

    }

    private fun startCameraPreview() {
        previewHolder!!.addCallback(surfaceCallback)
        previewHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        camera!!.setPreviewCallback(previewCallback)
        camera!!.startPreview()
    }

    override fun onResume() {
        super.onResume()
        wakeLock!!.acquire()
        camera = Camera.open()
        camera!!.setDisplayOrientation(90)
    }

    override fun onPause() {
        super.onPause()
        wakeLock!!.release()
        camera!!.setPreviewCallback(null)
        camera!!.stopPreview()
        camera!!.release()
        camera = null
    }

    private val previewCallback = Camera.PreviewCallback { data, cam ->
        if (data == null) throw NullPointerException()
        val size = cam.parameters.previewSize ?: throw NullPointerException()

        if (!processing.compareAndSet(false, true)) return@PreviewCallback

        val width = size.width
        val height = size.height
        var redAvg = 0

        if (redAvg < 200) {
            counter = 0
            processing.set(false)
        }
        val endTime = System.currentTimeMillis()

        if (isMeasuring) {
            redAvg= ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width)
            redAvgList.add(redAvg.toDouble())
            timeQueue.add(System.currentTimeMillis())
            ++counter
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - startTime

            // actualiza barra de progreso
            progressBar.progress = elapsedTime.toInt()

            if (elapsedTime >= 30_000) {
                // ajusta el tamaño de las lista a una potencia de 2
                val adjustedList = adjustListSizeToNextPowerOfTwo(redAvgList)
                val fftSize = adjustedList.size

                if (redAvgList.size < MINIMUM_DATA_SIZE) return@PreviewCallback

                val peaks = SignalProcessing().getPeaks(redAvgList, false)
                val rrIntervals = calculateRRIntervals(peaks, timeQueue)
                Log.d("ClasePruebas", "Valor rrInterval: $rrIntervals")

                val x = toPrimitive(adjustedList.toArray(arrayOfNulls<Double>(0)) as Array<Double>)
                val y = DoubleArray(fftSize)

                val time = toPrimitive(timeQueue.toArray(arrayOfNulls<Long>(0)) as Array<Long>)

                if (timeQueue.size < fftSize) {
                    processing.set(false)
                    //Log.d("ClasePruebas","Dentro de esta condicion")
                    return@PreviewCallback
                }

                val fs =
                    timeQueue.size.toDouble() / (time!![timeQueue.size - 1] - time[0]).toDouble() * 1000
                Log.d("ClasePruebas", "Valor fs: $fs")

                val fft = FFT(fftSize)
                fft.fft(x!!, y)

                val low = ((fftSize * 40).toDouble() / 60.0 / fs).toFloat().roundToInt()
                val high = ((fftSize * 160).toDouble() / 60.0 / fs).toFloat().roundToInt()

                var bestI = 0
                var bestV = 0.0
                for (i in low until high) {
                    val value = kotlin.math.sqrt(x[i] * x[i] + y[i] * y[i])

                    if (value > bestV) {
                        bestV = value
                        bestI = i
                    }
                }

                val frequency = (bestI.toDouble() * fs) / fftSize
                val bpm = (frequency * 60).toInt()
                val avnn = HrvData().calculateAVNN(rrIntervals)
                val sdsd = HrvData().calculateSDSD(rrIntervals)
                val nn50 = HrvData().calculateNN50(rrIntervals)
                val nn20 = HrvData().calculateNN20(rrIntervals)
                val sdnn = HrvData().calculateSDNN(rrIntervals)
                val rmssd = HrvData().calculateRMSSD(rrIntervals)
                val pnn50 = HrvData().calculatePNN50(rrIntervals)
                val pnn20 = HrvData().calculatePNN20(rrIntervals)

                //guardamos frec cardiaca en SharedPreferences
                val sharedPref = getSharedPreferences("AppData", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putInt("HEART_RATE", bpm)
                    putFloat("AVNN", avnn.toFloat())
                    putFloat("SDSD", sdsd.toFloat())
                    putFloat("NN50", nn50.toFloat())
                    putFloat("NN20", nn20.toFloat())
                    putFloat("SDNN", sdnn.toFloat())
                    putFloat("RMSSD", rmssd.toFloat())
                    putFloat("pNN50", pnn50.toFloat())
                    putFloat("pNN20", pnn20.toFloat())
                    apply()
                }

                Log.d("ClasePruebas", "Valor bpm: $bpm")
                isMeasuring = false // Detiene la medición
                finish() // Finaliza esta actividad

                //iniciamos DiagnosisActivity
                val intent = Intent(this, DiagnosisActivity::class.java)
                startActivity(intent)

                processing.set(false)
            }
        }

    }

    private fun adjustListSizeToNextPowerOfTwo(list: ArrayList<Double>): ArrayList<Double> {
        var size = list.size
        var powerOfTwo = 1
        while (powerOfTwo < size) {
            powerOfTwo *= 2
        }
        val newList = ArrayList<Double>(list)
        while (newList.size < powerOfTwo) {
            newList.add(0.0) // Rellenar con ceros
        }
        return newList
    }


    private fun getSmallestPreviewSize(
        width: Int,
        height: Int,
        parameters: Camera.Parameters
    ): Camera.Size? {
        var result: Camera.Size? = null
        for (size in parameters.supportedPreviewSizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size
                } else {
                    val resultArea = result.width * result.height
                    val newArea = size.width * size.height
                    if (newArea < resultArea) result = size
                }
            }
        }
        return result
    }

    private fun toPrimitive(array: Array<Double>?): DoubleArray? {
        if (array == null) {
            return null
        } else if (array.isEmpty()) {
            return EMPTY_DOUBLE_ARRAY
        }
        val result = DoubleArray(array.size)
        for (i in array.indices) {
            result[i] = array[i]
        }
        return result
    }

    private val EMPTY_DOUBLE_ARRAY = DoubleArray(0)

    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                camera!!.setPreviewDisplay(previewHolder)
                camera!!.setPreviewCallback(previewCallback)
            } catch (t: Throwable) {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t)
            }
        }

        override fun surfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            val parameters = camera!!.parameters
            parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            val size = getSmallestPreviewSize(width, height, parameters)
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height)
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height)
            }
            camera!!.parameters = parameters
            camera!!.startPreview()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {}
    }


    /**
     * An empty immutable `long` array.
     */
    private val EMPTY_LONG_ARRAY = LongArray(0)

    /**
     *
     * Converts an array of object Longs to primitives.
     *
     *
     * This method returns `null` for a `null` input array.
     *
     * @param array  a `Long` array, may be `null`
     * @return a `long` array, `null` if null array input
     * @throws NullPointerException if array content is `null`
     */
    private fun toPrimitive(array: Array<Long>?): LongArray? {
        if (array == null) {
            return null
        } else if (array.isEmpty()) {
            return EMPTY_LONG_ARRAY
        }
        val result = LongArray(array.size)
        for (i in array.indices) {
            result[i] = array[i]
        }
        return result
    }

    private fun calculateRRIntervals(
        peaks: Map<Double, Double>,
        times: ArrayList<Long>
    ): ArrayList<Double> {
        val rrIntervals = ArrayList<Double>()
        var previousPeakTime: Long? = null

        for ((index, _) in peaks) {
            val currentPeakTime = times[index.toInt()]

            previousPeakTime?.let {
                val rrInterval = (currentPeakTime - it) / 1000.0 // Convertir a segundos
                rrIntervals.add(rrInterval)
            }

            previousPeakTime = currentPeakTime
        }

        return rrIntervals
    }

    companion object {
        private const val MINIMUM_DATA_SIZE = 256 // Un ejemplo de tamaño mínimo de datos
    }

    fun createCsvFile(context: Context, userId: String, redAvgList: ArrayList<Double>, timeQueue: ArrayList<Long>, rrIntervals: ArrayList<Double>) {
        val fileName = "heart_rate_data.csv"
        val file = File(context.filesDir, fileName)
        file.printWriter().use { out ->
            // Escribir UID del usuario en el archivo
            out.println("UserID,$userId")

            // Escribir encabezados del CSV
            out.println("Timestamp,RedAverage,RRInterval")

            // Escribir datos
            for (i in redAvgList.indices) {
                val timestamp = timeQueue.getOrNull(i) ?: ""
                val redAvg = redAvgList.getOrNull(i) ?: ""
                val rrInterval = rrIntervals.getOrNull(i) ?: ""
                out.println("$timestamp,$redAvg,$rrInterval")
            }
        }
    }

    fun uploadCsvToFirebase(context: Context) {
        val fileName = "heart_rate_data.csv"
        val file = File(context.filesDir, fileName)

        if (file.exists()) {
            val storageReference = Firebase.storage.reference
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: "User_not_identified"
            val csvRef = storageReference.child("csv/$userId/$fileName")

            val uploadTask = csvRef.putFile(Uri.fromFile(file))
            uploadTask.addOnSuccessListener {
                Toast.makeText(context, "CSV was uploaded", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Error uploading CSV: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "CSV file not found", Toast.LENGTH_SHORT).show()
        }
    }


}




