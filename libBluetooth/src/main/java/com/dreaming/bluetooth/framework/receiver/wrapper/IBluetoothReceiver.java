package com.dreaming.bluetooth.framework.receiver.wrapper;

import com.dreaming.bluetooth.framework.receiver.listener.BluetoothReceiverListener;

public interface IBluetoothReceiver {

    void register(BluetoothReceiverListener listener);
}
