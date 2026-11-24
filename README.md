# Vathos – Private On-Device AI Assistant

Vathos is an Android AI assistant designed to run language-model inference locally on the device. It combines a compact Qwen3 model with local document retrieval and productivity tools, with a focus on keeping conversations and document processing on-device after the required models have been downloaded.

> **Project status:** Personal Android project. Core device tests have been completed by the developer, but release signing, a full security review, and production release validation are still separate steps.

## Features

- **On-device AI chat:** Qwen3-0.6B INT4 through LiteRT-LM.
- **PDF question answering:** Extracts and chunks PDF text, then retrieves relevant passages for grounded answers.
- **Semantic document retrieval:** Uses EmbeddingGemma to find relevant passages beyond exact keyword matches.
- **Local productivity tools:** Supports actions such as creating notes and tasks, and searching or summarizing documents.
- **Offline use:** Chat and local document workflows can run after the required models and documents are available on the device.
- **Model management:** Downloads and stores the language and embedding models in app-private storage.
- **Local persistence:** Uses Room for app data and DataStore for preferences.

## Technology stack

- Kotlin
- Jetpack Compose and Material 3
- MVVM and Clean Architecture
- LiteRT-LM
- Qwen3-0.6B INT4
- MediaPipe Tasks Text / EmbeddingGemma
- Room
- DataStore
- Kotlin Coroutines and Flow
- Android PdfRenderer / PDF text extraction

## Architecture

```text
Jetpack Compose UI
        |
        v
    ViewModels
        |
        v
Domain use cases
   |          |
   v          v
Repositories  Local AI
   |          |
   v          v
Room       LiteRT-LM
Database   Qwen3-0.6B
              |
              v
       EmbeddingGemma
       (semantic retrieval)
```

## Models

Vathos uses two separate local models:

| Model | Purpose | Download |
|---|---|---|
| Qwen3-0.6B INT4 | Chat and text generation | [Hugging Face model repository](https://huggingface.co/litert-community/Qwen3-0.6B-int4) |
| EmbeddingGemma | Semantic document retrieval | [Google-hosted model file](https://storage.googleapis.com/mediapipe-models/text_embedder/embedding_gemma/int4int8/latest/embedding_gemma.task) |

The Qwen3 model file used by the app is approximately 332 MB. The embedding model is an additional download. Initial model downloads require an internet connection.

## Build and run

1. Install Android Studio with a compatible Android SDK.
2. Clone this repository.
3. Open the project in Android Studio and allow Gradle sync to finish.
4. Connect an Android device or start an emulator.
5. Build and run the `app` configuration.
6. Download the required models from the app's Model Manager while internet access is available.
7. After both models are downloaded, test chat and document workflows in Airplane Mode.

> Exact SDK and Gradle requirements are defined by the project configuration. They may change as the project is updated.

## Privacy and offline behavior

- Model inference is performed locally using the downloaded on-device models.
- PDF processing and retrieval are designed to use local document data.
- The app's Offline Mode policy guards the Qwen and EmbeddingGemma model-download paths.
- Internet access is needed for the initial model downloads.
- The app currently retains Android's `INTERNET` permission for model downloads.

**Important limitation:** Offline Mode and local inference should not be interpreted as a verified system-wide network firewall or a guarantee of zero bytes transmitted. The in-app traffic display measures app-UID network traffic deltas; it does not identify destinations or prove that no data was sent to a server. A complete network audit is still required before making stronger privacy claims.

## Screenshots

Add screenshots from the running app here:

| Chat | Documents |
|---|---|
| `![Chat screenshot](screenshots/chat.png)` | `![Documents screenshot](screenshots/documents.png)` |

| Model Manager | Settings |
|---|---|
| `![Model Manager screenshot](screenshots/model-manager.png)` | `![Settings screenshot](screenshots/settings.png)` |

## Current verification

The developer has tested core flows on a physical Android device, including local chat, PDF question answering and semantic retrieval, note/task actions, model persistence after restart, and chat operation in Airplane Mode after model download.

These are developer-run functional checks, not a substitute for automated test coverage, a security audit, or release-build verification.

## License

No repository license has been added yet. Check the licenses and attribution requirements for the source code, dependencies, and model assets before distributing the project.
