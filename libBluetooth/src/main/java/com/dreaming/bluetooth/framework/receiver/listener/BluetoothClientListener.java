package com.dreaming.bluetooth.framework.receiver.listener;

public abstract class BluetoothClientListener extends AbsBluetoothListener {

    @Override
    final public void onInvoke(Object... args) {
        throw new UnsupportedOperationException();
    }
}
