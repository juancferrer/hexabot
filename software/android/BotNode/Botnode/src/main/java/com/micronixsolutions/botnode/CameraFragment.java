package com.micronixsolutions.botnode;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class CameraFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener{
    CustomCameraView mCamera;
    Detector mDetector;
    Mat mProcessedImg;
    private Display mDisplay;

    public CameraFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mDisplay = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        View rootView = inflater.inflate(R.layout.camera_fragment, container, false);
        mProcessedImg = new Mat();
        mDetector = new Detector(0,255,0,255,0,255,0,0);
        mCamera = (CustomCameraView) rootView.findViewById(R.id.camera_view);
        mCamera.setVisibility(SurfaceView.VISIBLE);
        mCamera.setCvCameraViewListener(this);
        mCamera.enableView();
        mCamera.enableFpsMeter();
        mCamera.setOnTouchListener(this);
        return rootView;
    }
    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Call a native method to process image, and display that.
        mDetector.detect(inputFrame.rgba(), mProcessedImg);
        if(mDisplay.getRotation() == Surface.ROTATION_270){
            Core.flip(mProcessedImg, mProcessedImg, -1);
        }
        return mProcessedImg;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d("TOUCHED", "X:" + motionEvent.getX() + " Y:" + motionEvent.getY());
        return true;
    }
}
