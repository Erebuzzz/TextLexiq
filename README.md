# TextLexiq

TextLexiq is your smart document companion. Scan images or papers, extract text with OCR, and convert to PDF, Word, or LaTeX. Summarize, ask questions, and get insights; all powered by on-device AI for speed, privacy, and precision. Transform documents into knowledge with ease.

## Features

-   **Smart Scanning**: Capture documents with edge detection, auto-cropping, and perspective correction.
-   **Advanced OCR**: Extract text with high accuracy using on-device ML Kit or robust Tesseract (via Python/MCP).
-   **AI-Powered Insights**: Summarize, simplify, and chat with your documents using local LLMs (Llama.cpp) or cloud models.
-   **Multi-Format Export**: Convert your documents to professional PDF, DOCX (Word), or LaTeX formats.
-   **Privacy First**: All processing can happen locally on your device or within your private network.
-   **Agent Ready**: Exposes functionality as an MCP (Model Context Protocol) server for AI agents.

## Tech Stack

### Android App
-   **Language**: Kotlin
-   **UI**: Jetpack Compose
-   **Architecture**: MVVM
-   **Core Libraries**: CameraX, ML Kit, OpenCV (Android), Room, Llama.cpp (Android)

### Logic Package (`packages/python-logic`)
-   **Language**: Python
-   **Core Libraries**: OpenCV (Headless), NumPy, PyTesseract, Pillow
-   **Purpose**: Advanced image processing and OCR pipeline, mirroring the Android implementation for server-side/agent use.

### MCP Server (`packages/mcp-server`)
-   **Language**: TypeScript
-   **Framework**: LeanMCP / Node.js
-   **Purpose**: Exposes OCR capabilities as AI tools for agents (e.g., in Daytona or simple MCP clients).

## Getting Started

### Android App
1.  Open the project in Android Studio.
2.  Sync Gradle files.
3.  Run on an Android device or emulator (Camera required).

### Python Logic (Standalone)
1.  Navigate to `packages/python-logic`.
2.  Install dependencies: `pip install -r requirements.txt`.
3.  Run OCR: `python main.py ocr <path_to_image>`.

### MCP Server
1.  Navigate to `packages/mcp-server`.
2.  Install dependencies: `npm install`.
3.  Build: `npm run build`.
4.  Start: `npm start`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
