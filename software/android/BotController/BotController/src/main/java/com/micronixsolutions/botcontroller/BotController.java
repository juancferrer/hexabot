
package com.micronixsolutions.botcontroller;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import com.micronixsolutions.botcontroller.views.JoystickView;
import com.micronixsolutions.botcontroller.views.JoystickView.OnJoystickMoveListener;

import java.lang.Math;

public class BotController extends IOIOActivity implements OnJoystickMoveListener, SeekBar.OnSeekBarChangeListener{

    private static final String TAG = "BotControllerActivity";
    private JoystickView mJoystick;
    private SeekBar mLeftTrack;
    private SeekBar mRightTrack;
    private Handler mToastMessageHandler;
    private BotControllerLooper mBotLooper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_controller);

        //Create the handler than will receive messages from the IOIO thread
        //IOIO thread will send messages indicating the status.
        mToastMessageHandler = new Handler(){
            public void handleMessage(Message msg) {
                Log.d(TAG, "handleMessage: toast");
                //Show a toast message with the given text from the IOIO thread
                Bundle bundle = msg.getData();
                String text = bundle.getString("msg", "ERROR");
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }

        };

        mBotLooper = new BotControllerLooper(mToastMessageHandler);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            //Use joystick
            mJoystick = (JoystickView) findViewById(R.id.joystickView);
            mJoystick.setOnJoystickMoveListener(this);
        }
        else{
            //Use tank tracks
            mLeftTrack = (SeekBar) findViewById(R.id.leftTrack);
            mLeftTrack.setOnSeekBarChangeListener(this); //Send command
            mRightTrack = (SeekBar) findViewById(R.id.rightTrack);
            mRightTrack.setOnSeekBarChangeListener(this);
        }
    }
            
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        super.onNewIntent(intent);
    }

    @Override
    protected IOIOLooper createIOIOLooper() {
        // TODO Auto-generated method stub
        Log.d(TAG, "createIOIOLooper");
        return mBotLooper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_bot_controller, menu);
        return true;
    }

    public void onJoystickValueChanged(int angle, int power, int direction, int xPosition, int yPosition) {
        int leftMode = (Math.abs(angle) < 90) ? 0:1; //Forward if <90 else, reverse
        int rightMode = (Math.abs(angle) < 90) ? 0:1; //Forward if <90 else, reverse
        int leftPower = power;
        int rightPower = power;
        if(angle>=0 && angle<=90){
            //Top right quadrant, both motors forward, subtract power from right motor
            rightPower=(int)Math.round(Math.cos(Math.toRadians(angle)) * power);
        }
        else if(angle>90){
            //Bottom right quadrant both motors reverse subtract power from right motor
            rightPower=(int)Math.abs(Math.round(Math.cos(Math.toRadians(angle)) * power));
        }
        else if(angle<0 && angle>=-90){
            //Top left quadrant both motors forward subtract power from left motor
            leftPower=(int)Math.round(Math.cos(Math.abs(Math.toRadians(angle))) * power);
        }
        else if(angle<-90){
            //Bottom left quadrant both motors reverse subtract power from left motor
            leftPower=(int)Math.abs(Math.round(Math.cos(Math.abs(Math.toRadians(angle))) * power));
        }
        Log.d(TAG, "GONNA SEND: " + leftMode + " " + leftPower + " " + rightMode + " " + rightPower);
        mBotLooper.sendCommand(leftMode, leftPower, rightMode, rightPower);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        //Read both seekbars, and send the command
        int leftMode, rightMode, leftPower, rightPower = 0;
        if(mLeftTrack.getProgress() > mLeftTrack.getMax()/2){
            //Going forward
            leftMode = 0;
            leftPower = mLeftTrack.getProgress()-(mLeftTrack.getMax()/2);
        }
        else{
            //Going reverse
            leftMode = 1;
            leftPower = Math.abs(mLeftTrack.getProgress()-(mLeftTrack.getMax()/2));
        }

        if(mRightTrack.getProgress() > mRightTrack.getMax()/2){
            //Going forward
            rightMode = 0;
            rightPower = mRightTrack.getProgress()-(mRightTrack.getMax()/2);
        }
        else{
            //Going reverse
            rightMode = 1;
            rightPower = Math.abs(mRightTrack.getProgress()-(mRightTrack.getMax()/2));
        }
        Log.d(TAG, "GONNA SEND: " + leftMode + " " + leftPower + " " + rightMode + " " + rightPower);
        mBotLooper.sendCommand(leftMode, leftPower, rightMode, rightPower);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //Set the seekbar back to the middle
        seekBar.setProgress(seekBar.getMax()/2);
    }
}
