package com.nexfi.yuanpeigen.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.bean.ChatUser;
import com.nexfi.yuanpeigen.dao.BuddyDao;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;


//鐩戝惉娑堟伅
public class ReceService extends Service {
    public String localIP;//鏈満IP
    BuddyDao dao;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //保存用户的IP
        SharedPreferences preferences = getSharedPreferences("IP", Context.MODE_PRIVATE);
        localIP = preferences.getString("useIP", null);
        dao = new BuddyDao(getApplicationContext());
        init();
        initReceUDP();
    }


    //单对单聊天消息的监听
    private void initReceUDP() {
        new Thread() {
            public void run() {

                try {
                    byte[] buf = new byte[1024];
                    DatagramSocket ds = new DatagramSocket(10000);//开始监视12345端口
                    DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName("192.168.1.255"), 10000);//创建接收数据报的实例
                    while (true) {
                        Log.e("TAG", "------while (true)---service");
                        ds.receive(dp);//阻塞,直到收到数据报后将数据装入IP中
                        //转化
                        ChatMessage msg = new ChatMessage();
                        ChatMessage fromXml = (ChatMessage) msg.fromXml(new String(dp.getData()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    private void init() {
        new Thread() {
            public void run() {
                try {
                    MulticastSocket ds = new MulticastSocket(8005);
                    InetAddress receiveAddress = InetAddress.getByName("224.0.0.105");
                    ds.joinGroup(receiveAddress);
                    byte[] buff = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buff, buff.length, receiveAddress, 8005);

                    while (true) {
                        ds.receive(dp);

                        ChatUser user = new ChatUser();
                        ChatUser fromXml = (ChatUser) user.fromXml(new String(dp.getData()));
                        Log.e("TAG",fromXml.account+"---------------------上线了");


                        if ("online".equals(fromXml.type)) {

                            if (!localIP.equals(fromXml.account)) {//鍒ゆ柇鏄惁鏄湰鏈篒P
//                                Log.e("TAG",localIP+"---equals==="+fromXml.account);

                                if (!dao.find(fromXml.account)) {//鍒ゆ柇鏁版嵁搴撲腑鏄惁宸叉湁璇ョ敤鎴?
//                                    Log.e("TAG","---equ==="+fromXml.account);
                                    dao.add(fromXml);
                                    System.out.println("---tianjia---------------");
                                }
                            }

                        } else if ("offine".equals(fromXml.type)) {
                            //根据IP删除数据库中的记录
                            dao.delete(fromXml.account);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
