package com.dreaming.bluetooth.framework.connect.request;

import com.dreaming.bluetooth.framework.connect.IBleConnectDispatcher;

public interface IBleRequest {

    void process(IBleConnectDispatcher dispatcher);

    void cancel();
}
