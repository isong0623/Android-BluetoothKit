package com.dreaming.bluetooth.framework.connect;

import com.dreaming.bluetooth.framework.connect.request.BleRequest;

public interface IBleConnectDispatcher {

    void onRequestCompleted(BleRequest request);
}
