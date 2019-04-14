package com.example.ibmwatsonprototype;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibmwatsonprototype.stt.SpeechConfiguration;
import com.example.ibmwatsonprototype.stt.SpeechListener;
import com.example.ibmwatsonprototype.stt.SpeechWorker;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;

public class TranscriptionActivity extends AppCompatActivity implements View.OnClickListener, SpeechListener {

    private Toolbar toolbar;
    private ImageButton ibMic;
    private TextView tvResult;
    private RadioGroup rgTranscriptionStrategy;
    private RadioGroup rgLanguage;
    private final static String DEBUG_TAG = "TranscriptionApp";
    private final static int RECORD_REQUEST_CODE = 101;
    private boolean permissionToRecord = false;
    private SpeechToText sttService;
    private SpeechWorker apiInstance;
    private boolean listening = false;
    private static int transcrptionStrategy;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcript);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ibMic = (ImageButton) findViewById(R.id.ibMic);
        tvResult = (TextView) findViewById(R.id.tvTranscriptionRes);
        rgTranscriptionStrategy = (RadioGroup) findViewById(R.id.rgTranscriptionStrategy);
        rgLanguage = (RadioGroup) findViewById(R.id.rgLanguage);

        initUserInterface();
    }

    private void initUserInterface(){
        toolbar.setTitle(R.string.app_name);
        ibMic.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    private void checkPermissions(){
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.d(DEBUG_TAG, "Permission to record denied");
            makeRequest();
        }
        else{
            permissionToRecord = true;
        }
    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case RECORD_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(DEBUG_TAG, "Permission to record granted");
                    permissionToRecord = true;
                } else {
                    Log.d(DEBUG_TAG, "Permission to record denied");
                    permissionToRecord = false;
                }
                return;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ibMic){
            if(!permissionToRecord){
                Toast.makeText(this,"To use this feature you need to grant access first", Toast.LENGTH_LONG).show();
                return;
            }

            int viewStrategy = rgTranscriptionStrategy.getCheckedRadioButtonId();
            int viewLanguage = rgLanguage.getCheckedRadioButtonId();
            setTranscrptionStrategy(viewStrategy);
            manageTranscriptions(v,getModel(viewLanguage),isSmartFormat(viewStrategy));
        }
    }

    private void setTranscrptionStrategy(int id){
        transcrptionStrategy = id;
    }

    private SpeechConfiguration.Model getModel(int id){
        switch (id) {
            case R.id.rbUs_uk:
                return SpeechConfiguration.Model.EN_UK;
            case R.id.rbUs_us:
                return SpeechConfiguration.Model.EN_US;
            case R.id.rbPt_br:
                return SpeechConfiguration.Model.PT_BR;
            default:
                return SpeechConfiguration.Model.EN_US;
        }
    }

    private boolean isSmartFormat(int id){
        if(id == R.id.rbAlphanumeric){
            return true;
        }

        return false;
    }

    private void manageTranscriptions(final View view,SpeechConfiguration.Model model, boolean smartFormat){

        if(apiInstance == null){
            apiInstance = SpeechWorker.getInstance(TranscriptionActivity.this);
        }

        if(sttService == null){
            sttService = apiInstance.initSpeechToTextService();
        }

        if (!listening) {
            // Update the icon background
           startFlickerEffect(view);
            listening = true;
            apiInstance.startListening(sttService,this,model,smartFormat);

        } else {
            // Update the icon background
            stopFlickerEffect(view);
            listening = false;
            apiInstance.stopListening(this);
        }

    }

    private void startFlickerEffect(final View view){
        view.setSelected(true);
    }

    private void stopFlickerEffect(final View view){
        view.setSelected(false);
        //view.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onSpeechResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isSmartFormat(transcrptionStrategy)){
                    tvResult.setText(result.replaceAll("[^a-zA-Z0-9_-]", ""));
                }
                else{
                    tvResult.setText(result);
                }
            }
        });
    }

    @Override
    public void onSpeechError(final Exception error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                error.printStackTrace();
                // Update the icon background
                ibMic.setSelected(false);
                listening = false;
            }
        });
    }

    @Override
    public void onSpeechStop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ibMic.setSelected(false);
                listening = false;
            }
        });
    }
}
