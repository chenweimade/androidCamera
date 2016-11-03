#
# Created on: 2015-7-9
#     Author: Wang Yang
#       Mail: admin@wysaid.org
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := CGE
IMAGE_MAGICK 	    := magick/
JPEG_SRC_PATH 		:= jpeg-9b/
PHYSFS_SRC_PATH 	:= physfs-2.0.2/
PNG_SRC_PATH 		:= libpng-1.5.26/
TIFF_SRC_PATH 		:= tiff-3.9.5/
FREETYPE_SRC_PATH	:= freetype2-android/
ZLIB_SRC_PATH	    := zlib128/
WEBP_SRC_PATH	    := libwebp-0.3.1/
JASPER_SRC_PATH	    := jasper-1.900.1/

############################################################################################################################################################################################
#libjpego
include $(CLEAR_VARS)

LOCAL_MODULE    := libjpego
LOCAL_MODULE_FILENAME := libhello-jni

LOCAL_C_INCLUDES  :=  ${JPEG_SRC_PATH}

LOCAL_SRC_FILES := \
        ${JPEG_SRC_PATH}jcapimin.c ${JPEG_SRC_PATH}jcapistd.c ${JPEG_SRC_PATH}jccoefct.c ${JPEG_SRC_PATH}jccolor.c ${JPEG_SRC_PATH}jcdctmgr.c ${JPEG_SRC_PATH}jchuff.c \
        ${JPEG_SRC_PATH}jcinit.c ${JPEG_SRC_PATH}jcmainct.c ${JPEG_SRC_PATH}jcmarker.c ${JPEG_SRC_PATH}jcmaster.c ${JPEG_SRC_PATH}jcomapi.c ${JPEG_SRC_PATH}jcparam.c \
        ${JPEG_SRC_PATH}jcprepct.c ${JPEG_SRC_PATH}jcsample.c ${JPEG_SRC_PATH}jctrans.c ${JPEG_SRC_PATH}jdapimin.c ${JPEG_SRC_PATH}jdapistd.c \
        ${JPEG_SRC_PATH}jdatadst.c ${JPEG_SRC_PATH}jdatasrc.c ${JPEG_SRC_PATH}jdcoefct.c ${JPEG_SRC_PATH}jdcolor.c ${JPEG_SRC_PATH}jddctmgr.c ${JPEG_SRC_PATH}jdhuff.c \
        ${JPEG_SRC_PATH}jdinput.c ${JPEG_SRC_PATH}jdmainct.c ${JPEG_SRC_PATH}jdmarker.c ${JPEG_SRC_PATH}jdmaster.c ${JPEG_SRC_PATH}jdmerge.c \
        ${JPEG_SRC_PATH}jdpostct.c ${JPEG_SRC_PATH}jdsample.c ${JPEG_SRC_PATH}jdtrans.c ${JPEG_SRC_PATH}jerror.c ${JPEG_SRC_PATH}jfdctflt.c ${JPEG_SRC_PATH}jfdctfst.c \
        ${JPEG_SRC_PATH}jfdctint.c ${JPEG_SRC_PATH}jidctflt.c ${JPEG_SRC_PATH}jidctfst.c ${JPEG_SRC_PATH}jidctint.c ${JPEG_SRC_PATH}jquant1.c \
        ${JPEG_SRC_PATH}jquant2.c ${JPEG_SRC_PATH}jutils.c ${JPEG_SRC_PATH}jmemmgr.c ${JPEG_SRC_PATH}jcarith.c ${JPEG_SRC_PATH}jdarith.c ${JPEG_SRC_PATH}jaricom.c \
        ${JPEG_SRC_PATH}jmemnobs.c


LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)

include $(BUILD_STATIC_LIBRARY)


############################################################################################################################################################################################


CGE_ROOT=$(LOCAL_PATH)

CGE_SOURCE=$(CGE_ROOT)/src

CGE_INCLUDE=$(CGE_ROOT)/include

#### CGE Library headers ###########
LOCAL_EXPORT_C_INCLUDES := \
					$(CGE_ROOT)/interface \
					$(CGE_INCLUDE) \
					$(CGE_INCLUDE)/filters \


#### CGE Library native source  ###########

LOCAL_SRC_FILES :=  \
				cge/$(TARGET_ARCH_ABI)/libCGE.so


LOCAL_CPPFLAGS := -frtti -std=gnu++11


# 'CGE_USE_VIDEO_MODULE' determines if the project should compile with ffmpeg.

ifdef CGE_USE_VIDEO_MODULE

VIDEO_MODULE_DEFINE = -D_CGE_USE_FFMPEG_ 

endif

ifndef CGE_RELEASE_MODE

BUILD_MODE = -D_CGE_LOGS_

endif

LOCAL_CFLAGS    := ${VIDEO_MODULE_DEFINE} ${BUILD_MODE} -DANDROID_NDK -DCGE_LOG_TAG=\"libCGE\" -DCGE_TEXTURE_PREMULTIPLIED=1 -D__STDC_CONSTANT_MACROS -D_CGE_DISABLE_GLOBALCONTEXT_ -O3 -ffast-math -D_CGE_ONLY_FILTERS_

ifdef CGE_USE_FACE_MODULE

LOCAL_CFLAGS := $(LOCAL_CFLAGS) -D_CGE_USE_FACE_MODULE_

endif

ifndef CGE_USE_VIDEO_MODULE

#LOCAL_CFLAGS := $(LOCAL_CFLAGS) -D_CGE_ONLY_FILTERS_

include $(PREBUILT_SHARED_LIBRARY)

else 

LOCAL_SHARED_LIBRARIES := ffmpeg

include $(PREBUILT_SHARED_LIBRARY)

################################

# include $(CLEAR_VARS)
# LOCAL_MODULE := x264
# LOCAL_CFLAGS := -march=armv7-a -mfloat-abi=softfp -mfpu=neon -O3 -ffast-math -funroll-loops
# LOCAL_SRC_FILES := ffmpeg/libx264.142.so
# #LOCAL_EXPORT_C_INCLUDES := $(CGE_ROOT)/ffmpeg
# include $(PREBUILT_SHARED_LIBRARY)

###############################

include $(CLEAR_VARS)
LOCAL_MODULE := ffmpeg
LOCAL_CFLAGS := -mfloat-abi=softfp -mfpu=vfp -O3 -ffast-math -funroll-loops -fPIC
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
LOCAL_CFLAGS := $(LOCAL_CFLAGS) march=armv7-a -mfpu=neon
endif
LOCAL_SRC_FILES := ffmpeg/$(TARGET_ARCH_ABI)/libffmpeg.so
LOCAL_EXPORT_C_INCLUDES := $(CGE_ROOT)/ffmpeg

# LOCAL_SHARED_LIBRARIES := x264

include $(PREBUILT_SHARED_LIBRARY)

endif

###############################

ifdef CGE_USE_FACE_MODULE

include $(CLEAR_VARS)
include $(CGE_ROOT)/faceTracker/jni/Android.mk

endif
###############################

# Call user defined module
include $(CLEAR_VARS)
include $(CGE_ROOT)/source/source.mk

###############################
# magick
include $(CLEAR_VARS)
include $(CGE_ROOT)/magick/Android.mk
