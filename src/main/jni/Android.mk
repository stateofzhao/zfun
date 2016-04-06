LOCAL_PATH:= $(call my-dir)
# 一个完整模块编译
include $(CLEAR_VARS)
LOCAL_SRC_FILES:=com_diagramsj_test_jniclass_ObserverUninstall.c #编译的源文件，系统将根据该文件来生成目标对象
#LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)
LOCAL_MODULE :=ObserverUninstall#编译的目标对象,系统将会生成 'libhello-jni.so'文件，供java文件调用，不能有空格
#LOCAL_PRELINK_MODULE := false
#LOCAL_MODULE_TAGS :=optional
LOCAL_LDLIBS:= -lm -llog
include $(BUILD_SHARED_LIBRARY)

#具体语法请参看：http://www.cnblogs.com/wainiwann/p/3837936.html
#现在Android Studio对NDK和C/C++原生支持挺好，所以现在如果不手动生成.so文件，可以直接在Module的build.gradle中配置即可
