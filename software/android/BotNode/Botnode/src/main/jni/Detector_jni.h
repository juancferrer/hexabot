#include <jni.h>
/* Header for class com_micronixsolutions_botnode_Detector */

#ifndef _Included_com_micronixsolutions_botnode_Detector
#define _Included_com_micronixsolutions_botnode_Detector
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_micronixsolutions_botnode_Detector_nativeCreateObject
  (JNIEnv *, jclass, jint, jint, jint, jint, jint, jint, jint, jint);

JNIEXPORT void JNICALL Java_com_micronixsolutions_botnode_Detector_nativeDetect
  (JNIEnv *, jclass, jlong, jlong, jlong, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif
