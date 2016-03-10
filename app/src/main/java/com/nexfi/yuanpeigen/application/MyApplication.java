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
    @Override
    public void onCreate() {
        super.onCreate();

/*
        String localIP = getLocalIP();

        Intent intent = new Intent(this, ReceService.class);
        intent.putExtra("LOCAL_IP", localIP);
        startService(intent);*/
    }


    public String getLocalIP() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        Log.e("TAG", ip + "===ip----");
        return ip;
    }


    public String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }


}
