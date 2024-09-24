package com.dreaming.bluetooth.framework.connect.listener;

import com.dreaming.bluetooth.framework.receiver.listener.BluetoothClientListener;

public abstract class BluetoothStateListener extends BluetoothClientListener {

    public abstract void onBluetoothStateChanged(boolean openOrClosed);

    @Override
    public void onSyncInvoke(Object...args) {
        boolean openOrClosed = (boolean) args[0];
        onBluetoothStateChanged(openOrClosed);
    }
}
