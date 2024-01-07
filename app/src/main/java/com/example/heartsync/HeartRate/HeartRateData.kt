package com.example.heartsync.HeartRate

data class HeartRateData(
    val date: String = "",
    val time: String = "",
    val heartRate: Int = 0,
    val activityState: String = "",
    val sdnn: Float = 0f,
    val rmssd: Float = 0f,
    val sdsd: Float = 0f,
    val nn50: Float = 0f,
    val pnn50: Float = 0f,
    val nn20: Float = 0f,
    val pnn20: Float = 0f
)

