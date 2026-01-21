package com.textlexiq

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class TextLexiqApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val initialized = OpenCVLoader.initDebug()
        if (!initialized) {
            Log.w("TextLexiqApp", "Failed to initialize OpenCV; image preprocessing will be degraded until runtime init succeeds.")
        }
    }
}
