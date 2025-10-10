package com.noohu.zcrosstest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.noohu.zcrosstest.Services.ADCBindService;
import com.noohu.zcrosstest.Services.FanControlBindService;
import com.noohu.zcrosstest.Services.LEDBindService;
import com.noohu.zcrosstest.Services.MicBindService;
import com.noohu.zcrosstest.Services.PIRBindService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vendor.zumi.adc.IAdcService;
import vendor.zumi.fancontrol.ITemperatureFanService;
import vendor.zumi.ledcontrol.ILEDService;
import vendor.zumi.miccontroller.IMicController;
import vendor.zumi.micrecorder.IMicRecorder;
import vendor.zumi.pircontrol.IPIRService;

public class MainActivity extends AppCompatActivity {
    private Button ADC_test, recordButton, Temp_test, Camer_test, Led_test, PIR_starttest, PIR_stoptest;
    private TextView AvgTmp_txt, PIR_status;
    private EditText led_edtx;
    private Spinner ChannelSpinner;

    public String selectedChannelNum = "Channel-1";
    private boolean isRecording = false;
    private File recordFile = null;
    private MediaRecorder recorder = null;

    /*Binds controller service*/
    private IAdcService iADC = null;
    private ITemperatureFanService iTmp = null;

    private ILEDService iLed = null;
    private IMicController iMic = null;
    private IPIRService iPIR = null;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iMic = MicBindService.getBind();
        try {
            boolean muted = iMic.isMuted();
            Toast.makeText(getApplicationContext(), muted?"volume muted":"volume Not muted", Toast.LENGTH_LONG).show();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        /*ADC TEST*/
        ADC_test = findViewById(R.id.ADC_test);
        ListView adcListView = findViewById(R.id.adcListView);


        ADC_test.setOnClickListener(v -> {
            iADC = ADCBindService.getBind();
            List<String> adcChannels = null;
            try {
                adcChannels = iADC.readAdcChannels();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    adcChannels
            );
            adcListView.setAdapter(adapter);


        });



        /*Audio Controller*/

        ChannelSpinner = findViewById(R.id.micSpinner);
        recordButton = findViewById(R.id.recordButton);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }


        ChannelSelection();
        recordButton.setOnClickListener(v -> {


            // Get current timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

            // Create file name with timestamp
            String fileName = "recorded_" + timeStamp + ".wav";

            // File path in Downloads folder
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            String path = file.getAbsolutePath();
            Log.d("ZumiTesting", "isRecording clicked");

            if (!isRecording) {
                String[] parts = selectedChannelNum.split("-");

                int Channelnumber = Integer.parseInt(parts[1]);

                Log.d("ZumiTesting", "isRecording if");
                recorder = new MediaRecorder();
                Log.d("ZumiTesting", "recorde itialized");

                int[] channelConfigs = {AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO};

                for (int config : channelConfigs) {
                    int bufferSize = AudioRecord.getMinBufferSize(48000, config, AudioFormat.ENCODING_PCM_32BIT);
                    if (bufferSize > 0) {
                        Log.d("ZumiTesting", "Supported channel config: " + config);
                    }
                }

                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                Log.d("ZumiTesting", "Mic setting");

                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                Log.d("ZumiTesting", "OutputFormat MPEG_4 setting");

                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                Log.d("ZumiTesting", "AudioEncoder AAC setting");
                try {
                    iMic.setMicfilDataline( Channelnumber);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                //recorder.setAudioChannels(Channelnumber);
                Log.d("ZumiTesting", "setAudioChannels selection setting");

                recorder.setAudioSamplingRate(48000);
                Log.d("ZumiTesting", "setAudioSamplingRate 48000 setting");

                recorder.setAudioEncodingBitRate(32);
                Log.d("ZumiTesting", "setAudioEncodingBitRate 32 setting");

                recorder.setMaxDuration(-1); // unlimited duration
                Log.d("ZumiTesting", "setMaxDuration unlimited setting");

                recorder.setOutputFile(path);
                Log.d("ZumiTesting", "setOutputFile  setting");


                try {
                    recorder.prepare();
                    Log.d("ZumiTesting", " recorder.prepare();  setting");

                    recorder.start();
                    Log.d("ZumiTesting", " recorder.start();  setting");

                    isRecording = true;

                } catch (IOException | IllegalStateException e) {
                    Log.e("ZumiTesting", "Recorder failed: " + e.getMessage());

                }

                /*String[] parts = selectedChannelNum.split("-");
                if (parts.length == 2) {
                    int Channelnumber = Integer.parseInt(parts[1]);
                    try {

                        iMic.recordMicrophone(path, 0, Channelnumber, 48000, 32, 5);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }*/

                recordButton.setText("Stop Recording");
            } else {
               /* try {
                    iMic.stopRecording();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }*/

                if (recorder != null) {
                    try {
                        recorder.stop();
                        recorder.reset();
                        recorder.release();
                    } catch (IllegalStateException ignored) {
                    }
                    recorder = null;
                }
                isRecording = false;
                recordButton.setText("Start Recording");

            }

        });


        /*Temperature test*/
        Temp_test = findViewById(R.id.Temp_test);
        AvgTmp_txt = findViewById(R.id.AvgTmp_txt);
        ListView tmpListView = findViewById(R.id.tmpListView);
        iTmp = FanControlBindService.getBind();
        try {
            AvgTmp_txt.setText("controlFanBasedOnAverageTemperature:" + iTmp.controlFanBasedOnAverageTemperature());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }


        Temp_test.setOnClickListener(v -> {
            List<String> ItmpList = null;

            try {

                ItmpList = iTmp.readAllSensorTemperatures();

                List<String> formattedList = new ArrayList<>();
                if (ItmpList != null) {
                    for (String raw : ItmpList) {

                        String sensorNum = raw.split(" ")[1];

                        // Extract only the digits before °C
                        String tempMatch = "";
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)°C").matcher(raw);
                        if (m.find()) {
                            tempMatch = m.group(1);
                        }


                        String formatted = "Sensor " + sensorNum + " : " + tempMatch + "°C";
                        formattedList.add(formatted);


                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        formattedList
                );
                tmpListView.setAdapter(adapter);

            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });


        /*Camera test */

        Camer_test = findViewById(R.id.Camer_test);

        Camer_test.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraTest.class);
            startActivity(intent);
        });


        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);

        for (AudioDeviceInfo device : devices) {
            int type = device.getType();
            String name = device.getProductName().toString();
            Log.d("MicInfo", "Device Type: " + type + " Name: " + name);
        }
        /*Led test*/

        led_edtx = findViewById(R.id.led_edtx);
        Led_test = findViewById(R.id.Led_test);
        Led_test.setOnClickListener(v -> {
            if (!led_edtx.getText().toString().isEmpty()) {
                iLed = LEDBindService.getBind();

                try {
                    iLed.setDevicePath("/dev/ttyRPMSG0");
                    iLed.writeData(led_edtx.getText().toString().trim());
                    // String response = iLed.readData();
                    // Log.d("UART", "Got response: " + response);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }

            } else {
                Toast.makeText(this, "Please Enter the color code", Toast.LENGTH_LONG).show();
            }
        });

        /*PIR test */

        PIR_starttest = findViewById(R.id.PIR_starttest);
        PIR_stoptest = findViewById(R.id.PIR_stoptest);
        PIR_status = findViewById(R.id.PIR_status);

        PIR_starttest.setOnClickListener(v -> {
            iPIR = PIRBindService.getBind();

            try {
                // Open I²C bus 3, address 0x5A
                boolean opened = iPIR.openDevice(3, 0x5A);
                // Init PIR
                boolean init = iPIR.initPir();

                PirTestStart(true);

                // Debug read reg 0x25
                /*int val = pir.readRegister(0x25);
                Log.d("APP", "Reg 0x25 = " + val);*/

            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

        });


        PIR_stoptest.setOnClickListener(v -> {

            try {
                if (iPIR != null) {
                    // Close device when done
                    PirTestStart(false);

                    iPIR.closeDevice();
                } else {
                    Toast.makeText(this, "PIR test not able to close", Toast.LENGTH_LONG).show();

                }

            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

        });

    }


    /*Audio Controller*/
    private void ChannelSelection() {
        List<String> ChannelLabels = new ArrayList<>();
        ChannelLabels.add("Mic-0");
        ChannelLabels.add("Mic-1");
        ChannelLabels.add("Mic-2");
        ChannelLabels.add("Mic-3");
        ChannelLabels.add("Mic-4");
        ChannelLabels.add("Mic-5");
        ChannelLabels.add("Mic-6");
        ChannelLabels.add("Mic-7");
        ChannelLabels.add("Mic-8");


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ChannelLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ChannelSpinner.setAdapter(adapter);

        ChannelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedValue = parent.getItemAtPosition(position).toString();

                // Now assign to your variable
                selectedChannelNum = selectedValue;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedChannelNum = null;
            }
        });


    }


    private void PirTestStart(boolean StartTest) {
        // Run in a background thread so it doesn’t block the UI
        new Thread(() -> {
            while (StartTest) {

                // Update UI on the main thread

                runOnUiThread(() -> {
                    try {
                        PIR_status.setText(
                                iPIR.readPir() ? "PIR Status: Presence Detected" : "PIR Status: Presence Not Detected"
                        );
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }

                });


                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }
}