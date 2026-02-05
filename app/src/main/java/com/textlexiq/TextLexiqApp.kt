package com.textlexiq

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class TextLexiqApp : Application() {
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        
        if (OpenCVLoader.initDebug()) {
            Log.i("TextLexiqApp", "OpenCV loaded successfully")
        } else {
            Log.e("TextLexiqApp", "OpenCV initialization failed!")
        }
    }
}
