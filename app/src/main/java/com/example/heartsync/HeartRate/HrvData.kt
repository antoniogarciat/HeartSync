package com.example.heartsync.HeartRate

import kotlin.math.pow
import kotlin.math.sqrt

class HrvData {

    fun calculateAVNN(rrInterval: ArrayList<Double>): Double {
        var sum = 0.0
        for (d in rrInterval) {
            sum += d
        }
        return sum / rrInterval.size

    }

    fun calculateSDNN(rrIntervals: ArrayList<Double>): Double {
        val mean = calculateAVNN(rrIntervals)
        return sqrt(rrIntervals.sumOf { (it - mean).pow(2) } / rrIntervals.size)
    }

    fun calculateRMSSD(rrIntervals: ArrayList<Double>): Double {
        var sumOfSquares = 0.0
        for (i in 0 until rrIntervals.size - 1) {
            val diff = rrIntervals[i + 1] - rrIntervals[i]
            sumOfSquares += diff.pow(2)
        }
        return sqrt(sumOfSquares / (rrIntervals.size - 1))
    }

    fun calculatePNN50(rrInterval: ArrayList<Double>): Double {
        var count = 0
        val size: Int = rrInterval.size
        for (i in 1 until size) {
            val diff: Double = rrInterval[i] - rrInterval[i - 1]
            if (diff > 0.05) {
                ++count
            }
        }
        return count.toDouble() / (rrInterval.size - 1) * 100
    }

    fun calculateSDSD(rrIntervals: ArrayList<Double>): Double {
        if (rrIntervals.size <= 1) return 0.0

        val diffList = ArrayList<Double>()
        for (i in 0 until rrIntervals.size - 1) {
            val diff = rrIntervals[i + 1] - rrIntervals[i]
            diffList.add(diff)
        }

        val mean = diffList.average()
        var sumOfSquares = 0.0
        for (diff in diffList) {
            sumOfSquares += (diff - mean).pow(2)
        }

        return sqrt(sumOfSquares / (diffList.size - 1))
    }


    fun calculateNN50(rrInterval: ArrayList<Double>): Int {
        var count = 0
        val size: Int = rrInterval.size
        for (i in 1 until size) {
            val diff: Double = rrInterval[i] - rrInterval[i - 1]
            if (diff > 0.05) {
                ++count
            }
        }
        return count
    }

    fun calculateNN20(rrInterval: ArrayList<Double>): Int {
        var count = 0
        val size: Int = rrInterval.size
        for (i in 1 until size) {
            val diff: Double = rrInterval[i] - rrInterval[i - 1]
            if (diff > 0.02) {
                ++count
            }
        }
        return count
    }

    fun calculatePNN20(rrInterval: ArrayList<Double>): Double {
        var count = 0
        val size: Int = rrInterval.size
        for (i in 1 until size) {
            val diff: Double = rrInterval[i] - rrInterval[i - 1]
            if (diff > 0.02) {
                ++count
            }
        }
        return count.toDouble() / (rrInterval.size - 1) * 100
    }

}