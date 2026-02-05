package com.textlexiq.ocr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader

@RunWith(AndroidJUnit4::class)
class ImagePreprocessorTest {

    @Before
    fun setUp() {
        // Ensure OpenCV is initialized before running tests
        val success = OpenCVLoader.initDebug()
        assertTrue("OpenCV failed to initialize", success)
    }

    @Test
    fun testEnhanceForOcr_returnsBitmap() {
        // Create a dummy black and white bitmap
        val width = 100
        val height = 100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        // Draw some "text" (white pixels on black)
        for(i in 20..80) {
            bitmap.setPixel(i, 50, Color.WHITE)
        }
        
        val result = ImagePreprocessor.enhanceForOcr(bitmap)
        
        assertNotNull(result)
        assertEquals(width, result.width)
        assertEquals(height, result.height)
        // Check that it's not empty/null
    }

    @Test
    fun testFinalizeForOcr_preservesDimensions() {
        val width = 640
        val height = 480
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val result = ImagePreprocessor.finalizeForOcr(bitmap)
        
        assertNotNull(result)
        assertEquals(width, result.width)
        assertEquals(height, result.height)
    }
}
