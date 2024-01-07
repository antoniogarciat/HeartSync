package com.example.heartsync.HeartRate

class SignalProcessing {
    fun getPeaks(values: ArrayList<Double>, isTime: Boolean): Map<Double, Double> {
        val peaks = LinkedHashMap<Double, Double>()
        var lookForMax = true
        var maximum = Double.MIN_VALUE
        var minimum = Double.MAX_VALUE
        var maximumPos = 0.0
        var minimumPos = 0.0

        for (i in values.indices) {
            val value = values[i]
            val x = if (isTime) i.toDouble() / values.size else i.toDouble()

            if (value > maximum) {
                maximum = value
                maximumPos = x
            }

            if (value < minimum) {
                minimum = value
                minimumPos = x
            }

            if (lookForMax) {
                if (value < maximum) {
                    peaks[maximumPos] = maximum
                    minimum = value
                    minimumPos = x
                    lookForMax = false
                }
            } else {
                if (value > minimum) {
                    minimum = value
                    maximum = value
                    maximumPos = x
                    lookForMax = true
                }
            }
        }
        return peaks
    }

}