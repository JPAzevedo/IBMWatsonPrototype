package com.example.ibmwatsonprototype.stt;

import android.app.Activity;

import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SpeechWorker {

    private static SpeechWorker instance;
    private static Activity context;
    private static MicrophoneHelper microphoneHelper;
    private MicrophoneInputStream capture;

    public static SpeechWorker getInstance(Activity context) {
        if (instance == null) {
            synchronized (SpeechWorker.class) {
                if (instance == null) {
                    instance = new SpeechWorker(context);
                }
            }
        }

        return instance;
    }

    private SpeechWorker(Activity context) {
        this.context = context;
        this.microphoneHelper = new MicrophoneHelper(context);
    }

    public SpeechToText initSpeechToTextService() {
        return SpeechConfiguration.getInstance().initSpeechToTextService(context);
    }

    public void startListening(final SpeechToText speechService, final SpeechListener listener, final SpeechConfiguration.Model model, final boolean smartFormat) {
        capture = microphoneHelper.getInputStream(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    speechService.recognizeUsingWebSocket(SpeechConfiguration.getInstance().getRecognizeOptions(capture, model,smartFormat),
                            new MicrophoneRecognizeDelegate(listener));
                } catch (Exception e) {
                    showError(e, listener);
                }
            }
        }).start();
    }

    public void stopListening(final SpeechListener listener) {
        if (capture == null) {
            return;
        }

        try {
            capture.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        listener.onSpeechStop();
    }

    private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback {

        private SpeechListener listener;
        private Boolean listening = false;
        private final Integer rate = 2000;
        private Timer threadStopper;
        private boolean threadStopperFlag = false;

        public MicrophoneRecognizeDelegate(final SpeechListener listener) {
            this.listener = listener;
            threadStopper = new Timer();
        }

        @Override
        public void onTranscription(SpeechRecognitionResults speechResults) {
            System.out.println(speechResults);
            if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {

                synchronized (listening) {
                    listening = true;
                }

                String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                listener.onSpeechResult(text);
            }

            if (!threadStopperFlag) {
                startThreadStopper();
            }
        }

        @Override
        public void onError(Exception e) {
            stopListening(e);
        }

        @Override
        public void onDisconnected() {
            stopListening(null);
        }

        @Override
        public void onInactivityTimeout(RuntimeException runtimeException) {
            super.onInactivityTimeout(runtimeException);
            stopListening(null);
        }

        private void stopListening(Exception e) {
            try {
                // This is critical to avoid hangs
                // (see https://github.com/watson-developer-cloud/android-sdk/issues/59)
                capture.close();
                listener.onSpeechStop();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            if (e != null) {
                showError(e, listener);
            }


            threadStopperFlag = false;
            stopThreadStopper();
        }

        private void startThreadStopper() {
            threadStopper.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (!listening) {
                        synchronized (listening) {
                            stopListening(null);
                        }
                    } else {
                        synchronized (listening) {
                            listening = false;
                        }
                    }
                }
            }, rate, rate);

            threadStopperFlag = true;
        }

        private void stopThreadStopper() {
            threadStopper.cancel();
        }
    }

    private void showError(final Exception e, final SpeechListener listener) {
        listener.onSpeechError(e);
    }
}
