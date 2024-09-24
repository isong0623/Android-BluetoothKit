// IBluetoothManager.aidl
package com.dreaming.bluetooth.framework;

// Declare any non-default types here with import statements

import com.dreaming.bluetooth.framework.IResponse;

interface IBluetoothService {
    void callBluetoothApi(int code, inout Bundle args, IResponse response);
}
