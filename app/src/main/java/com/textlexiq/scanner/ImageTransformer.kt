package com.textlexiq.scanner

import android.graphics.Bitmap
import android.graphics.PointF
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

object ImageTransformer {

    fun correctPerspective(bitmap: Bitmap, corners: List<PointF>): Bitmap {
        if (corners.size != 4) return bitmap

        // Convert PointF to OpenCV Point
        // Input points are normalized 0..1, so scale to bitmap dimensions
        val w = bitmap.width.toDouble()
        val h = bitmap.height.toDouble()
        
        val srcPoints = listOf(
            Point(corners[0].x * w, corners[0].y * h), // TL
            Point(corners[1].x * w, corners[1].y * h), // TR
            Point(corners[2].x * w, corners[2].y * h), // BR
            Point(corners[3].x * w, corners[3].y * h)  // BL
        )

        // Calculate destination dimensions (max width/height)
        val widthA = distance(srcPoints[2], srcPoints[3]) // BR - BL
        val widthB = distance(srcPoints[1], srcPoints[0]) // TR - TL
        val maxWidth = max(widthA, widthB)

        val heightA = distance(srcPoints[1], srcPoints[2]) // TR - BR
        val heightB = distance(srcPoints[0], srcPoints[3]) // TL - BL
        val maxHeight = max(heightA, heightB)

        // Destination points (Rectangular)
        val dstPoints = listOf(
            Point(0.0, 0.0),
            Point(maxWidth - 1, 0.0),
            Point(maxWidth - 1, maxHeight - 1),
            Point(0.0, maxHeight - 1)
        )

        val srcMat = MatOfPoint2f(*srcPoints.toTypedArray())
        val dstMat = MatOfPoint2f(*dstPoints.toTypedArray())

        // Compute Homography
        val perspectiveMatrix = Imgproc.getPerspectiveTransform(srcMat, dstMat)
        
        // Apply Transform
        val srcLoc = Mat()
        Utils.bitmapToMat(bitmap, srcLoc)
        val dstLoc = Mat()
        
        Imgproc.warpPerspective(
            srcLoc, 
            dstLoc, 
            perspectiveMatrix, 
            Size(maxWidth, maxHeight)
        )

        // Convert back to Bitmap
        val result = Bitmap.createBitmap(maxWidth.toInt(), maxHeight.toInt(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dstLoc, result)
        
        return result
    }

    private fun distance(p1: Point, p2: Point): Double {
        return sqrt((p1.x - p2.x).pow(2.0) + (p1.y - p2.y).pow(2.0))
    }
}
