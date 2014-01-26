#include "Detector.h"

Detector::Detector(int hMi, int hMax, int sMin, int sMax, 
                int vMin, int vMax, int erodeSize, int dilateSize){
    this->hMin = hMin;
    this->hMax = hMax;
    this->sMin = sMin;
    this->sMax = sMax;
    this->vMin = vMin;
    this->vMax = vMax;
    this->erodeSize = erodeSize;
    this->dilateSize = dilateSize;
}

Detector::~Detector(void){
}

void Detector::detect(Mat *origImg, Mat *processedImg, Scalar *lowLimit,
        Scalar *highLimit){
    //Create mask, leaving only selected color
    cvtColor(*origImg, rgb, CV_YUV2BGR_NV12, 4); //Convert android YUV camera to RGB
    cvtColor(rgb, hsv, CV_BGR2HSV); //Convert RGB to HSV
    inRange(hsv, *lowLimit, *highLimit, mask); //Create the mask
    processedImg->setTo(Scalar(0)); //Turn black
    rgb.copyTo(*processedImg, mask); //Put rgb through mask, and save into processed
}
