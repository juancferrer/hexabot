#include <Detector_jni.h>
#include <Detector.h>
#include <opencv2/opencv.hpp>

#include <android/log.h>

#define LOG_TAG "FaceDetection/DetectionBasedTracker"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;



JNIEXPORT jlong JNICALL Java_com_micronixsolutions_botnode_Detector_nativeCreateObject
(JNIEnv * jenv, jclass, jint hMin, jint hMax, jint sMin, jint sMax, jint vMin, jint vMax, jint erodeSize, jint dilateSize)
{
    //LOGD("Java_com_micronixsolutions_botnode_Detector_nativeCreateObject enter");
    jlong result = 0;
    try
    {
        result = (jlong)new Detector(hMin, hMax, sMin, sMax, vMin, vMax, erodeSize, dilateSize);
    }
    catch(cv::Exception& e)
    {
        //LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        //LOGD("nativeCreateObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of Detector.nativeCreateObject()");
        return 0;
    }

    //LOGD("Java_com_micronixsolutions_botnode_Detectior exit");
    return result;
}

JNIEXPORT void JNICALL Java_com_micronixsolutions_botnode_Detector_nativeDetect
(JNIEnv * jenv, jclass, jlong thiz, jlong origImg, jlong processedImg, jobject lowLimit, jobject highLimit)
{
    jclass scalarClass = jenv->GetObjectClass(lowLimit);
    jfieldID valField = jenv->GetFieldID(scalarClass, "val", "[D");

    jdoubleArray lowArray = static_cast<jdoubleArray>(jenv->GetObjectField(lowLimit, valField));
    jdoubleArray highArray = static_cast<jdoubleArray>(jenv->GetObjectField(highLimit, valField));

    double *lowLimitData = jenv->GetDoubleArrayElements(lowArray, 0);
    double *highLimitData = jenv->GetDoubleArrayElements(highArray, 0);

    Scalar low = Scalar(lowLimitData[0], lowLimitData[1], lowLimitData[2]);
    Scalar high = Scalar(highLimitData[0], highLimitData[1], highLimitData[2]);
    //LOGD("Java_com_micronixsolutions_botnode_Detector_nativeDetect enter");
    try
    {
        ((Detector*)thiz)->detect((Mat*)origImg, (Mat*)processedImg, &low, &high);
        //Release stuff
        jenv->ReleaseDoubleArrayElements(lowArray, lowLimitData,0);
        jenv->ReleaseDoubleArrayElements(highArray, highLimitData,0);
    }
    catch(cv::Exception& e)
    {
        //LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        //LOGD("nativeDetect caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code Detector.nativeDetect()");
    }
    //LOGD("Java_com_micronixsolutions_botnode_Detector_nativeDetect exit");
}
