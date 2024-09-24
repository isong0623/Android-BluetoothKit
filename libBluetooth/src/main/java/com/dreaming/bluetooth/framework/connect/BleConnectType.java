package com.dreaming.bluetooth.framework.connect;

public enum BleConnectType {
    None  (0     ),
    Read  (0x1   ),
    Write (0x2   ),
    Notify(0x4   ),
    Rssi  (0x8   ),
    All   (0x16-1),
    ;
    public final int bit;
    private BleConnectType(int bit){
        this.bit = bit;
    }

    public static int make(BleConnectType...bits){
        int result = 0;
        for(BleConnectType type : bits){
            result |= type.bit;
        }
        return result;
    }
}
