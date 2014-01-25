package com.micronixsolutions.botnode;

/**
 * Created by juan on 1/22/14.
 */
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class Detector {
    public Detector(int hMin, int hMax, int sMin, int sMax, int vMin, int vMax, int erodeSize, int dilateSize) {
        mNativeObj = nativeCreateObject(hMin, hMax, sMin, sMax, vMin, vMax, erodeSize, dilateSize);
    }

    public void detect(Mat origImg, Mat processedImg, Scalar lowColorLimit, Scalar highColorLimit) {
        nativeDetect(mNativeObj, origImg.getNativeObjAddr(), processedImg.getNativeObjAddr(), lowColorLimit, highColorLimit);
    }

    private long mNativeObj = 0;

    private static native long nativeCreateObject(int hMin, int hMax, int sMin, int sMax, int vMin, int vMax, int erodeSize, int dilateSize);
    private static native long nativeDetect(long thiz, long origImg, long processedImg, Object lowColorLimit, Object highColorLimit);
}
