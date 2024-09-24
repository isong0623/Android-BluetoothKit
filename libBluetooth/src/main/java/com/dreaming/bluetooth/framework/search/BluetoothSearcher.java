package com.dreaming.bluetooth.framework.search;

import android.bluetooth.BluetoothAdapter;

import com.dreaming.bluetooth.framework.search.classic.BluetoothClassicSearcher;
import com.dreaming.bluetooth.framework.search.le.BluetoothLESearcher;
import com.dreaming.bluetooth.framework.search.response.BluetoothSearchResponse;

public class BluetoothSearcher {

	protected BluetoothAdapter mBluetoothAdapter;
	protected BluetoothSearchResponse mSearchResponse;

	public static BluetoothSearcher newInstance(int type) {
		switch (SearchType.parse(type)) {
			case Classic:
				return BluetoothClassicSearcher.getInstance();
			case Ble:
				return BluetoothLESearcher.getInstance();
			default:
				throw new IllegalStateException(String.format("unknown search type %d", type));
		}
	}

	protected void startScanBluetooth(BluetoothSearchResponse callback) {
		mSearchResponse = callback;
		notifySearchStarted();
	}

	protected void stopScanBluetooth() {
		notifySearchStopped();
		mSearchResponse = null;
	}

	protected void cancelScanBluetooth() {
		notifySearchCanceled();
		mSearchResponse = null;
	}

	private void notifySearchStarted() {
		if (mSearchResponse != null) {
			mSearchResponse.onSearchStarted();
		}
	}

	protected void notifyDeviceFounded(SearchResult device) {
		if (mSearchResponse != null) {
			mSearchResponse.onDeviceFounded(device);
		}
	}

	private void notifySearchStopped() {
		if (mSearchResponse != null) {
			mSearchResponse.onSearchStopped();
		}
	}

	private void notifySearchCanceled() {
		if (mSearchResponse != null) {
			mSearchResponse.onSearchCanceled();
		}
	}
}
