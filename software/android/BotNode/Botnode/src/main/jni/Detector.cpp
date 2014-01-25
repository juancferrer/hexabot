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
    //cvtColor(*origImg, *processedImg, CV_BGR2HSV); //Convert from BGR to HSV
    //Create mask, leaving only selected color
    Mat mask;
    inRange(*origImg, *lowLimit, *highLimit, mask);
    processedImg->setTo(Scalar(0,0,0)); //Turn black
    origImg->copyTo(*processedImg, mask); //Put original through mask
    //Convert to rgba, so android can display it
    cvtColor(*processedImg, *processedImg, CV_YUV2BGR_NV12, 4);
}
