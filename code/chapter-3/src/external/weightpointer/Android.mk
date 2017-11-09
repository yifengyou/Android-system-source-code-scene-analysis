LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := weightpointer
LOCAL_SRC_FILES := weightpointer.cpp
LOCAL_SHARED_LIBRARIES :=  \
	libcutils \
	libutils
include $(BUILD_EXECUTABLE)
