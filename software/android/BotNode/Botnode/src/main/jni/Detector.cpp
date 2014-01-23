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

void Detector::detect(Mat *origImg, Mat *processedImg){
    cvtColor(*origImg, *processedImg, CV_BGR2HSV); //Convert from BGR to HSV
}
