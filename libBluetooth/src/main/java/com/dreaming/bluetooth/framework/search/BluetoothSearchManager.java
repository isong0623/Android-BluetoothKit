package com.dreaming.bluetooth.framework.search;

import android.os.Bundle;

import com.dreaming.bluetooth.framework.connect.BluetoothExtra;
import com.dreaming.bluetooth.framework.connect.response.BleGeneralResponse;
import com.dreaming.bluetooth.framework.search.response.BluetoothSearchResponse;

import static com.dreaming.bluetooth.framework.search.SearchState.Finished;
import static com.dreaming.bluetooth.framework.search.SearchState.Cancel;
import static com.dreaming.bluetooth.framework.search.SearchState.Start;
import static com.dreaming.bluetooth.framework.search.SearchState.Stop;

public class BluetoothSearchManager {

    public static void search(SearchRequest request, final BleGeneralResponse response) {
        BluetoothSearchRequest requestWrapper = new BluetoothSearchRequest(request);
        BluetoothSearchHelper.getInstance().startSearch(requestWrapper, new BluetoothSearchResponse() {
            @Override
            public void onSearchStarted() {
                response.onResponse(Start.state, null);
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(BluetoothExtra.SearchResult.toString(), device);
                response.onResponse(Finished.state, bundle);
            }

            @Override
            public void onSearchStopped() {
                response.onResponse(Stop.state, null);
            }

            @Override
            public void onSearchCanceled() {
                response.onResponse(Cancel.state, null);
            }
        });
    }

    public static void stopSearch() {
        BluetoothSearchHelper.getInstance().stopSearch();
    }
}
