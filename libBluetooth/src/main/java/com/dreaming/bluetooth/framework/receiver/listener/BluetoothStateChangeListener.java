package com.dreaming.bluetooth.framework.receiver.listener;

import com.dreaming.bluetooth.framework.BluetoothClientImpl;
import com.dreaming.bluetooth.framework.connect.BluetoothState;

public abstract class BluetoothStateChangeListener extends BluetoothReceiverListener {

    protected abstract void onBluetoothStateChanged(int prevState, int curState);

    @Override
    public void onInvoke(Object... args) {
        int prevState = (int) args[0];
        int curState = (int) args[1];

        if (curState == BluetoothState.Off.state || curState == BluetoothState.TurningOff.state) {
            BluetoothClientImpl.getInstance().stopSearch();
        }

        onBluetoothStateChanged(prevState, curState);
    }

    @Override
    public String getName() {
        return BluetoothStateChangeListener.class.getSimpleName();
    }
}
