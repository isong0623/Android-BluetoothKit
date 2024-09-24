package com.dreaming.bluetooth.framework.connect.request;

public enum BluetoothApiRequestCode {
    Unknown        (-1),
    Connect        ( 1),
    Disconnect     ( 2),
    Read           ( 3),
    Write          ( 4),
    WriteNorsp     ( 5),
    Notify         ( 6),
    Unnotify       ( 7),
    ReadRssi       ( 8),
    Indicate       (10),
    Search         (11),
    StopSearch    (12),
    ReadDescriptor (13),
    WriteDescriptor(14),
    ClearRequest   (20),
    RefreshCache   (21),
    RequestMtu     (22),
    ;
    
    public final int code;
    private BluetoothApiRequestCode(int code){
        this.code = code;
    }

    public static BluetoothApiRequestCode parse(int code){
        switch (code){
            case  1 : return Connect        ;
            case  2 : return Disconnect     ;
            case  3 : return Read           ;
            case  4 : return Write          ;
            case  5 : return WriteNorsp     ;
            case  6 : return Notify         ;
            case  7 : return Unnotify       ;
            case  8 : return ReadRssi       ;
            case 10 : return Indicate       ;
            case 11 : return Search         ;
            case 12 : return StopSearch    ;
            case 13 : return ReadDescriptor ;
            case 14 : return WriteDescriptor;
            case 20 : return ClearRequest   ;
            case 21 : return RefreshCache   ;
            case 22 : return RequestMtu     ;
        }
        return Unknown;
    }
}
