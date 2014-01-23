LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_LIB_TYPE:=STATIC
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
include ../../../../opencv-android/src/main/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := $(wildcard $(LOCAL_PATH)/*.cpp)
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl

LOCAL_MODULE     := detector

include $(BUILD_SHARED_LIBRARY)
