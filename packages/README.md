# TextLexiq Ecosystem Packages

This directory contains the ecosystem integrations for TextLexiq, allowing the core OCR and image processing logic to be used outside the Android app.

## Structure

### 1. `python-logic` (Core Logic)
A Python package that replicates the Android app's image processing pipeline using OpenCV and Tesseract.
- **Usage**: `python main.py ocr <image_path>`
- **Dependencies**: `opencv-python-headless`, `numpy`, `pytesseract`

### 2. `mcp-server` (Agent API)
A TypeScript Model Context Protocol system built with **LeanMCP** SDK.
- **Purpose**: Exposes the logic as a tool (`ocr_document`) for AI agents (Claude, etc.).
- **Usage**: `npm start` (Runs on port 3000)
- **Integration**: Bridges to the Python logic via child process.

## Daytona Integration
The root `.devcontainer` folder allows this entire repository to be opened in **Daytona** (or GitHub Codespaces/VSCode Remote) with all dependencies (Android SDK, Python, Node.js, OpenCV) pre-installed.

### Quick Start (Daytona)
1. Run `daytona create <repo-url>`
2. Open the workspace.
3. Run `cd packages/mcp-server && npm install && npm start`.
4. Connect your AI agent to the MCP endpoint.
