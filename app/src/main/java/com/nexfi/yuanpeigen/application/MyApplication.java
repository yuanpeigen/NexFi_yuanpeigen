package com.nexfi.yuanpeigen.application;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by Mark on 2016/2/29.
 */
public class MyApplication extends Application {

    public boolean DEBUG;

    @Override
    public void onCreate() {
        super.onCreate();
        DEBUG=true;
    }
}
