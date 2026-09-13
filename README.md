# MYRAA AI ASSISTANT

> **Native Android AI Assistant for realme 12 Pro 5G**  
> Primary User: **Piyush**

---

## 🌟 Overview

**MYRAA** is a real native Android AI assistant crafted in Kotlin and Jetpack Compose. Built specifically for the **realme 12 Pro 5G** (Android 14+), MYRAA provides low-latency conversational AI with full on-device phone control, persistent memory via Room Database, multimodal Camera Vision via CameraX, and Google Gemini reasoning.

---

## ✨ Key Capabilities

1. **Voice-First Interaction (Hindi / Hinglish / English)**:
   - Natural, affectionate, witty, and loyal assistant personality.
   - Live speech recognition and dynamic audio waveform visualizer.
   - TextToSpeech audio output with speech rate and pitch tuning.

2. **Real Phone Control**:
   - **App Launching**: WhatsApp, YouTube, Chrome, Instagram, Maps, Dialer, Camera.
   - **Hardware Control**: Flashlight toggle, system volume up/down, media play/pause/skip.
   - **System Shortcuts**: Wi-Fi, Bluetooth, Display, Sound, Battery, Apps settings.
   - **Device Telemetry**: Battery % & charging status, network connection checks.
   - **System Timers**: Create alarms and timers directly in Android.

3. **Gemini AI Integration**:
   - High-performance reasoning with `gemini-3.5-flash`.
   - Multimodal Camera Vision analysis using CameraX.
   - Graceful offline fallback to local actions when offline.

4. **Memory Vault (Room DB)**:
   - On-device local storage for preferences, notes, tasks, and conversation context.
   - Full user control: search, add, review, and clear all memory.

5. **Safety & Privacy Core**:
   - Explicit confirmation dialog for medium and high-risk actions.
   - Zero background secret recording or surveillance.
   - Strict command blocklist prevents dangerous root or destructive scripts.

6. **realme 12 Pro 5G Optimization**:
   - Custom guides to configure Auto-Launch, unrestricted background battery permissions, and recent apps lock.

7. **Developer Copilot Mode**:
   - Built-in technical companion for Android, Kotlin, Jetpack Compose, Coroutines, and architecture consultations.

---

## 🚀 Setup & Build

1. Clone repository:
   ```bash
   git clone https://github.com/example/myraa-ai-assistant.git
   ```
2. Configure Gemini API key in `.env`:
   ```properties
   GEMINI_API_KEY=your_actual_gemini_api_key
   ```
3. Build with Gradle:
   ```bash
   gradle assembleDebug
   ```

---

## 🛡️ License

Distributed under the MIT License. See `LICENSE` for more information.
