package com.dreaming.bluetooth.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.dreaming.bluetooth.view.ListConstants;
import com.dreaming.bluetooth.adapter.DeviceListAdapter;
import com.dreaming.bluetooth.R;
import com.dreaming.bluetooth.framework.BluetoothClient;
import com.dreaming.bluetooth.framework.connect.listener.BluetoothStateListener;
import com.dreaming.bluetooth.framework.search.SearchRequest;
import com.dreaming.bluetooth.framework.search.SearchResult;
import com.dreaming.bluetooth.framework.search.response.SearchResponse;
import com.dreaming.bluetooth.view.PullRefreshListView;
import com.dreaming.bluetooth.view.PullToRefreshFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String MAC = "B0:D5:9D:6F:E7:A5";

    private PullToRefreshFrameLayout mRefreshLayout;
    private PullRefreshListView mListView;
    private DeviceListAdapter mAdapter;
    private TextView mTvTitle;

    private List<SearchResult> mDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CoordinatorLayout c;
        FloatingActionButton fab;
        AppBarLayout abl;
        Toolbar tb;
        mDevices = new ArrayList<SearchResult>();

        mTvTitle = (TextView) findViewById(R.id.title);

        mRefreshLayout = (PullToRefreshFrameLayout) findViewById(R.id.pulllayout);

        mListView = mRefreshLayout.getPullToRefreshListView();
        mAdapter = new DeviceListAdapter(this);
        mListView.setAdapter(mAdapter);

        mListView.setOnRefreshListener(new PullRefreshListView.OnRefreshListener() {

            @Override
            public void onRefresh() {
                searchDevice();
            }

        });

        searchDevice();

        BluetoothClient.getInstance().registerBluetoothStateListener(new BluetoothStateListener() {
            @Override
            public void onBluetoothStateChanged(boolean openOrClosed) {
                Log.v("MainActivity", String.format("onBluetoothStateChanged %b", openOrClosed));
            }
        });
    }

    private void searchDevice() {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(5000, 2).build();

        BluetoothClient.getInstance().search(request, mSearchResponse);
    }

    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
            Log.w("MainActivity", "MainActivity.onSearchStarted");
            mListView.onRefreshComplete(true);
            mRefreshLayout.showState(ListConstants.LIST);
            mTvTitle.setText(R.string.string_refreshing);
            mDevices.clear();
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
//            BluetoothLog.w("MainActivity.onDeviceFounded " + device.device.getAddress());
            if (!mDevices.contains(device)) {
                mDevices.add(device);
                mAdapter.setDataList(mDevices);

//                Beacon beacon = new Beacon(device.scanRecord);
//                BluetoothLog.v(String.format("beacon for %s\n%s", device.getAddress(), beacon.toString()));

//                BeaconItem beaconItem = null;
//                BeaconParser beaconParser = new BeaconParser(beaconItem);
//                int firstByte = beaconParser.readByte(); // 读取第1个字节
//                int secondByte = beaconParser.readByte(); // 读取第2个字节
//                int productId = beaconParser.readShort(); // 读取第3,4个字节
//                boolean bit1 = beaconParser.getBit(firstByte, 0); // 获取第1字节的第1bit
//                boolean bit2 = beaconParser.getBit(firstByte, 1); // 获取第1字节的第2bit
//                beaconParser.setPosition(0); // 将读取起点设置到第1字节处
            }

            if (mDevices.size() > 0) {
                mRefreshLayout.showState(ListConstants.LIST);
            }
        }

        @Override
        public void onSearchStopped() {
            Log.w("MainActivity", "MainActivity.onSearchStopped");
            mListView.onRefreshComplete(true);
            mRefreshLayout.showState(ListConstants.LIST);

            mTvTitle.setText(R.string.devices);
        }

        @Override
        public void onSearchCanceled() {
            Log.w("MainActivity", "MainActivity.onSearchCanceled");

            mListView.onRefreshComplete(true);
            mRefreshLayout.showState(ListConstants.LIST);

            mTvTitle.setText(R.string.devices);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        BluetoothClient.getInstance().stopSearch();
    }
}
