cmake_minimum_required(VERSION 3.25)

project(fx-jni)
set(VERSION "1.0.0")

set(CMAKE_BUILD_TYPE Release)
set(CMAKE_CXX_STANDARD 17)


set(JAVA_INCLUDE_PATH "$ENV{JAVA_HOME}/include")
set(JAVA_EXTRAS "")

if (WIN32)
    set(JAVA_EXTRAS "${JAVA_INCLUDE_PATH}/win32")
endif (WIN32)


#Add libs
find_package(JNI REQUIRED)
include_directories(${JAVA_INCLUDE_PATH})
include_directories(${JAVA_EXTRAS})


add_library(fx-jni-${VERSION} SHARED
        src/Decoration.cpp
        src/jni_headers/com_sun_fx_jni_Decoration.h
        src/utils/HwndMap.cpp
        src/utils/HwndMap.h
        src/utils/utils.cpp
        src/utils/utils.h
        src/utils/FXWinProc.h
        src/fx-jni.rc
)


