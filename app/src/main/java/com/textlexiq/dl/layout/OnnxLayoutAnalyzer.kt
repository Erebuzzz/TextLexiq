package com.textlexiq.dl.layout

import android.content.Context
import android.graphics.Bitmap
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import java.util.Collections

class OnnxLayoutAnalyzer(
    private val context: Context,
    private val modelPath: String
) : LayoutAnalyzer {

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    init {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            // In a real app, modelPath ensures the file exists before creation
            // ortSession = ortEnv?.createSession(modelPath, OrtSession.SessionOptions())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun analyze(bitmap: Bitmap): List<LayoutRegion> = withContext(Dispatchers.Default) {
        if (ortSession == null) {
            // Check if model exists now (user might have just downloaded it)
            val file = java.io.File(modelPath)
            if (file.exists()) {
                 try {
                     ortSession = ortEnv?.createSession(modelPath, OrtSession.SessionOptions())
                 } catch(e: Exception) {
                     return@withContext emptyList()
                 }
            } else {
                return@withContext emptyList()
            }
        }

        // 1. Preprocess: Resize to 640x640 (standard YOLO input)
        val resized = Bitmap.createScaledBitmap(bitmap, 640, 640, true)
        
        // 2. Convert to FloatBuffer (NCHW format)
        val inputName = ortSession?.inputNames?.iterator()?.next() ?: "images"
        val floatBuffer = bitmapToFloatBuffer(resized)
        
        // 3. Run Inference
        val inputTensor = ai.onnxruntime.OnnxTensor.createTensor(ortEnv, floatBuffer, longArrayOf(1, 3, 640, 640))
        val result = ortSession?.run(Collections.singletonMap(inputName, inputTensor))
        
        // 4. Post-process output
        // YOLOv8 output is usually [1, 84, 8400] (Classes + Box coords)
        // This parser is simplified as we don't have the actual model weights/structure confirmed yet.
        return@withContext parseOutput(result)
    }

    private fun bitmapToFloatBuffer(bitmap: Bitmap): FloatBuffer {
        val buffer = FloatBuffer.allocate(1 * 3 * 640 * 640)
        buffer.rewind()
        
        val intValues = IntArray(640 * 640)
        bitmap.getPixels(intValues, 0, 640, 0, 0, 640, 640)
        
        // Normalize 0..255 to 0..1
        for (i in 0 until 640 * 640) {
            val pixel = intValues[i]
            buffer.put(((pixel shr 16 and 0xFF) / 255.0f)) // R
        }
        for (i in 0 until 640 * 640) {
            val pixel = intValues[i]
            buffer.put(((pixel shr 8 and 0xFF) / 255.0f))  // G
        }
        for (i in 0 until 640 * 640) {
            val pixel = intValues[i]
            buffer.put(((pixel and 0xFF) / 255.0f))        // B
        }
        buffer.rewind()
        return buffer
    }

    private fun parseOutput(result: OrtSession.Result?): List<LayoutRegion> {
        // Stub for output parsing
        return emptyList() 
    }
}
