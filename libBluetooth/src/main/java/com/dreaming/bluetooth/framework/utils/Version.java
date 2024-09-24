package com.dreaming.bluetooth.framework.utils;

import android.os.Build;

public class Version {

    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
