package com.dreaming.bluetooth.framework.channel;

public enum  ChanelCallbackCode {
    Success(0 ),
    Fail   (-1),
    Timeout(-2),
    Busy   (-3),
    ;

    public final int code;
    private ChanelCallbackCode(int code){
        this.code = code;
    }

    public static ChanelCallbackCode parse(int code){
        switch (code){
            case 0 : return Success;
            case -1: return Fail   ;
            case -2: return Timeout;
            case -3: return Busy   ;
        }
        return Fail;
    }
    
}
