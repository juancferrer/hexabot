package com.micronixsolutions.botcontroller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;

class BotControllerLooper extends BaseIOIOLooper {
	private DigitalOutput led_;
	private PwmOutput mPwmled;

    private static final String TAG = "Looper";
    private static final int RX_PIN = 6;
    private static final int TX_PIN = 7;
    private Handler mToastQueue;
    private Handler mCommandQueue;
    private Uart mUart;
    private InputStream mIn;
    private OutputStream mOut;
    private Boolean mCommandReadyFlag;
    private byte[] mLastCommandReceived;
    private byte[] mReadBuffer;
    
    public BotControllerLooper(Handler toastQueue){
        super(); //Don't forget it
        mToastQueue = toastQueue; //The queue used to send toast msgs back to UI thread
        mCommandReadyFlag = false;
        mReadBuffer = new byte[5];
        mLastCommandReceived = new byte[5];
        mCommandQueue = new Handler(){
            public void handleMessage(Message msg) {
                //Log.d(TAG, "handleMessage: cmd");
                Bundle bundle = msg.getData();
                mLastCommandReceived = bundle.getByteArray("cmd");
                mCommandReadyFlag = true;
            }
            
        };
    }
    
    @Override
    protected void setup() throws ConnectionLostException, InterruptedException {
        Log.d(TAG, "BotControllerLooper:setup");
        showToast("BotControllerLooper:setup");
		//led_ = ioio_.openDigitalOutput(0, true);
		mPwmled = ioio_.openPwmOutput(ioio_.LED_PIN, 60);
		mUart = ioio_.openUart(RX_PIN, TX_PIN, 9600, Uart.Parity.NONE, Uart.StopBits.ONE);
		mIn = mUart.getInputStream();
		mOut = mUart.getOutputStream();
    }

    @Override
    public void loop() throws ConnectionLostException, InterruptedException {
        //led_.write(false);
        if(mCommandReadyFlag){
            Log.d(TAG, "Sending_: " + mLastCommandReceived.length + " " + 
            (mLastCommandReceived[0] & 0xFF) + " " + (mLastCommandReceived[1] & 0xFF) + " " +
            (mLastCommandReceived[2] & 0xFF) + " " + (mLastCommandReceived[3] & 0xFF) + " " +
            (mLastCommandReceived[4] & 0xFF));
            mPwmled.setDutyCycle((float)(mLastCommandReceived[2]/100.0));
            
            try {
                mOut.write(mLastCommandReceived);
                mCommandReadyFlag = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
       
        try {
            if(mIn.available() > 0){
                try {
                    int data = mIn.read(mReadBuffer);
                    //Log.d(TAG, "Received: " + mReadBuffer);
                    Log.d(TAG, "Received: " + mReadBuffer.length + " " + 
                    (mReadBuffer[0] & 0xFF) + " " + (mReadBuffer[1] & 0xFF) + " " +
                    (mReadBuffer[2] & 0xFF) + " " + (mReadBuffer[3] & 0xFF) + " " +
                    (mReadBuffer[4] & 0xFF));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnected() {
        // TODO Auto-generated method stub
        Log.d(TAG, "BotControllerLooper:disconnected");
        showToast("BotControllerLooper:disconnected");
        super.disconnected();
    }

    @Override
    public void incompatible() {
        // TODO Auto-generated method stub
        super.incompatible();
    }
   
    public void sendCommand(int leftMode, int leftPower, int rightMode, int rightPower){
        //Receives a command (from UI), and adds it to command queue
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt("leftMode", leftMode);
        bundle.putInt("leftPower", leftPower);
        bundle.putInt("rightMode", rightMode);
        bundle.putInt("rightPower", rightPower);
        bundle.putByteArray("cmd", new byte[] {
                //(byte) 'H', //H is the move command
                (byte) leftMode,
                (byte) leftPower,
                (byte) rightMode,
                (byte) rightPower,
                (byte) '\n'
                });
        msg.setData(bundle);
        mCommandQueue.sendMessage(msg);
    }
    
    public void showToast(String txt){
        // Send a string to the UI thread's toast queue
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg", txt);
        msg.setData(bundle);
        mToastQueue.sendMessage(msg);
    }
}