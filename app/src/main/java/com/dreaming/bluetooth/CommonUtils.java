package com.dreaming.bluetooth;

import android.widget.Toast;

public class CommonUtils {

    public static void toast(String text) {
        Toast.makeText(MyApplication.getInstance(), text, Toast.LENGTH_SHORT).show();
    }
}
