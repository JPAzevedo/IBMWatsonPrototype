package com.example.ibmwatsonprototype.stt;

import android.content.Context;

import com.example.ibmwatsonprototype.R;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;

import java.io.InputStream;

public class SpeechConfiguration {

    private static SpeechConfiguration instance;

    public enum Model {
        PT_BR("pt-BR_NarrowbandModel"),
        EN_UK ("en-GB_NarrowbandModel"),
        EN_US("en-US_NarrowbandModel");

        private final String model;

        Model(String s) {
            model = s;
        }

        public String getModel(){
            return model;
        }

    }

    private SpeechConfiguration(){}

    public static SpeechConfiguration getInstance(){
        if(instance == null){
            synchronized (SpeechConfiguration.class){
                if(instance == null){
                    instance = new SpeechConfiguration();
                }
            }
        }

        return instance;
    }

    public SpeechToText initSpeechToTextService(Context context) {
        SpeechToText service = new SpeechToText();
        String apiKey = context.getString(R.string.speech_text_iam_apikey);
        String url = context.getString(R.string.speech_text_url);
        service.setUsernameAndPassword("apikey", apiKey);
        service.setEndPoint(url);
        return service;
    }

    public RecognizeOptions getRecognizeOptions(InputStream captureStream, Model model,boolean smartFormat) {
        return new RecognizeOptions.Builder()
                .audio(captureStream)
                .contentType(ContentType.OPUS.toString())
                .model(model.getModel())
                .interimResults(true)
                .inactivityTimeout(5)
                .smartFormatting(smartFormat)
                .build();
    }
}
