package com.micronixsolutions.botnode;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class CameraFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2{
    JavaCameraView mCamera;
    Detector mDetector;
    Mat processedImg;

    public CameraFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.camera_fragment, container, false);
        mDetector = new Detector(0,255,0,255,0,255,0,0);
        mCamera = (JavaCameraView) rootView.findViewById(R.id.camera_view);
        mCamera.setVisibility(SurfaceView.VISIBLE);
        mCamera.setCvCameraViewListener(this);
        mCamera.enableView();
        mCamera.enableFpsMeter();
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
        if(processedImg==null){
            processedImg = new Mat(inputFrame.rgba().size(), inputFrame.rgba().type());
        }
        mDetector.detect(inputFrame.rgba(), processedImg);
        return processedImg;
        //return inputFrame.rgba();
        /*
        Mat hsv = new Mat();
        Imgproc.cvtColor(inputFrame.rgba(), hsv, Imgproc.COLOR_RGB2HSV);
        return hsv;
        */
    }
}
