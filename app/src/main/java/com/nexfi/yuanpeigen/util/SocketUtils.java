package com.nexfi.yuanpeigen.util;

/**
 * Created by Mark on 2016/2/25.
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Timer;
import java.util.TimerTask;

public class SocketUtils {

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
        m_PlayTimer.schedule(m_PlayTimerTask, 0, 1000);// 鍗佺鍙戦€佷竴娆?
    }

    public static void sendBroadcast(String msg) {
        try {
            /*鍒涘缓socket瀹炰緥*/
            MulticastSocket ms = new MulticastSocket();
            ms.setBroadcast(true);
            InetAddress address = InetAddress.getByName("224.0.0.105");
            //String sendData=mEditText.getText().toString().trim();
            byte[] data = msg.getBytes();
            DatagramPacket dataPacket = new DatagramPacket(data, data.length, address,
                    8005);
            //鍔犲叆缁?
            ms.joinGroup(address);
            //鍙戦€?
            ms.send(dataPacket);
            ms.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static void sendBroadcastRoom(String msg) {
        try {
            /*鍒涘缓socket瀹炰緥*/
            MulticastSocket ms = new MulticastSocket();
            ms.setBroadcast(true);
            InetAddress address = InetAddress.getByName("224.0.0.110");
            //String sendData=mEditText.getText().toString().trim();
            byte[] data = msg.getBytes();
            DatagramPacket dataPacket = new DatagramPacket(data, data.length, address,
                    8007);
            //鍔犲叆缁?
            ms.joinGroup(address);
            //鍙戦€?
            ms.send(dataPacket);
            ms.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //鍙戦€佸崟鎾?
    public static void sendUDP(String destIP, String msg) {
        try {
            InetAddress target = InetAddress.getByName(destIP);//寰楀埌鐩爣鏈哄櫒鐨勫湴鍧€瀹炰緥
            DatagramSocket ds = new DatagramSocket();//浠?999绔彛鍙戦€佹暟鎹姤
            byte[] buf = msg.getBytes();//灏嗘暟鎹浆鎹㈡垚Byte绫诲瀷
            DatagramPacket op = new DatagramPacket(buf, buf.length, target, 10005);//灏咮UF缂撳啿鍖轰腑鐨勬暟鎹墦鍖?
            ds.send(op);//鍙戦€佹暟鎹?
            ds.close();//鍏抽棴杩炴帴
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //接收单播
    public static String receUDP() {
        try {
            byte[] buf = new byte[32 * 1024];
            DatagramSocket ds = new DatagramSocket(10005);//开始监视12345端口
            DatagramPacket dp = new DatagramPacket(buf, buf.length);//创建接收数据报的实例
            while (true) {
                ds.receive(dp);//阻塞,直到收到数据报后将数据装入IP中

                return new String(dp.getData());
            }

        } catch (Exception e) {
            // TODO: handle exception

            e.printStackTrace();
        }
        return null;
    }


}