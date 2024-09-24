package com.dreaming.bluetooth.framework.search;

import com.dreaming.bluetooth.framework.search.response.BluetoothSearchResponse;

public interface IBluetoothSearchHelper {

    void startSearch(BluetoothSearchRequest request, BluetoothSearchResponse response);

    void stopSearch();
}
