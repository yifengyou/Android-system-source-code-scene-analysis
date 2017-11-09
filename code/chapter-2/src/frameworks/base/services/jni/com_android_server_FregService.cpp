#define LOG_TAG "FregServiceJNI"

#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"

#include <utils/misc.h>
#include <utils/Log.h>
#include <hardware/hardware.h>
#include <hardware/freg.h>

#include <stdio.h>

namespace android
{
	static void freg_setVal(JNIEnv* env, jobject clazz, jint ptr, jint value) {
		freg_device_t* device = (freg_device_t*)ptr;
		if(!device) {
			LOGE("Device freg is not open.");
			return;
		}	
	
		int val = value;

		LOGI("Set value %d to device freg.", val);
		
		device->set_val(device, val);
	}

	static jint freg_getVal(JNIEnv* env, jobject clazz, jint ptr) {
		freg_device_t* device = (freg_device_t*)ptr;
		if(!device) {
			LOGE("Device freg is not open.");
			return 0;
		}

		int val = 0;

		device->get_val(device, &val);
		
		LOGI("Get value %d from device freg.", val);
	
		return val;
	}

	static inline int freg_device_open(const hw_module_t* module, struct freg_device_t** device) {
		return module->methods->open(module, FREG_HARDWARE_DEVICE_ID, (struct hw_device_t**)device);
	}
	
	static jint freg_init(JNIEnv* env, jclass clazz) {
		freg_module_t* module;
		freg_device_t* device;
		
		LOGI("Initializing HAL stub freg......");

		if(hw_get_module(FREG_HARDWARE_MODULE_ID, (const struct hw_module_t**)&module) == 0) {
			LOGI("Device freg found.");
			if(freg_device_open(&(module->common), &device) == 0) {
				LOGI("Device freg is open.");
				return (jint)device;
			}

			LOGE("Failed to open device freg.");
			return 0;
		}

		LOGE("Failed to get HAL stub freg.");

		return 0;		
	}

	static const JNINativeMethod method_table[] = {
		{"init_native", "()I", (void*)freg_init},
		{"setVal_native", "(II)V", (void*)freg_setVal},
		{"getVal_native", "(I)I", (void*)freg_getVal},
	};

	int register_android_server_FregService(JNIEnv *env) {
    		return jniRegisterNativeMethods(env, "com/android/server/FregService", method_table, NELEM(method_table));
	}
};

