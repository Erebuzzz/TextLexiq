from setuptools import setup, find_packages

setup(
    name="textlexiq-core",
    version="0.1.0",
    description="Core image processing and OCR logic for TextLexiq",
    author="TextLexiq Team",
    packages=find_packages(),
    install_requires=[
        "opencv-python-headless>=4.8.0",
        "numpy>=1.24.0",
        "pytesseract>=0.3.10",
        "Pillow>=10.0.0"
    ],
    entry_points={
        "console_scripts": [
            "textlexiq=main:main",
        ],
    },
)
