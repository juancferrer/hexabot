
package com.micronixsolutions.botcontroller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import com.micronixsolutions.botcontroller.views.JoystickView;
import com.micronixsolutions.botcontroller.views.JoystickView.OnJoystickMoveListener;

import java.lang.Math;

public class BotController extends IOIOActivity {

    private static final String TAG = "BotControllerActivity";
    private TextView mAngleTextView;
    private TextView mPowerTextView;
    private JoystickView mJoystick;
    private Handler mToastMessageHandler;
    private BotControllerLooper mBotLooper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_controller);
        mAngleTextView = (TextView) findViewById(R.id.angleText);
        mPowerTextView = (TextView) findViewById(R.id.powerText);
        mJoystick = (JoystickView) findViewById(R.id.joystickView);
       
        mJoystick.setOnJoystickMoveListener(new OnJoystickMoveListener(){
            @Override
            public void onValueChanged(int angle, int power, int direction, int xPosition, int yPosition) {
                mAngleTextView.setText(String.valueOf(angle));
                mPowerTextView.setText(String.valueOf(power) + "%");
                int leftMode = (Math.abs(angle) < 90) ? 0:1; //Forward if <90 else, reverse
                int rightMode = (Math.abs(angle) < 90) ? 0:1; //Forward if <90 else, reverse
                int leftPower = power;
                int rightPower = power;
                if(angle>=0 && angle<=90){
                    //Top right quadrant, both motors forward, subtract power from right motor
                    //double scaledAngle = Math.floor(256 * Math.abs(angle) / (90 + 1));
                    //double scaledPower = Math.floor(256 * Math.abs(scaledAngle) / (90 + 1));
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
            }
        );
       
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

}
