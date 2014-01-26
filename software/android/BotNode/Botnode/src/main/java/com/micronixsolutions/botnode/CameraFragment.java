package com.micronixsolutions.botnode;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
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
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class CameraFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2,
        View.OnTouchListener, GestureDetector.OnDoubleTapListener{
    private static int THRESHOLD = 45;
    private CustomCameraView mCamera;
    private Detector mDetector;
    private CustomCameraView.JavaCameraFrame mOrigImg;
    private Mat mProcessedImg;
    private GestureDetector mGestureDetector;
    private Display mDisplay;
    private Scalar mLowColorLimit;
    private Scalar mHighColorLimit;
    private boolean mFilterColor = false;

    public CameraFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.camera_fragment, container, false);
        mDisplay = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mGestureDetector = new GestureDetector(this.getActivity(), new MyGestureListener());
        mGestureDetector.setOnDoubleTapListener(this);
        mGestureDetector.setIsLongpressEnabled(false);
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
        mOrigImg = ((CustomCameraView.JavaCameraFrame) inputFrame);
        if(mLowColorLimit==null || mHighColorLimit==null || !mFilterColor){
            if(mDisplay.getRotation() == Surface.ROTATION_270){
                Core.flip(mOrigImg.rgba(), mProcessedImg, -1);
                return  mProcessedImg;
            }
            return mOrigImg.rgba();
        }
        else{
            mDetector.detect(mOrigImg.mYuvFrameData, mProcessedImg, mLowColorLimit, mHighColorLimit);
            /*
            Mat mask = new Mat();
            Mat hsv = new Mat();
            Imgproc.cvtColor(mOrigImg.rgba(), hsv, Imgproc.COLOR_RGB2HSV); // Convert yuv to hsv
            Core.inRange(hsv, mLowColorLimit, mHighColorLimit, mask); //filter in hsv
            mProcessedImg.setTo(new Scalar(0,0,0,0));
            mOrigImg.rgba().copyTo(mProcessedImg, mask); //copy rgb data to processed
            */
            if(mDisplay.getRotation() == Surface.ROTATION_270){
                Core.flip(mProcessedImg, mProcessedImg, -1);
            }
            return mProcessedImg;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d("TOUCHED", "X:" + motionEvent.getX() + " Y:" + motionEvent.getY());
        return mGestureDetector.onTouchEvent(motionEvent);
    }


    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        //Clear the color limits
        mFilterColor = false;
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        double[] color;
        double xFactor, yFactor;
        Mat rgba = mOrigImg.rgba();
        if(mDisplay.getRotation() == Surface.ROTATION_270){
            Core.flip(rgba, rgba, -1);
        }
        Mat hsv = new Mat();
        Imgproc.cvtColor(rgba, hsv, Imgproc.COLOR_RGB2HSV);
        if(mDisplay.getRotation() == Surface.ROTATION_0){
            xFactor = (double)rgba.height() / mCamera.getHeight();
            yFactor = (double)rgba.width() / mCamera.getWidth();
            //color = hsv.get((int)(motionEvent.getY()*xFactor), (int)(motionEvent.getX()*yFactor));
        }
        else{
            xFactor = (double)rgba.width() / (double)mCamera.getWidth();
            yFactor = (double)rgba.height() / (double)mCamera.getHeight();
            //color = hsv.get((int)(motionEvent.getX()*xFactor), (int)(motionEvent.getY()*yFactor));
        }
        //Get average color from 5x5 rect under the touch point
        Rect roiRect = new Rect((int)(motionEvent.getX() * xFactor)-2, (int)(motionEvent.getY()*yFactor)-2, 5, 5);
        Mat roiMat = hsv.submat(roiRect);
        Scalar avgColor = Core.mean(roiMat);
        double[] avgColorVals = avgColor.val;
        mLowColorLimit = new Scalar(avgColorVals[0]-(THRESHOLD*3), avgColorVals[1]-THRESHOLD, avgColorVals[2]-THRESHOLD);
        mHighColorLimit = new Scalar(avgColorVals[0]+(THRESHOLD*3), avgColorVals[1]+THRESHOLD, avgColorVals[2]+THRESHOLD);
        mFilterColor = true;
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent event){
            return true;
        }


    }
}
