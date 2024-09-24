package com.dreaming.bluetooth.framework.connect;

/**
 * GATT Status
 */
public enum BleConnectStatus {
    Unknown     (0   ),
    Connected   (0x10),
    Disconnected(0x20),
    ;
    public final int status;
    private BleConnectStatus(int status){
        this.status = status;
    }

    public static BleConnectStatus parse(int status){
        switch (status){
            case 0x10: return Connected   ;
            case 0x20: return Disconnected;
        }
        return Unknown;
    }
}
