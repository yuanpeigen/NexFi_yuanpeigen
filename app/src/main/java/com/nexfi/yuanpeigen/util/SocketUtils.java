package com.nexfi.yuanpeigen.util;

/**
 * Created by Mark on 2016/2/25.
 */

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.os.Handler;
import android.util.Log;

import com.nexfi.yuanpeigen.application.MyApplication;
import com.nexfi.yuanpeigen.bean.ChatMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Timer;
import java.util.TimerTask;

public class SocketUtils {

    static MyApplication app=new MyApplication();
    private static Timer m_PlayTimer;
    private static TimerTask m_PlayTimerTask;

    /* 鎵撳紑鍙戦€佺嚎绋?*/
    public static void startSendThread(final String msg) {

        m_PlayTimer = new Timer();

        m_PlayTimerTask = new TimerTask() {
            public void run() {
                sendBroadcast(msg);
            }

        };
        m_PlayTimer.schedule(m_PlayTimerTask, 0, 1000);//每隔1s发送一次
    }

    public static void sendBroadcast(String msg) {
        try {
            /*鍒涘缓socket瀹炰緥*/
            MulticastSocket ms = new MulticastSocket();
            ms.setBroadcast(true);
            InetAddress address = InetAddress.getByName("224.0.0.105");
            byte[] data = msg.getBytes();
            DatagramPacket dataPacket = new DatagramPacket(data, data.length, address,
                    8005);
            //加入组播
            ms.joinGroup(address);
            //发送
            ms.send(dataPacket);
            ms.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 聊天室发送多播
     * @param msg
     */
    public static void sendBroadcastRoom(final String msg) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
            /*创建组播对象*/
                    MulticastSocket ms = new MulticastSocket();
                    ms.setBroadcast(true);
                    InetAddress address = InetAddress.getByName("224.0.0.110");
                    byte[] data = msg.getBytes();
                    DatagramPacket dataPacket = new DatagramPacket(data, data.length, address,
                            8007);
                    //加入组播
                    ms.joinGroup(address);
                    //发送
                    if(app.DEBUG){
                        Log.e("DEBUG","-聊天室发送多播----"+new String(dataPacket.getData()));
                    }
                    ms.send(dataPacket);
                    ms.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * 接收UDP多播
     */
    public static void initReceMul(final Handler handler,final String localIP) {
        new Thread() {
            public void run() {
                try {
                    MulticastSocket ds = new MulticastSocket(8007);
                    InetAddress receiveAddress = InetAddress.getByName("224.0.0.110");
                    ds.joinGroup(receiveAddress);
                    byte[] buff = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buff, buff.length, receiveAddress, 8007);
                    while (true) {
                        ds.receive(dp);
                        if (null != dp) {
                            //说明是聊天信息，而不是不停发送的进入消息
                            ChatMessage msgg = new ChatMessage();
                            ChatMessage fromXml = (ChatMessage) msgg.fromXml(new String(dp.getData()));
                            if (!localIP.equals(fromXml.fromIP)) {
                                Message msg = handler.obtainMessage();
                                msg.obj = fromXml;
                                msg.what = 1;
                                handler.sendMessage(msg);
                            }
                        }
                        if(app.DEBUG){
                            Log.e("TAG","接收UDP多播-----"+app.DEBUG);
                            Log.e("TAG","接收UDP多播-----"+new String(dp.getData()));
                        }
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }.start();

    }



    //发送UDP单播
    public static void sendUDP(final String destIP, final String msg) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    InetAddress target = InetAddress.getByName(destIP);
                    DatagramSocket ds = new DatagramSocket();
                    byte[] buf = msg.getBytes();
                    DatagramPacket op = new DatagramPacket(buf, buf.length, target, 10005);
                    ds.send(op);
                    ds.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 接收UDP单播
     */
    public static void initReUDP(final Handler handler,final String toIp) {

        new Thread() {
            public void run() {
                try {
                    DatagramSocket mDataSocket=null;
                    byte[] buf = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);
                    if (mDataSocket == null) {
                        mDataSocket = new DatagramSocket(null);
                        mDataSocket.setReuseAddress(true);
                        mDataSocket.bind(new InetSocketAddress(10005));
                    }
                    while (true) {
                        mDataSocket.receive(dp);
                        ChatMessage msgg = new ChatMessage();
                        ChatMessage fromXml = (ChatMessage) msgg.fromXml(new String(dp.getData()));
                        //TODO
                        if (toIp.equals(fromXml.fromIP)) {
                            Message msg = handler.obtainMessage();
                            msg.obj = fromXml;
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public static int getIpAddress(Context context) {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress();
    }

    public static String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 获取本机IP
     * @param context
     * @return
     */
    public static String getLocalIP(Context context){
        int ipAddress = getIpAddress(context);
        String localIP = intToIp(ipAddress);
        return localIP;
    }


}