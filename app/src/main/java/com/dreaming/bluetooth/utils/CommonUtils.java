package com.dreaming.bluetooth.utils;

import android.widget.Toast;

import com.dreaming.bluetooth.MyApplication;

public class CommonUtils {

    public static void toast(String text) {
        Toast.makeText(MyApplication.getInstance(), text, Toast.LENGTH_SHORT).show();
    }
}
