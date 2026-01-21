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

## ⚠️ Partially Implemented / Needs Hardening

### OCR & Confidence
- Confidence is averaged from text blocks (ML Kit only supports block-level confidence).
- No line/word-level confidence or bounding boxes surfaced in UI.

### Document Viewing
- Document view screen is still placeholder (no load by ID).
- No editing or re-export from stored documents.

### Error Handling
- Basic errors surfaced in UI, but no retry strategies or offline recovery.

---

## 🚧 Remaining Work (Detailed)

### 1) OCR Enhancements
- Option to switch between ML Kit and Tesseract (tess-two) with language packs.
- Support multiple languages and auto-language detection.
- Add visual OCR overlays for confidence highlighting.

### 2) Document Pipeline & Storage
- Update `DocumentViewScreen` to load by ID and show saved document content.
- Support updating an existing document (edit + save).
- Add metadata fields: source image path, OCR engine, language, tags.

### 3) Exporting
- Implement PDF export using iText.
- Implement DOCX export using Apache POI.
- Allow share/export actions from document detail screen.

### 4) LLM Integration
- Add `llm` module plumbing with llama.cpp JNI wrapper.
- Summarize, extract keywords, generate outlines from OCR text.
- Toggle on-device vs cloud LLM in settings.

### 5) Scanner UX
- Auto edge detection overlay using real-time frame analysis.
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

---

## Notes
- Current implementation uses ML Kit for OCR. If you prefer tess-two, a new implementation can be added in `ocr/TextExtractor.kt` behind an interface.
- OpenCV initialization is performed in `TextLexiqApp` and may need runtime loading for production builds.
