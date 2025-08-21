package com.example.voicekeyboard;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VoiceInputMethodService extends InputMethodService {

    private MediaRecorder mediaRecorder;
    private File audioFile;
    private Button recordButton;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateInputView() {
        View keyboardView = getLayoutInflater().inflate(R.layout.keyboard_view, null);
        recordButton = keyboardView.findViewById(R.id.recordButton);

        recordButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startRecording();
                    return true;
                case MotionEvent.ACTION_UP:
                    stopRecording();
                    return true;
            }
            return false;
        });

        return keyboardView;
    }

    private void startRecording() {
        // app would request permission here. We assume it's granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Recording permission is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        recordButton.setText("Recording...");

        // Set up the file to save the recording
        audioFile = new File(getFilesDir(), "recording.3gp");

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(audioFile.getAbsolutePath());

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            // Immediately stop audio recording
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            // Show processing/loading state to user
            recordButton.setText("Processing...");

            // Send the complete audio file to the API
            transcribeAudioFile();
        }
    }

    private void transcribeAudioFile() {
        if (audioFile == null || !audioFile.exists()) {
            resetButtonState("Recording Failed");
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.groq.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GroqApiService apiService = retrofit.create(GroqApiService.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/3gp"), audioFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", audioFile.getName(), requestFile);
        RequestBody model = RequestBody.create(MediaType.parse("text/plain"), "whisper-large-v3");


        String apiKey = "Bearer YOUR_GROQ_API_KEY";

        apiService.transcribe(apiKey, body, model).enqueue(new Callback<TranscriptionResponse>() {
            @Override
            public void onResponse(Call<TranscriptionResponse> call, Response<TranscriptionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // On success, insert the transcribed text
                    String transcribedText = response.body().getText();
                    insertText(transcribedText);
                } else {
                    // Handle API errors gracefully
                    resetButtonState("API Error");
                }
            }

            @Override
            public void onFailure(Call<TranscriptionResponse> call, Throwable t) {
                // Handle network errors gracefully
                resetButtonState("Network Error");
                t.printStackTrace();
            }
        });
    }

    private void insertText(String text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            // Insert it at the current cursor position
            ic.commitText(text, 1);

            // Show brief confirmation and return to idle state
            uiHandler.post(() -> recordButton.setText("Inserted!"));
            uiHandler.postDelayed(() -> resetButtonState("Hold to Record"), 1500); // 1.5-second delay
        }
    }

    private void resetButtonState(String message) {
        uiHandler.post(() -> recordButton.setText(message));
    }
}