package com.dreaming.bluetooth.framework.search;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.dreaming.bluetooth.framework.search.response.BluetoothSearchResponse;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;
import com.dreaming.bluetooth.framework.utils.BluetoothUtils;

import java.util.ArrayList;
import java.util.List;

public class BluetoothSearchRequest implements Handler.Callback {
	
	private static BluetoothLogger logger = new BluetoothLogger(BluetoothSearchRequest.class);
	private static final int SCAN_INTERVAL = 100;
	
	private static final int MSG_START_SEARCH = 0x11;
	private static final int MSG_Finished = 0x12;

	private List<BluetoothSearchTask> mSearchTaskList;
	private BluetoothSearchResponse mSearchResponse;

	private BluetoothSearchTask mCurrentTask;

	private Handler mHandler;

	public BluetoothSearchRequest(SearchRequest request) {
		mSearchTaskList = new ArrayList<BluetoothSearchTask>();
		List<SearchTask> tasks = request.getTasks();
		for (SearchTask task : tasks) {
			mSearchTaskList.add(new BluetoothSearchTask(task));
		}

		mHandler = new Handler(Looper.myLooper(), this);
	}

	public void setSearchResponse(BluetoothSearchResponse response) {
		mSearchResponse = response;
	}

	public void start() {
		if (mSearchResponse != null) {
			mSearchResponse.onSearchStarted();
		}

		notifyConnectedBluetoothDevices();

		mHandler.sendEmptyMessageDelayed(MSG_START_SEARCH, SCAN_INTERVAL);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_START_SEARCH:
				scheduleNewSearchTask();
				break;

			case MSG_Finished:
				SearchResult device = (SearchResult) msg.obj;
				if (mSearchResponse != null) {
					mSearchResponse.onDeviceFounded(device);
				}
				break;
		}
		return true;
	}

	private void scheduleNewSearchTask() {
		if (mSearchTaskList.size() > 0) {
			mCurrentTask = mSearchTaskList.remove(0);
			mCurrentTask.start(new BluetoothSearchTaskResponse(mCurrentTask));
		} else {
			mCurrentTask = null;
			
			if (mSearchResponse != null) {
				mSearchResponse.onSearchStopped();
			}
		}
	}
	
	public void cancel() {
		if (mCurrentTask != null) {
			mCurrentTask.cancel();
			mCurrentTask = null;
		}
		
		mSearchTaskList.clear();
		
		if (mSearchResponse != null) {
			mSearchResponse.onSearchCanceled();
		}

		mSearchResponse = null;
	}

	private void notifyConnectedBluetoothDevices() {
		boolean hasBleTask = false;
		boolean hasBscTask = false;

		for (BluetoothSearchTask task : mSearchTaskList) {
			if (task.isBluetoothLeSearch()) {
				hasBleTask = true;
			} else if (task.isBluetoothClassicSearch()) {
				hasBscTask = true;
			} else {
				throw new IllegalArgumentException("unknown search task type!");
			}
		}

		if (hasBleTask) {
			notifyConnectedBluetoothLeDevices();
		}

		if (hasBscTask) {
			notifyBondedBluetoothClassicDevices();
		}
	}

	private void notifyConnectedBluetoothLeDevices() {
		List<BluetoothDevice> devices = BluetoothUtils.getConnectedBluetoothLeDevices();

		for (BluetoothDevice device : devices) {
			notifyDeviceFounded(new SearchResult(device));
		}
	}

	private void notifyBondedBluetoothClassicDevices() {
		List<BluetoothDevice> devices = BluetoothUtils.getBondedBluetoothClassicDevices();

		for (BluetoothDevice device : devices) {
			notifyDeviceFounded(new SearchResult(device));
		}
	}

	private void notifyDeviceFounded(SearchResult device) {
		mHandler.obtainMessage(MSG_Finished, device).sendToTarget();
	}

	private class BluetoothSearchTaskResponse implements BluetoothSearchResponse {

		BluetoothSearchTask task;

		BluetoothSearchTaskResponse(BluetoothSearchTask task) {
			this.task = task;
		}

		@Override
		public void onSearchStarted() {
			logger.v("%s onSearchStarted", task);
		}

		@Override
		public void onDeviceFounded(SearchResult device) {
			logger.v("onDeviceFounded %s", device);
			notifyDeviceFounded(device);
		}

		@Override
		public void onSearchStopped() {
			logger.v("%s onSearchStopped", task);
			mHandler.sendEmptyMessageDelayed(MSG_START_SEARCH, SCAN_INTERVAL);
		}

		@Override
		public void onSearchCanceled() {
			logger.v("%s onSearchCanceled", task);
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (BluetoothSearchTask task : mSearchTaskList) {
			sb.append(task.toString() + ", ");
		}
		
		return sb.toString();
	}
}
