package com.nexfi.yuanpeigen.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.dao.BuddyDao;
import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.util.SocketUtils;
import com.nexfi.yuanpeigen.weight.MyListViewAdapater;

import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Mark on 2016/2/17.
 */
public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView lv;
    private Button sendMsg;
    private PopupWindow mPopupWindow = null;
    private ImageView iv;
    private View View_pop;
    private EditText editText, et_chat;
    private LinearLayout modify_name, release;
    private Dialog mDialog_modify, mDialog_remove;
    private RelativeLayout back;
    private MyListViewAdapater mListViewAdapater;
    private String fromIp, username, localIP;
    private int avatar, myAvatar;
    private TextView nick;
    private int length;
    private DatagramSocket mDataSocket;
    private ChatMessage chatMessage;
    /**
     * 数据
     */
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


    private void initmyAvatar() {
        SharedPreferences preferences = getSharedPreferences("UserHeadIcon", Context.MODE_PRIVATE);
        myAvatar = preferences.getInt("userhead", R.mipmap.user_head_male_1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        fromIp = intent.getStringExtra("8");
        username = intent.getStringExtra("1");
        avatar = intent.getIntExtra("3", R.mipmap.user_head_female_1);
        SharedPreferences preferences = getSharedPreferences("IP", Context.MODE_PRIVATE);
        localIP = preferences.getString("useIP", null);
        initView();
        initReUDP();
        setAdapter();
        setOnClickListener();
        initmyAvatar();
        length = mDataArrays.size();
        chatMessage = new ChatMessage();

    }

   /* @Override
    protected void onStop() {
        super.onStop();
        Intent intent3 = new Intent(this, NotificationService.class);
        intent3.putExtra("avatar", avatar);
        intent3.putExtra("username", username);
        startService(intent3);
    }*/

    /*    private void showNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this);
        Intent resultIntent = new Intent(this, ChatActivity.class);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), avatar);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notifyBuilder.setContentTitle(username)
                .setContentText("您有新消息")
                .setLargeIcon(bitmap)
                .setSmallIcon(avatar)
                .setAutoCancel(true)
                .setNumber(mDataArrays.size() - length)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis());
        manager.notify(52, notifyBuilder.build());
    }*/


    private void initReUDP() {
        new Thread() {
            public void run() {
                try {
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
                        Message msg = handler.obtainMessage();
                        msg.obj = fromXml;
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private void initView() {
        View_pop = LayoutInflater.from(this).inflate(R.layout.pop_menu, null);
        modify_name = (LinearLayout) View_pop.findViewById(R.id.modify_name);
        back = (RelativeLayout) findViewById(R.id.iv_back);
        release = (LinearLayout) View_pop.findViewById(R.id.release_connection);
        iv = (ImageView) findViewById(R.id.iv_man);
        lv = (ListView) findViewById(R.id.lv_chat);
        et_chat = (EditText) findViewById(R.id.et_chat);
        nick = (TextView) findViewById(R.id.textView);
        nick.setText(username);
        sendMsg = (Button) findViewById(R.id.btn_sendMsg);
    }


    private void setAdapter() {
        BuddyDao buddyDao = new BuddyDao(ChatActivity.this);
        mDataArrays = buddyDao.findP2PMsgAll();
        mListViewAdapater = new MyListViewAdapater(this, mDataArrays);
        lv.setAdapter(mListViewAdapater);
    }

    private void receive(ChatMessage chatMessage) {
        chatMessage.fromAvatar = avatar;
        chatMessage.setMsgType(1);
        mDataArrays.add(chatMessage);
        mListViewAdapater.notifyDataSetChanged();
        BuddyDao buddyDao = new BuddyDao(ChatActivity.this);
        buddyDao.addP2PMsg(chatMessage);
        lv.setSelection(lv.getCount() - 1);
    }

    private void send() {
        final String contString = et_chat.getText().toString();
        if (contString.length() > 0) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.fromAvatar = myAvatar;
            chatMessage.setMsgType(0);
            chatMessage.toIP = fromIp;
            chatMessage.fromIP = localIP;
            chatMessage.sendTime = getDateNow();
            chatMessage.content = contString;
            chatMessage.fromNick = username;
            chatMessage.type = "chatP2P";
            BuddyDao buddyDao = new BuddyDao(ChatActivity.this);
            buddyDao.addP2PMsg(chatMessage);
            final String xml = chatMessage.toXml();
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    SocketUtils.sendUDP(fromIp, xml);
                }
            }.start();
            mDataArrays.add(chatMessage);
            mListViewAdapater.notifyDataSetChanged();
            lv.setSelection(lv.getCount() - 1);
        }
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

    private void setOnClickListener() {
        iv.setOnClickListener(this);
        modify_name.setOnClickListener(this);
        release.setOnClickListener(this);
        back.setOnClickListener(this);
        sendMsg.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_man:
                initPop();
                break;
            case R.id.modify_name:
                initDialog_modify();
                break;
            case R.id.release_connection:
                initDialog_remove();
                break;
            case R.id.iv_back:
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                intent.putExtra("Dialog", false);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_sendMsg:
                send();
                et_chat.setText(null);
                break;
        }
    }

    private void initDialog_remove() {
        mDialog_remove = new Dialog(ChatActivity.this);
        mDialog_remove.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog_remove.show();
        Window win = mDialog_remove.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);
        mDialog_remove.getWindow().setContentView(R.layout.dialog_remove);
        mDialog_remove.setCancelable(false);

        /**设置与谁解除连接*/
        TextView textView = (TextView) mDialog_remove.getWindow().findViewById(R.id.tv_remove);
        Button btn_ensure = (Button) mDialog_remove.getWindow().findViewById(R.id.btn_ensure_remove);
        Button btn_cancel = (Button) mDialog_remove.getWindow().findViewById(R.id.btn_cancel_remove);
        btn_ensure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "成功解除连接", Toast.LENGTH_SHORT).show();
                mDialog_remove.dismiss();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog_remove.dismiss();
            }
        });
    }

    private void initPop() {
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(View_pop, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        }
        mPopupWindow.showAsDropDown(iv, 0, 0);
    }

    private void initDialog_modify() {
        mDialog_modify = new Dialog(this);
        mDialog_modify.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog_modify.show();
        Window win = mDialog_modify.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);
        mDialog_modify.getWindow().setContentView(R.layout.dialog_modifyremarks);
        Button btn_cancel = (Button) mDialog_modify.getWindow().findViewById(R.id.btn_cancel_remarks);
        editText = (EditText) mDialog_modify.getWindow().findViewById(R.id.et_remarks);
        Button btn_ensure = (Button) mDialog_modify.getWindow().findViewById(R.id.btn_ensure_remarks);
        btn_ensure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editText.getText())) {
                    saveUserInfo(ChatActivity.this, editText.getText().toString(), null);
                    mDialog_modify.dismiss();
                } else {
                    Toast.makeText(ChatActivity.this, "备注不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog_modify.dismiss();
            }
        });
    }

    public static boolean saveUserInfo(Context context, String username, String password) {
        try {
            File file = new File(context.getFilesDir(), "userinfo.txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write((username + "##" + password).getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

}
