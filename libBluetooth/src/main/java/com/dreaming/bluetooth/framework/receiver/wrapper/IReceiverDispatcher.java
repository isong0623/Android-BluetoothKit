package com.dreaming.bluetooth.framework.receiver.wrapper;

import com.dreaming.bluetooth.framework.receiver.listener.BluetoothReceiverListener;

import java.util.List;

public interface IReceiverDispatcher {

    List<BluetoothReceiverListener> getListeners(Class<?> clazz);
}
