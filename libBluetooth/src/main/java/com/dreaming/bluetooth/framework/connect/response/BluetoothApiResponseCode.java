package com.dreaming.bluetooth.framework.connect.response;

public enum BluetoothApiResponseCode {
    Unknown          (-128),
    Success          (0  ),
    Failed           (-1 ),
    Canceled         (-2 ),
    IllegalArgument  (-3 ),
    BleNotSupported  (-4 ),
    BluetoothDisabled(-5 ),
    ServiceUnready   (-6 ),
    Timedout         (-7 ),
    Overflow         (-8 ),
    Denied           (-9 ),
    Exception        (-10),
    ;

    public final int code;
    private BluetoothApiResponseCode(int code){
        this.code = code;
    }

    public static BluetoothApiResponseCode parse(int code){
        switch (code){
            case 0  : return Success          ;
            case -1 : return Failed           ;
            case -2 : return Canceled         ;
            case -3 : return IllegalArgument  ;
            case -4 : return BleNotSupported  ;
            case -5 : return BluetoothDisabled;
            case -6 : return ServiceUnready   ;
            case -7 : return Timedout         ;
            case -8 : return Overflow         ;
            case -9 : return Denied           ;
            case -10: return Exception        ;
        }
        return Unknown;
    }
}
