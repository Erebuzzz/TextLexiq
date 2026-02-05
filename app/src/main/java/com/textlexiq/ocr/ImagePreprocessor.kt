package com.textlexiq.ocr

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.core.MatOfPoint2f
import kotlin.math.sqrt

object ImagePreprocessor {

    /**
     * Enhances the image for OCR by applying:
     * 1. Grayscale conversion
     * 2. Skew correction (deskewing)
     * 3. Adaptive thresholding (binarization)
     * 4. Denoising
     */
    fun enhanceForOcr(bitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // 1. Grayscale
        val gray = Mat()
        if (mat.channels() > 1) {
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
        } else {
            mat.copyTo(gray)
        }

        // 2. Deskew
        val deskewed = correctSkew(gray)

        // 3. Binarize (Adaptive Threshold)
        val binary = Mat()
        Imgproc.adaptiveThreshold(
            deskewed,
            binary,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            31, // Block size
            15.0 // Constant c
        )

        // 4. Convert back to Bitmap
        // Create output bitmap. Note: ARGB_8888 is standard for Android UI
        val result = Bitmap.createBitmap(binary.cols(), binary.rows(), Bitmap.Config.ARGB_8888)
        
        // Convert single channel binary back to 4-channel for Bitmap
        val finalMat = Mat()
        Imgproc.cvtColor(binary, finalMat, Imgproc.COLOR_GRAY2RGBA)
        Utils.matToBitmap(finalMat, result)

        // Cleanup
        mat.release()
        gray.release()
        if (deskewed != gray) deskewed.release()
        binary.release()
        finalMat.release()

        return result
    }

    private fun correctSkew(gray: Mat): Mat {
        // Invert threshold to get white text on black background for contour/moment analysis
        val binary = Mat()
        Imgproc.threshold(gray, binary, 0.0, 255.0, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU)

        // Find all non-zero points (text pixels)
        val points = MatOfPoint()
        org.opencv.core.Core.findNonZero(binary, points)
        
        // If no text found, return original
        if (points.empty()) {
            binary.release()
            points.release()
            return gray
        }

        // Calculate minimum area rectangle covering all text
        val points2f = MatOfPoint2f(*points.toArray())
        val rotatedRect = Imgproc.minAreaRect(points2f)
        
        points.release()
        points2f.release()
        binary.release()

        var angle = rotatedRect.angle
        
        // Normalize angle to horizontal
        if (angle < -45.0) {
            angle += 90.0
        } else if (angle > 45.0) {
            angle -= 90.0
        }

        // If angle is negligible, don't rotate
        if (kotlin.math.abs(angle) < 0.5) {
            return gray
        }

        val center = rotatedRect.center
        val rotMat = Imgproc.getRotationMatrix2D(center, angle, 1.0)
        
        val rotated = Mat()
        Imgproc.warpAffine(
            gray, 
            rotated, 
            rotMat, 
            gray.size(), 
            Imgproc.INTER_CUBIC + Imgproc.WARP_FILL_OUTLIERS, 
            org.opencv.core.Core.BORDER_REPLICATE
        )
        
        rotMat.release()
        return rotated
    }

    fun preprocess(bitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val denoised = Mat()
        Imgproc.GaussianBlur(mat, denoised, Size(3.0, 3.0), 0.0)

        val gray = Mat()
        Imgproc.cvtColor(denoised, gray, Imgproc.COLOR_BGR2GRAY)

        val tilted = correctPerspective(gray)

        val binarized = Mat()
        Imgproc.adaptiveThreshold(
            tilted,
            binarized,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            35,
            10.0
        )

        val outputBitmap = Bitmap.createBitmap(binarized.cols(), binarized.rows(), Bitmap.Config.ARGB_8888)
        Imgproc.cvtColor(binarized, binarized, Imgproc.COLOR_GRAY2BGR)
        Utils.matToBitmap(binarized, outputBitmap)

        mat.release()
        denoised.release()
        gray.release()
        tilted.release()
        binarized.release()

        return outputBitmap
    }

    /**
     * Preprocesses an image that has already been cropped/warped by the user.
     * Skips geometric transformations and only applies visual enhancement.
     */
    fun finalizeForOcr(bitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val denoised = Mat()
        Imgproc.GaussianBlur(mat, denoised, Size(3.0, 3.0), 0.0)

        val gray = Mat()
        Imgproc.cvtColor(denoised, gray, Imgproc.COLOR_BGR2GRAY)

        val binarized = Mat()
        Imgproc.adaptiveThreshold(
            gray,
            binarized,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            35,
            10.0
        )

        val outputBitmap = Bitmap.createBitmap(binarized.cols(), binarized.rows(), Bitmap.Config.ARGB_8888)
        Imgproc.cvtColor(binarized, binarized, Imgproc.COLOR_GRAY2BGR)
        Utils.matToBitmap(binarized, outputBitmap)

        mat.release()
        denoised.release()
        gray.release()
        binarized.release()

        return outputBitmap
    }

    private fun correctPerspective(gray: Mat): Mat {
        val edges = Mat()
        Imgproc.Canny(gray, edges, 75.0, 200.0)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        contours.sortByDescending { Imgproc.contourArea(it) }

        val screenContour = contours.firstOrNull { contour ->
            val contourPoints = MatOfPoint2f(*contour.toArray())
            val peri = Imgproc.arcLength(contourPoints, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(contourPoints, approx, 0.02 * peri, true)
            val isQuadrilateral = approx.total() == 4L
            contourPoints.release()
            approx.release()
            isQuadrilateral
        }

        if (screenContour == null) {
            edges.release()
            hierarchy.release()
            contours.forEach { it.release() }
            return gray
        }

        val contour2f = MatOfPoint2f(*screenContour.toArray())
        val points = contour2f.toArray()
        val ordered = orderPoints(points)

        val widthA = distance(ordered[2], ordered[3])
        val widthB = distance(ordered[1], ordered[0])
        val maxWidth = maxOf(widthA, widthB).toInt()

        val heightA = distance(ordered[1], ordered[2])
        val heightB = distance(ordered[0], ordered[3])
        val maxHeight = maxOf(heightA, heightB).toInt()

        val destination = MatOfPoint2f(
            Point(0.0, 0.0),
            Point((maxWidth - 1).toDouble(), 0.0),
            Point((maxWidth - 1).toDouble(), (maxHeight - 1).toDouble()),
            Point(0.0, (maxHeight - 1).toDouble())
        )

        val transform = Imgproc.getPerspectiveTransform(contour2f, destination)
        val warped = Mat()
        Imgproc.warpPerspective(gray, warped, transform, Size(maxWidth.toDouble(), maxHeight.toDouble()))

    edges.release()
    hierarchy.release()
    contours.forEach { it.release() }
        contour2f.release()
        destination.release()
        transform.release()

        return warped
    }

    private fun orderPoints(points: Array<Point>): Array<Point> {
        val sortedByX = points.sortedBy { it.x }
        val leftMost = sortedByX.take(2)
        val rightMost = sortedByX.takeLast(2)

        val topLeft = leftMost.minByOrNull { it.y } ?: points.first()
        val bottomLeft = leftMost.maxByOrNull { it.y } ?: points.first()
        val topRight = rightMost.minByOrNull { it.y } ?: points.last()
        val bottomRight = rightMost.maxByOrNull { it.y } ?: points.last()

        return arrayOf(topLeft, topRight, bottomRight, bottomLeft)
    }

    private fun distance(p1: Point, p2: Point): Double {
        return sqrt(((p2.x - p1.x) * (p2.x - p1.x)) + ((p2.y - p1.y) * (p2.y - p1.y)))
    }
}
