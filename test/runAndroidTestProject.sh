#!/bin/bash
cd ..
adb shell pm uninstall -k de.bitdroid.flooding
ant clean debug
adb install -r bin/flooding-debug.apk
cd - 
ant clean debug
adb install -r bin/flooding_test-debug.apk
ant test
