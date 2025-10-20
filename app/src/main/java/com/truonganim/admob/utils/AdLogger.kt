package com.truonganim.admob.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ad Logger with timestamp prefix
 * Format: [AdEvent: HH:mm:ss:SSS] message
 */
object AdLogger {
    
    private const val TAG = "AdEvent"
    private val dateFormat = SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault())
    
    private fun getPrefix(): String {
        return "[AdEvent: ${dateFormat.format(Date())}]"
    }
    
    fun d(place: String, message: String) {
        Log.d(TAG, " ${getPrefix()} [${place}] $message")
    }
    
    fun e(place: String, message: String) {
        Log.e(TAG, "${getPrefix()} [${place}] $message")
    }
    
    fun i(place: String, message: String) {
        Log.i(TAG, "${getPrefix()} [${place}] $message")
    }
    
    fun w(place: String, message: String) {
        Log.w(TAG, "${getPrefix()} [${place}] $message")
    }
}

