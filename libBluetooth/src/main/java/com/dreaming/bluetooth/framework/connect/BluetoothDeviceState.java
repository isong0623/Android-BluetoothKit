package com.dreaming.bluetooth.framework.connect;

import android.bluetooth.BluetoothProfile;

public enum BluetoothDeviceState {
    Unknown      (-1                           ),
    Connected    (BluetoothProfile.STATE_CONNECTED    ),
    Connecting   (BluetoothProfile.STATE_CONNECTING   ),
    Disconnecting(BluetoothProfile.STATE_DISCONNECTING),
    Disconnected (BluetoothProfile.STATE_DISCONNECTED ),
    ServiceReady (0x13                         ),
    ;

    final int code;
    private BluetoothDeviceState(int code){
        this.code = code;
    }

    public static BluetoothDeviceState parse(int code){
        switch (code){
            case BluetoothProfile.STATE_CONNECTED     : return Connected    ;
            case BluetoothProfile.STATE_CONNECTING    : return Connecting   ;
            case BluetoothProfile.STATE_DISCONNECTING : return Disconnecting;
            case BluetoothProfile.STATE_DISCONNECTED  : return Disconnected ;
            case 0x13                                 : return ServiceReady ;
        }
        return Unknown;
    }
}
