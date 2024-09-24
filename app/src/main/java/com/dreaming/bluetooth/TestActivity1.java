package com.dreaming.bluetooth;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class TestActivity1 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity2);
        Log.v("TestActivity1","onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("TestActivity1","onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("TestActivity1","onStart");
    }
}
