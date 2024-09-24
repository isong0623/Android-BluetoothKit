package com.dreaming.bluetooth.framework.search.le;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

import com.dreaming.bluetooth.framework.search.BluetoothSearcher;
import com.dreaming.bluetooth.framework.search.SearchResult;
import com.dreaming.bluetooth.framework.search.response.BluetoothSearchResponse;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.BluetoothUtils;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLESearcher extends BluetoothSearcher {
	
	private static BluetoothLogger logger = new BluetoothLogger(BluetoothLESearcher.class);
	private BluetoothLESearcher() {
		mBluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
	}

	public static BluetoothLESearcher getInstance() {
		return BluetoothLESearcherHolder.instance;
	}

	private static class BluetoothLESearcherHolder {
		private static BluetoothLESearcher instance = new BluetoothLESearcher();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void startScanBluetooth(BluetoothSearchResponse response) {
		super.startScanBluetooth(response);
		
		mBluetoothAdapter.startLeScan(mLeScanCallback);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void stopScanBluetooth() {
		try {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		} catch (Exception e) {
			logger.e(e);
		}

		super.stopScanBluetooth();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void cancelScanBluetooth() {
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
		super.cancelScanBluetooth();
	}

	private final LeScanCallback mLeScanCallback = new LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            notifyDeviceFounded(new SearchResult(device, rssi, scanRecord));
		}
		
	};
}
