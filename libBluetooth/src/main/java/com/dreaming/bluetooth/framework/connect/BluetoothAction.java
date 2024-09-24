package com.dreaming.bluetooth.framework.connect;

public enum BluetoothAction {
    ConnectStatusChanged("action.connect_status_changed"),
    CharacterChanged("action.character_changed"),
    ;
    private String action;

    @Override
    public String toString() {
        return action;
    }

    private BluetoothAction(String action){
        this.action = action;
    }

}
