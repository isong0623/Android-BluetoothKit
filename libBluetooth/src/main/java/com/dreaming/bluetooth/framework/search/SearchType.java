package com.dreaming.bluetooth.framework.search;

public enum SearchType {
    Unknown(0),
    Classic(1),
    Ble    (2),
    ;

    public final int type;
    private SearchType(int type){
        this.type = type;
    }

    public static SearchType parse(int type){
        switch (type){
            case 1: return Classic;
            case 2: return Ble    ;
        }
        return Unknown;
    }
}

