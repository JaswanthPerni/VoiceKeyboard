# Voice Keyboard for Android

This is a custom Android keyboard I built for a technical assessment. It replaces the standard QWERTY layout with a single button, allowing you to use your voice as the primary input method. Just press and hold, say what you want to type, and the transcribed text appears in any app you're using.

### How It Works

The workflow is designed to be simple and intuitive. You press and hold the button to start recording audio, and it continues for as long as you hold it down. The moment you release the button, the recording stops immediately. The app then sends the complete audio file—not a live stream—to Groq's Whisper API for transcription. Once the text comes back, it's automatically inserted wherever your cursor is. The button provides visual feedback throughout the process so you know if it's recording, processing, or done.

### Setup and Implementation Guide

Getting this project running on your own device is straightforward.

1.  **Clone & Open:** First, clone this repository and open it as a new project in Android Studio.

2.  **Add Your API Key:** This is the most important step. Navigate to `app/src/main/java/com/example/voicekeyboard/VoiceInputMethodService.java`. Inside this file, find the line below and replace `"YOUR_GROQ_API_KEY"` with your actual key from Groq.

    ```java
    String apiKey = "Bearer YOUR_GROQ_API_KEY";
    ```

3.  **Build the App:** Build the project in Android Studio and run it on an emulator or a physical Android device.

4.  **Enable the Keyboard:** Since this is a custom input method, you need to enable it in your device's settings.
    * Go to `Settings > System > Languages & input`.
    * Tap `On-screen keyboard` > `Manage on-screen keyboards`.
    * Find the **Java Voice Keyboard** in the list and turn it on.

5.  **Use It!** Open any app with a text field. When the keyboard appears, tap the small keyboard icon (usually in the bottom-right corner) and switch your input method to the **Java Voice Keyboard**. Now you're all set to press and hold the button to type with your voice!

**Tech Used:** This was built natively for Android using **Java**. It uses Android's `InputMethodService` for the keyboard functionality, `MediaRecorder` for audio capture, and **Retrofit** to communicate with the **Groq Whisper API**.
