package com.example.voicekeyboard;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface GroqApiService {
    @Multipart
    @POST("openai/v1/audio/transcriptions")
    Call<TranscriptionResponse> transcribe(
            @Header("Authorization") String apiKey,
            @Part MultipartBody.Part file,
            @Part("model") RequestBody model
    );
}