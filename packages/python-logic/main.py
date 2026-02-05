import sys
import argparse
import cv2
import numpy as np
import pytesseract
from PIL import Image
import json
import os

def preprocess_image(image_path):
    """
    Replicates ImagePreprocessor.kt logic:
    1. Grayscale
    2. Adaptive Threshold (Binarize)
    3. Denoise
    """
    img = cv2.imread(image_path)
    if img is None:
        raise ValueError("Could not read image")

    # Denoise
    denoised = cv2.GaussianBlur(img, (3, 3), 0)
    
    # Grayscale
    gray = cv2.cvtColor(denoised, cv2.COLOR_BGR2GRAY)
    
    # Adaptive Threshold
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
    # Note: efficient way is passing numpy array via PIL
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
