package com.dreaming.bluetooth.framework.connect;

import android.bluetooth.BluetoothAdapter;

public enum BluetoothState {
    Unknown      (-1                                ),
    Off          (BluetoothAdapter.STATE_OFF        ),
    On           (BluetoothAdapter.STATE_ON         ),
    TurningOn    (BluetoothAdapter.STATE_TURNING_ON ),
    TurningOff   (BluetoothAdapter.STATE_TURNING_OFF),
    BleTurningOn (14),//LE Mode Only, BluetoothAdapter.STATE_BLE_TURNING_ON
    BleOn        (15),//LE Mode Only, BluetoothAdapter.STATE_BLE_ON
    BleTurningOff(16),//LE Mode Only, BluetoothAdapter.STATE_BLE_TURNING_OFF
    ;
    
    public final int state;
    private BluetoothState(int state){
        this.state = state;
    }

    public BluetoothState parse(int state){
        switch (state){
            case BluetoothAdapter.STATE_OFF        : return Off          ;
            case BluetoothAdapter.STATE_ON         : return On           ;
            case BluetoothAdapter.STATE_TURNING_ON : return TurningOn    ;
            case BluetoothAdapter.STATE_TURNING_OFF: return TurningOff   ;
            case 14                                : return BleTurningOn ;
            case 15                                : return BleOn        ;
            case 16                                : return BleTurningOff;
        }
        return Unknown;
    }
}
