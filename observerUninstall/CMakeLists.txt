# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             observerUninstall

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/ObserverUninstall.c )

# Specifies a path to native header files.

include_directories(src/main/cpp/include/)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )


# 设置关联库给自己写的源码，比如在自己写的observerUninstall模块中用到了一些NDK中提供的代码log，需要将log导入到observerUninstall中
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       observerUninstall

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

# 使用此命令设置目标ABI，如果要配置仅支持特定的ABI只能在 gradle中配置（可以是模块级的gradle），这里有个疑问：难道不能直接在cmake中配置吗？
#set_target_properties( # Specifies the target library.
#                       imported-lib
#
#                       # Specifies the parameter you want to define.
#                       PROPERTIES IMPORTED_LOCATION
#
#                       # Provides the path to the library you want to import.
#                       imported-lib/src/${ANDROID_ABI}/libimported-lib.so )