#include <opencv2/opencv.hpp>
using namespace cv;

class Detector{
    public:
        Detector(int hMin=0, int hMax=255, int sMin=0, int sMax=255, 
                int vMin=0, int vMax=255, int erodeSize=0, int dilateSize=0);
        ~Detector(void);

        void detect(Mat *origImg, Mat *processedImg);

    private:
        int hMin, hMax, sMin, sMax, vMin, vMax, erodeSize, dilateSize;
};
