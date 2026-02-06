import sys
import argparse
import cv2
import numpy as np
import pytesseract
from PIL import Image
import json
import os

def order_points(pts):
    """
    Orders points in the order: top-left, top-right, bottom-right, bottom-left
    """
    rect = np.zeros((4, 2), dtype="float32")

    # the top-left point will have the smallest sum, whereas
    # the bottom-right point will have the largest sum
    s = pts.sum(axis=1)
    rect[0] = pts[np.argmin(s)]
    rect[2] = pts[np.argmax(s)]

    # now, compute the difference between the points, the
    # top-right point will have the smallest difference,
    # whereas the bottom-left will have the largest difference
    diff = np.diff(pts, axis=1)
    rect[1] = pts[np.argmin(diff)]
    rect[3] = pts[np.argmax(diff)]

    return rect

def four_point_transform(image, pts):
    """
    Applies perspective transform to flatten the document.
    Replicates ImageTransformer.correctPerspective
    """
    rect = order_points(pts)
    (tl, tr, br, bl) = rect

    # compute the width of the new image, which will be the
    # maximum distance between bottom-right and bottom-left
    # x-coord or the top-right and top-left x-coords
    widthA = np.sqrt(((br[0] - bl[0]) ** 2) + ((br[1] - bl[1]) ** 2))
    widthB = np.sqrt(((tr[0] - tl[0]) ** 2) + ((tr[1] - tl[1]) ** 2))
    maxWidth = max(int(widthA), int(widthB))

    # compute the height of the new image, which will be the
    # maximum distance between the top-right and bottom-right
    # y-coords or the top-left and bottom-left y-coords
    heightA = np.sqrt(((tr[0] - br[0]) ** 2) + ((tr[1] - br[1]) ** 2))
    heightB = np.sqrt(((tl[0] - bl[0]) ** 2) + ((tl[1] - bl[1]) ** 2))
    maxHeight = max(int(heightA), int(heightB))

    # now that we have the dimensions of the new image, construct
    # the set of destination points to obtain a "birds eye view",
    # (i.e. top-down view) of the image, again specifying points
    # in the top-left, top-right, bottom-right, and bottom-left
    # order
    dst = np.array([
        [0, 0],
        [maxWidth - 1, 0],
        [maxWidth - 1, maxHeight - 1],
        [0, maxHeight - 1]], dtype="float32")

    # compute the perspective transform matrix and then apply it
    M = cv2.getPerspectiveTransform(rect, dst)
    warped = cv2.warpPerspective(image, M, (maxWidth, maxHeight))

    return warped

def detect_document(image):
    """
    Detects document corners.
    Replicates EdgeDetector.detectDocument
    """
    ratio = image.shape[0] / 500.0
    orig = image.copy()
    image = cv2.resize(image, (int(image.shape[1] / ratio), 500))

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    gray = cv2.GaussianBlur(gray, (5, 5), 0)
    
    # Canny parameters from EdgeDetector.kt (75.0, 200.0)
    edged = cv2.Canny(gray, 75, 200)

    cnts = cv2.findContours(edged.copy(), cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    cnts = cnts[0] if len(cnts) == 2 else cnts[1]
    cnts = sorted(cnts, key=cv2.contourArea, reverse=True)[:5]

    screenCnt = None

    for c in cnts:
        # approxPolyDP parameters from EdgeDetector.kt (0.02 * peri)
        peri = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.02 * peri, True)

        if len(approx) == 4:
            screenCnt = approx
            break

    if screenCnt is not None:
        # Rescale points back to original image size
        return screenCnt.reshape(4, 2) * ratio
    else:
        return None

def preprocess_image(image_path):
    """
    Replicates ImagePreprocessor.kt logic:
    1. Detect Document & Crop (if found)
    2. Denoise
    3. Grayscale
    4. Adaptive Threshold (Binarize)
    """
    img = cv2.imread(image_path)
    if img is None:
        raise ValueError("Could not read image")

    # Try to detect and crop document
    doc_cnt = detect_document(img)
    if doc_cnt is not None:
        try:
            img = four_point_transform(img, doc_cnt)
        except Exception as e:
            # Fallback if transform fails
            pass

    # Denoise
    denoised = cv2.GaussianBlur(img, (3, 3), 0)
    
    # Grayscale
    gray = cv2.cvtColor(denoised, cv2.COLOR_BGR2GRAY)
    
    # Adaptive Threshold
    # Using parameters from ImagePreprocessor.kt logic (retained from original main.py)
    binary = cv2.adaptiveThreshold(
        gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 31, 15
    )
    
    return binary

def run_ocr(image_path):
    """
    Runs Tesseract OCR on the image.
    """
    # Preprocess first
    processed = preprocess_image(image_path)
    
    # Save temp for Tesseract (or pass PIL image)
    temp_filename = f"{image_path}.processed.png"
    cv2.imwrite(temp_filename, processed)
    
    # Run Tesseract
    pil_img = Image.fromarray(processed)
    text = pytesseract.image_to_string(pil_img)
    
    return {"text": text.strip(), "processed_image_path": temp_filename}

def main():
    parser = argparse.ArgumentParser(description="TextLexiq Core Logic")
    parser.add_argument("command", choices=["preprocess", "ocr"], help="Command to run")
    parser.add_argument("image_path", help="Path to input image")
    
    args = parser.parse_args()
    
    try:
        if args.command == "preprocess":
            processed = preprocess_image(args.image_path)
            output_path = f"{args.image_path}.processed.png"
            cv2.imwrite(output_path, processed)
            print(json.dumps({"status": "success", "output_path": output_path}))
            
        elif args.command == "ocr":
            result = run_ocr(args.image_path)
            print(json.dumps({"status": "success", "result": result}))
            
    except Exception as e:
        print(json.dumps({"status": "error", "message": str(e)}))
        sys.exit(1)

if __name__ == "__main__":
    main()
