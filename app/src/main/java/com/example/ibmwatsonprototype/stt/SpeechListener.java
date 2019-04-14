package com.example.ibmwatsonprototype.stt;

public interface SpeechListener {
    void onSpeechResult(String result);
    void onSpeechError(Exception error);
    void onSpeechStop();
}
