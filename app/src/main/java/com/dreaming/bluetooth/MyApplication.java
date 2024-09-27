package com.dreaming.bluetooth;

import android.Manifest;
import android.app.Application;

import com.dreaming.bluetooth.framework.BluetoothContext;
import com.dreaming.bluetooth.framework.connect.BleConnectAuthorizer;
import com.dreaming.easy.lib.permission.EasyPermission;
import com.dreaming.easy.lib.permission.request.PermissionConfigure;

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

        PermissionConfigure.setPermissionName("android.permission.BLUETOOTH_ADMIN", "蓝牙管理");
        PermissionConfigure.setPermissionMessage("android.permission.BLUETOOTH_ADMIN", "用于广播和搜索附近可用的蓝牙设备");

        PermissionConfigure.setPermissionName("android.permission.ACCESS_COARSE_LOCATION", "蓝牙定位");
        PermissionConfigure.setPermissionMessage("android.permission.ACCESS_COARSE_LOCATION", "用于获取附近外部蓝牙设备的硬件标识符");

        PermissionConfigure.setPermissionName("android.permission.BLUETOOTH_SCAN", "蓝牙搜索");
        PermissionConfigure.setPermissionMessage("android.permission.BLUETOOTH_SCAN", "用于搜索附近蓝牙设备");

        PermissionConfigure.setPermissionName("android.permission.BLUETOOTH_ADVERTISE", "蓝牙广播");
        PermissionConfigure.setPermissionMessage("android.permission.BLUETOOTH_ADVERTISE", "用于向附近的蓝牙设备发送广播，以便能够被它们搜索到");

        PermissionConfigure.setPermissionName("android.permission.BLUETOOTH_CONNECT", "蓝牙连接");
        PermissionConfigure.setPermissionMessage("android.permission.BLUETOOTH_CONNECT", "用于连接到蓝牙服务");

        BleConnectAuthorizer.setPermissionRequestor((onGranted, onDenied, permissions)->{
            EasyPermission.permissions(permissions)
                    .onGranted(lst -> {
                        if(onGranted == null) return;
                        onGranted.onAction(lst);
                    })
                    .onDenied(lst -> {
                        if(onDenied == null) return;
                        onDenied.onAction(lst);
                    })
                    .requestFully();
        });
    }
}
