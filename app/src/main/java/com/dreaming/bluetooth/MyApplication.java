package com.dreaming.bluetooth;

import android.app.Application;

import com.dreaming.bluetooth.framework.BluetoothContext;
import com.dreaming.bluetooth.framework.connect.BleConnectAuthorizer;

import java.util.List;

public class MyApplication extends Application {

    private static MyApplication instance;

    public static Application getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initBluetooth();
    }

    private void initBluetooth(){
        BluetoothContext.set(this);

        BleConnectAuthorizer.setPermissionRequestor((onGranted, onDenied, permissions)->{

        });
    }
}
