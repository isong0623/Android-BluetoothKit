package com.dreaming.bluetooth.framework.connect;

import android.bluetooth.BluetoothDevice;

public enum BluetoothDeviceBondState {
    None   (BluetoothDevice.BOND_NONE   ),
    Bonding(BluetoothDevice.BOND_BONDING),
    Bonded (BluetoothDevice.BOND_BONDED ),
    ;

    public final int state;
    private BluetoothDeviceBondState(int state){
        this.state = state;
    }

    public static BluetoothDeviceBondState parse(int state){
        switch (state){
            case BluetoothDevice.BOND_NONE   : return None   ;
            case BluetoothDevice.BOND_BONDING: return Bonding;
            case BluetoothDevice.BOND_BONDED : return Bonded ;
        }
        return None;
    }
}
