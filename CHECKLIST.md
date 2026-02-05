# TextLexiq Checklist

## Vision
Build a mobile-first document intelligence workspace that lets users capture paper documents, clean and OCR them reliably, refine text with on-device or cloud LLMs, and export to PDF/DOCX—while keeping data private and usable offline.

---

## ✅ Implemented (Working / Scaffolded)

### Project & Architecture
- MVVM structure with `ui/`, `viewmodel/`, `data/`, `ocr/`, `exporter/`, `utils/` modules.
- Navigation graph with Home → Scanner → Crop/Preview → OCR → Document.
- Centralized ViewModel factory for injection-ready flows.

### Scanner & Capture
- CameraX live preview integrated in Compose.
- Capture button saves image to internal storage.
- Basic rectangular overlay guide for edge alignment.
- Runtime camera permission flow handled.

### Crop & Preprocess
- Crop and preview screen with adjustable horizontal/vertical bounds.
- OpenCV preprocessing pipeline:
  - Denoise (Gaussian blur)
  - Grayscale conversion
  - Perspective correction (basic contour-based deskew)
  - Adaptive thresholding (binarize for OCR)
- Processed image is saved to internal storage and forwarded to OCR.

### OCR
- ML Kit Text Recognition implemented in `ocr/TextExtractor.kt`.
- OCR returns structured result: `OCRResult(text, confidence)`.
- OCR screen displays extracted text and confidence.
- User can edit text before saving.

### Persistence
- Room database with `DocumentEntity`, `DocumentDao`, and `TextLexiqDatabase`.
- Repository saves OCR output and exposes recent documents.
- Home screen lists saved documents (basic summary).

---

### Document Viewing & Editing
- [x] Document view screen loads by ID and displays content.
- [x] Document editing mode implemented with save/discard.
- [x] Metadata fields added (source, engine, language, tags).
- [x] `DocumentDao` and Repository updated for CRUD operations.

### Exporting
- [x] PDF export implemented using iText7.
- [x] DOCX export implemented using Apache POI.
- [x] LaTeX code generation added.
- [x] Export action menu in UI with format selection.

### OCR Enhancements
- [x] Advanced OpenCV preprocessing: Deskewing (minAreaRect), Binarization (Adaptive Threshold), Grayscale.
- [x] Text block sorting in `TextExtractor` for reliable reading order.
- [x] Skew correction implemented.

### LLM Optimization
- [x] `TokenOptimizer`: Whitespace compression and stopword removal.
- [x] `SmartModelRouter`: Floating access logic (On-Device vs Cloud).
- [x] `LLMEngine` architecture defined.

---

## ⚠️ Partially Implemented / Needs Hardening

### OCR & Confidence
- Confidence is averaged from blocks. Visual overlays missing.
- Tesseract (tess-two) implementation optional/pending.
- Auto-language detection pending.

### Error Handling
- Basic errors surfaced in UI, but no retry strategies or offline recovery.

---

## 🚧 Remaining Work (Detailed)

### 1) OCR Future Work
- Option to switch between ML Kit and Tesseract (tess-two).
- Support multiple languages and auto-language detection.
- Add visual OCR overlays for confidence highlighting.


### 2) LLM Integration [DONE]
- [x] Design `LLMEngine` interface (abstraction for On-Device vs Cloud).
- [x] Implement `TokenOptimizer` (Stopword removal, context compression).
- [x] Implement `SmartModelRouter` (Floating access: Route based on complexity/cost).
- [x] Implement `LlamaCppClient` (On-Device Tier).
- [x] Implement `CloudLLMClient` (Paid Tier).
- [x] Integrate LLM features into UI (Summarize, Simplify).

### 3) Scanner UX
- Implement auto edge detection interface (OpenCV contors logic exists, need UI overlay).
- Auto capture when page is stable inside the guide.
- Manual crop corners (drag handles) instead of slider-only crop.

### 6) UI & State
- Create a unified app state (loading/saving indicators, error toasts/snackbars).
- Add settings screen bindings to actual config/state.
- Add onboarding/permissions screen.

### 7) Testing & Stability
- Unit tests for preprocessing and repository layers.
- Instrumentation tests for CameraX and OCR flows.
- Validate OpenCV initialization on device (not just debug).

### 8) Cross-Platform & Server (Future - Daytona/LeanMCP)
- Create `TextLexiq Server` (Python) for Daytona/LeanMCP.
- Port OpenCV/OCR pipeline to Python (pip package).
- Create `textlexiq-js` client (npm package).
- Deploy MCP Server to LeanMCP.

---

## Notes
- Current implementation uses ML Kit for OCR. If you prefer tess-two, a new implementation can be added in `ocr/TextExtractor.kt` behind an interface.
- OpenCV initialization is performed in `TextLexiqApp` and may need runtime loading for production builds.
