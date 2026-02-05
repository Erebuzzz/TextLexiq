package com.textlexiq.scanner

import android.graphics.Bitmap
import android.graphics.PointF
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.Collections
import kotlin.math.abs

object EdgeDetector {

    // Returns normalized points (0..1) for the 4 corners of the document
    fun detectDocument(bitmap: Bitmap): List<PointF>? {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY)
        
        // Downscale for performance
        val scale = 0.5
        val smallGray = Mat()
        Imgproc.resize(gray, smallGray, Size(), scale, scale, Imgproc.INTER_AREA)

        // Blur & Canny
        Imgproc.GaussianBlur(smallGray, smallGray, Size(5.0, 5.0), 0.0)
        val edges = Mat()
        Imgproc.Canny(smallGray, edges, 75.0, 200.0)

        // Find Contours
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        // Find largest 4-sided polygon
        var maxArea = 0.0
        var bestRect: List<PointF>? = null
        
        // Sort by area descending to find large document quickly
        contours.sortByDescending { Imgproc.contourArea(it) }

        for (c in contours) {
            val area = Imgproc.contourArea(c)
            if (area < 1000) break // Too small

            val peri = Imgproc.arcLength(MatOfPoint2f(*c.toArray()), true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*c.toArray()), approx, 0.02 * peri, true)

            if (approx.toArray().size == 4) {
                 // Found a quad!
                 // Convert back to original scale
                 val points = approx.toArray().map { 
                     PointF((it.x / scale).toFloat() / bitmap.width, (it.y / scale).toFloat() / bitmap.height)
                 }
                 // Sort TL, TR, BR, BL for consistency ? (Optional)
                 bestRect = points
                 break
            }
        }
        
        return bestRect
    }
}
