package com.nexfi.yuanpeigen.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.dao.BuddyDao;
import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.util.SocketUtils;
import com.nexfi.yuanpeigen.weight.ChatRoomMessageAdapater;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Mark on 2016/3/22.
 */
public class ChatRoomActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView textViewRoom;
    private RelativeLayout iv_backRoom;
    private ListView lv_chatRoom;
    private ImageView iv_addRoom, iv_chatRoom_room, iv_picRoom, iv_photographRoom, iv_folderRoom;
    private EditText et_chatRoom;
    private Button btn_sendMsgRoom;
    private ChatRoomMessageAdapater chatRoomMessageAdapater;
    private LinearLayout layout_view;
    private boolean visibility_Flag = false;
    private String username, localIP;
    public static final int REQUEST_CODE_SELECT_FILE = 1;
    private int myAvatar;
    private String select_file_path = "";//发送端选择的文件的路径

    private String rece_file_path = "";//接收端文件的保存路径
    private List<ChatMessage> mDataArrays = new ArrayList<ChatMessage>();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (msg.obj != null) {
                    receive((ChatMessage) msg.obj);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        initRece();
        initView();
        setOnClickListener();
        setAdapter();
        initmyAvatar();
//        SharedPreferences preferences = getSharedPreferences("IP", Context.MODE_PRIVATE);
//        localIP = preferences.getString("useIP", null);
        SharedPreferences preferences2 = getSharedPreferences("username", Context.MODE_PRIVATE);
        username = preferences2.getString("userName", null);
        int ipAddress = getIpAddress();
        localIP = intToIp(ipAddress);
    }

    private int getIpAddress() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress();
    }

    public String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 接收多播
     */
    private void initRece() {
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
                        System.out.println(new String(dp.getData()));
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }.start();

    }


    private void initmyAvatar() {
        SharedPreferences preferences = getSharedPreferences("UserHeadIcon", Context.MODE_PRIVATE);
        myAvatar = preferences.getInt("userhead", R.mipmap.user_head_female_3);
    }

    private void receive(ChatMessage chatMessage) {
        chatMessage.msgType = 1;
        BuddyDao buddyDao = new BuddyDao(ChatRoomActivity.this);
        buddyDao.addRoomMsg(chatMessage);
        mDataArrays.add(chatMessage);
        chatRoomMessageAdapater.notifyDataSetChanged();
        lv_chatRoom.setSelection(lv_chatRoom.getCount() - 1);

    }

    private void send() {
        final String contString = et_chatRoom.getText().toString();
        if (contString.length() > 0) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.fromAvatar = myAvatar;
            chatMessage.msgType = 0;
            chatMessage.sendTime = getDateNow();
            chatMessage.fromIP = localIP;
            chatMessage.content = contString;
            chatMessage.fromNick = username;
            chatMessage.type = "chatRoom";
            final String xml = chatMessage.toXml();
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    SocketUtils.sendBroadcastRoom(xml);
                }
            }.start();
            BuddyDao buddyDao = new BuddyDao(ChatRoomActivity.this);
            buddyDao.addRoomMsg(chatMessage);
            mDataArrays.add(chatMessage);
            chatRoomMessageAdapater.notifyDataSetChanged();
            lv_chatRoom.setSelection(lv_chatRoom.getCount() - 1);
        }
    }

    private void setAdapter() {
        BuddyDao buddyDao = new BuddyDao(ChatRoomActivity.this);
        mDataArrays = buddyDao.findRoomMsgAll();
        chatRoomMessageAdapater = new ChatRoomMessageAdapater(getApplicationContext(), mDataArrays);
        lv_chatRoom.setAdapter(chatRoomMessageAdapater);
    }

    /**
     * 获得发送时间
     */
    private String getDateNow() {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
        SimpleDateFormat hour = new SimpleDateFormat("HH");
        String date = hour.format(new Date());
        int num = Integer.parseInt(date);
        if (num >= 12) {
            return "下午" + " " + format.format(new Date());
        }
        return "上午" + " " + format.format(new Date());
    }


    private void initView() {
        textViewRoom = (TextView) findViewById(R.id.textViewRoom);
        iv_backRoom = (RelativeLayout) findViewById(R.id.iv_backRoom);
        iv_chatRoom_room = (ImageView) findViewById(R.id.iv_chatRoom_room);
        lv_chatRoom = (ListView) findViewById(R.id.lv_chatRoom);
        iv_photographRoom = (ImageView) findViewById(R.id.iv_photographRoom);
        iv_picRoom = (ImageView) findViewById(R.id.iv_picRoom);
        iv_folderRoom = (ImageView) findViewById(R.id.iv_folderRoom);
        iv_addRoom = (ImageView) findViewById(R.id.iv_addRoom);
        et_chatRoom = (EditText) findViewById(R.id.et_chatRoom);
        btn_sendMsgRoom = (Button) findViewById(R.id.btn_sendMsgRoom);
        layout_view = (LinearLayout) findViewById(R.id.layout_viewRoom);
        textViewRoom.setText("群聊");
    }

    private void setOnClickListener() {
        btn_sendMsgRoom.setOnClickListener(this);
        iv_addRoom.setOnClickListener(this);
        iv_folderRoom.setOnClickListener(this);
        iv_picRoom.setOnClickListener(this);
        iv_photographRoom.setOnClickListener(this);
        iv_chatRoom_room.setOnClickListener(this);
        iv_backRoom.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_addRoom:
                if (visibility_Flag) {
                    layout_view.setVisibility(View.GONE);
                    visibility_Flag = false;
                } else {
                    layout_view.setVisibility(View.VISIBLE);
                    visibility_Flag = true;
                }
                break;
            case R.id.iv_backRoom:
                Intent intent = new Intent(ChatRoomActivity.this, MainActivity.class);
                intent.putExtra("DialogRoom", false);
                startActivity(intent);
                finish();
                break;
            case R.id.iv_chatRoom_room:
                Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_photographRoom:
                Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_folderRoom:
                /**
                 * 发送文件
                 * */
                Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_picRoom:
                Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_sendMsgRoom:
                /**
                 * 发送消息
                 * */
                send();
                et_chatRoom.setText(null);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatRoomActivity.this, MainActivity.class);
        intent.putExtra("DialogRoom", false);
        startActivity(intent);
        finish();
    }
}
