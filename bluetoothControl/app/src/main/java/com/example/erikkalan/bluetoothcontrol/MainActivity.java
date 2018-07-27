package com.example.erikkalan.bluetoothcontrol;

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
//import androidRecyclerViw.MessageAdapter

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.String;
import java.lang.Runnable;
import android.view.View.OnTouchListener;
import java.nio.charset.StandardCharsets;
import android.view.View.OnClickListener;



public class MainActivity extends AppCompatActivity {
    ///*
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int BRAKE = 0;
    public final static int SPEED_FORWARD = 1;
    public final static int SPEED_REVERS = 2;
    public final static int TURN_LEFT = 3;
    public final static int TURN_RIGHT = 4;

    ///*
    public static final String BT_ADDRESS = "adress";
    public static final String KEY_TOAST = "toast";
    public static final String NAME = "Main_Activity";


    private MyBluetoothService bluetoothService = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private Handler handler = null;
    //private BluetoothDevice bluetoothDevice = ;

    // intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 1;

    // Name of connected device
    private String connectedDeviceName = null;
    private StringBuilder instruction;
    //private MessageAdapter messageAdapter;

    // min value to send speed
    private static final int MIN_SPEED_VALUE = 10;

    private Button mngBluetoothBtn, rightBtn,leftBtn, speedUpBtn, speedDwnBtn;
    private SeekBar speedSeekBar;
    private EditText speedConsEditText;
    private TextView instructionTextView, percentTextView;











    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initController();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null){
            Toast.makeText(this,"Bluetooth unavailabel", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled() ) {
            Toast.makeText(this,"Bluetooth is not turned on ", Toast.LENGTH_LONG).show();
            finish();
        }else {
            if (bluetoothService == null){
                setComunication();
            }
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if ( bluetoothService != null){
            if (bluetoothService.getState() == MyBluetoothService.STATE_NONE){
                bluetoothService.start();
            }
        }
    }


    private void setComunication() {
        bluetoothService = new MyBluetoothService(this, handler);

        instruction = new StringBuilder("");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (bluetoothService != null) {
            bluetoothService.stop();
        }

    }


    private void initController(){

        mngBluetoothBtn = (Button) findViewById(R.id.mngBluetoothBtn);
        rightBtn = (Button) findViewById(R.id.rightButton);
        leftBtn = (Button) findViewById(R.id.leftButton);
        speedUpBtn = (Button) findViewById(R.id.speedUpBtn);
        speedDwnBtn = (Button) findViewById(R.id.speedDwnBtn);
        speedSeekBar = (SeekBar) findViewById(R.id.speedSeekBar);
        percentTextView = (TextView) findViewById(R.id.percentTextView);
        speedConsEditText = (EditText) findViewById(R.id.speedConsEditText);
        instructionTextView = (TextView) findViewById(R.id.instructionTextView);

        //instructionTextView.setMovementMethod(new ScrollingMovementMethod());

        mngBluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), ManageBluetooth.class);
                startActivity(startIntent);
            }
        });


        /*
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int speedConstant = Integer.parseInt(speedConsEditText.getText().toString());
                writeInstruction(TURN_RIGHT, speedConstant);

            }
        });
        */




        rightBtn.setOnTouchListener(new RepeatListener(400, 100, new OnClickListener() {
            @Override
            public void onClick(View view) {
                // the code to execute repeatedly
            }
        }));

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int speedConstant = Integer.parseInt(speedConsEditText.getText().toString());
                writeInstruction(TURN_RIGHT, speedConstant);


            }
        });/**/



        speedUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                int progress = speedSeekBar.getProgress();
                int speedConstant = Integer.parseInt(speedConsEditText.getText().toString());
                int progressMax = speedSeekBar.getMax();
                int result = progress+speedConstant;

                if ((result) < progressMax){
                    speedSeekBar.setProgress(result);
                }else{
                    speedSeekBar.setProgress(progressMax);
                }

                setPercentTextView();
            }
        });



        speedDwnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                int progress = speedSeekBar.getProgress();
                int speedConstant = Integer.parseInt(speedConsEditText.getText().toString());
                int progressMax = speedSeekBar.getMax();
                int result = progress-speedConstant;

                if ((result) >= 0){
                    speedSeekBar.setProgress(result);

                }else{
                    speedSeekBar.setProgress(0);
                    result = 0;
                }


                setPercentTextView();

            }
        });


        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser){
                    setPercentTextView();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });





    }

    public void setPercentTextView(){


        String s = "";
        int direction = 0;

        int progressMax = speedSeekBar.getMax();
        int progressZero = progressMax / 2;

        int result = speedSeekBar.getProgress();

        int resultPercent = ((result - progressZero) * 100) / progressZero;

        // Clean up result and give easier zero spot for seekBar
        if (resultPercent > 100) {
            resultPercent = 100;
            percentTextView.setTextColor(getResources().getColor(R.color.myDarkBlue));
            writeInstruction(SPEED_FORWARD, resultPercent);

        }else if (resultPercent>= MIN_SPEED_VALUE) {
            percentTextView.setTextColor(getResources().getColor(R.color.myLiteBlue));
            writeInstruction(SPEED_FORWARD, resultPercent);

        }else if (resultPercent > MIN_SPEED_VALUE && resultPercent < -MIN_SPEED_VALUE) {
            resultPercent = 0;
            writeInstruction(BRAKE, resultPercent);
            percentTextView.setTextColor(getResources().getColor(R.color.myCharcoal));

        }else if (resultPercent <= -MIN_SPEED_VALUE) {
            percentTextView.setTextColor(getResources().getColor(R.color.myLiteRed));
            writeInstruction(SPEED_REVERS, resultPercent);

        } else if (resultPercent < -100){
            resultPercent = -100;
            writeInstruction(SPEED_REVERS, resultPercent);
            percentTextView.setTextColor(getResources().getColor(R.color.myDarkRed));
        }

        String percentString = "%" + resultPercent + " thrust ";
        percentTextView.setText(percentString);



    }



    public void writeInstruction(int direction, int value) {
        String seperator =  "  :  ";
        String end = "\n";
        String space = "  ";

        instruction = new StringBuilder("");
        instruction.append(direction).append(space).append(value).append(seperator).
                append(intToDirection(direction)).append(end);

        instructionTextView.append(instruction);
        /*
        final int scrollAmount = instructionTextView.getLayout().
                getLineTop(instructionTextView.getLineCount()) - instructionTextView.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0) {
            instructionTextView.scrollTo(0, scrollAmount);
        }else {
            instructionTextView.scrollTo(0, 0);
        }*/

        byte [] bytes = new byte[2];
        bytes[0] = (byte) direction;
        bytes[1] = (byte) value;

        bluetoothService.write(bytes);

    }

    public String instructionToBytes (byte[] bytes){

        return null;
    }

    private String intToDirection(int b){
        switch (b){
            case 0:
                return "Brake";
            case 1:
                return "Forward";
            case 2:
                return "Reverse";
            case 3:
                return "Left";
            case 4:
                return "Right";
            case 5:
                return "Yaw Duration";
            case 6:
                return "Yaw Const";
            default :
                return "Error incorrect 1st instruction";

        }

    }

    private String secondInstruction(byte b){
        int a = b;
        return Integer.toString(a);
    }




}
