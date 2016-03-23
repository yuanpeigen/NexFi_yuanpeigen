package com.nexfi.yuanpeigen.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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
import com.nexfi.yuanpeigen.util.FileUtils;
import com.nexfi.yuanpeigen.util.SocketUtils;
import com.nexfi.yuanpeigen.weight.ChatMessageAdapater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Mark on 2016/2/17.
 */
public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView lv;
    private Button sendMsg;
    private PopupWindow mPopupWindow = null;
    private ImageView iv, iv_add, iv_chatRoom, iv_pic, iv_photo, iv_folder;
    private View View_pop;
    private EditText editText, et_chat;
    private LinearLayout modify_name, release, layout_view;
    private Dialog mDialog_modify, mDialog_remove;
    private RelativeLayout back;
    private ChatMessageAdapater mListViewAdapater;
    private String toIp, username, localIP;
    private int avatar, myAvatar;
    private TextView nick;
    private DatagramSocket mDataSocket;
    private boolean visibility_Flag = false;
    public static final int REQUEST_CODE_SELECT_FILE = 1;
    private String fileName;
    private long fileSize;
    private String select_file_path = "";//发送端选择的文件的路径

    private String rece_file_path = "";//接收端文件的保存路径

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
        toIp = intent.getStringExtra("8");
        username = intent.getStringExtra("1");
        avatar = intent.getIntExtra("3", R.mipmap.user_head_female_1);
        SharedPreferences preferences = getSharedPreferences("IP", Context.MODE_PRIVATE);
        localIP = preferences.getString("useIP", null);
        initView();
        initReUDP();
        startServer();
        setAdapter();
        setOnClickListener();
        initmyAvatar();

    }

    /**
     * 选择本地文件
     */
    private void selectFileFromLocal() {
        Intent intent = null;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }


    //开启接收端
    private void startServer() {

        new Thread() {
            @Override
            public void run() {
                super.run();
                //使用线程池
                ExecutorService threadpool = Executors.newFixedThreadPool(10);
                try {
                    ServerSocket ss = new ServerSocket(10035);
                    while (true) {
                        threadpool.execute(new TcpFenDuanThread(ss.accept()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    //分段接收线程
    class TcpFenDuanThread implements Runnable {
        private Socket s;
        InputStream in = null;

        TcpFenDuanThread(Socket s) {
            this.s = s;
        }

        @Override
        public void run() {
            System.out.println(s.getInetAddress().getHostAddress() + "-----ip");
//            Message msg = handler.obtainMessage();
//            msg.what = 1;
//            handler.sendEmptyMessage(1);

            try {
                in = s.getInputStream();
                byte[] filename = new byte[256];
                in.read(filename);//鎺ユ敹鏂囦欢鍚?
                String file_name = new String(filename).trim();//文件名
//                File fileout = new File(Environment.getExternalStorageDirectory().getPath() + "/" + file_name);//鎺ユ敹鍒扮殑鏂囦欢鐨勫瓨鍌ㄨ矾寰?
                File fileDir = new File(Environment.getExternalStorageDirectory().getPath() + "/NexFi");
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                rece_file_path = fileDir + "/" + file_name;
                File fileout = new File(rece_file_path);//鎺ユ敹鍒扮殑鏂囦欢鐨勫瓨鍌ㄨ矾寰?
                FileOutputStream fos = new FileOutputStream(fileout);

                byte[] filesize = new byte[64];//瀛樺偍鏂囦欢澶у皬鐨勬暟瀛楃殑瀛楄妭鏁扮粍
                int b = 0;
                while (b < filesize.length) {
                    b += in.read(filesize, b, filesize.length - b);//鏂囦欢澶у皬鐨勫疄闄呭瓧鑺傛暟
                }
                int ends = 0;
                for (int i = 0; i < filesize.length; i++) {
                    if (filesize[i] == 0) {
                        ends = i;
                        break;
                    }
                }
                String filesizes = new String(filesize, 0, ends);
                int ta = Integer.parseInt(filesizes);//鏂囦欢鏈韩澶у皬
                final long fileSize = filesize.length;
//                final String fileName = fileout.getName();//文件名
                //文件扩展名
                final String extensionName = FileUtils.getExtensionName(file_name);
                ChatMessage chatMessage = new ChatMessage();
                //设置文件接收路径
                chatMessage.filePath = rece_file_path;
                //设置文件图标
                setFileIcon(chatMessage, extensionName);
                //文件大小
                final int finalTa = ta;
                chatMessage.isPb = 1;


                //TODO
                /**
                 * 文件接收
                 * */
                if (chatMessage.fromIP == toIp) {
                    chatMessage.fromAvatar = avatar;
                    chatMessage.msgType = 3;
                    if (file_name.length() > 23) {
                        file_name = file_name.substring(0, 23) + "\n" + file_name.substring(23);
                    }
                    chatMessage.fileName = file_name;
                    chatMessage.fileSize = finalTa;
                    chatMessage.sendTime = getDateNow();
                    mDataArrays.add(chatMessage);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mListViewAdapater != null) {
                                mListViewAdapater.notifyDataSetChanged();
                            }
                            if (mDataArrays.size() > 0) {
                                lv.setSelection(lv.getCount() - 1);
                            }
                        }
                    });
                }
//                pb_receive.setMax(ta);//-----设置进度条最大值---------------------------------------------------------
                byte[] buf = new byte[1024 * 1024];
                //循环接收
                while (true) {
                    if (ta == 0) {
                        break;
                    }
                    int len = ta;
                    if (len > buf.length) {
                        len = buf.length;
                    }
                    int rlen = in.read(buf, 0, len);
                    ta -= rlen;//姣忚鍙栦竴娆★紝鍓╀綑鐨勫瓧鑺傛暟
                    if (rlen > 0) {
                        fos.write(buf, 0, rlen);
                        Log.e("TAG", rlen + "---------------------------receive===================");
//                        pb_receive.setProgress(progress);//更新进度条进度
                        fos.flush();
                    } else {
                        break;
                    }
                }
//                msg.what = 2;
//                handler.sendEmptyMessage(2);

                fos.close();
                in.close();
                s.close();
                System.out.println(file_name + "文件接收完毕");
                Log.e("TAG", file_name + "---------------------------文件接收完毕===================");
                chatMessage.isPb = 0;
                //发送完毕就隐藏
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mListViewAdapater != null) {
                            mListViewAdapater.notifyDataSetChanged();
                        }
                        if (mDataArrays.size() > 0) {
                            lv.setSelection(lv.getCount() - 1);
                        }
                    }
                });

                BuddyDao buddyDao = new BuddyDao(ChatActivity.this);
                buddyDao.addP2PMsg(chatMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 根据文件扩展名设置文件图标
     *
     * @param chatMessage
     * @param extensionName
     */
    public void setFileIcon(ChatMessage chatMessage, String extensionName) {
        if (null != extensionName) {
            if ("txt".equals(extensionName)) {
                chatMessage.fileIcon = (R.mipmap.txt);
            } else if (extensionName.contains("doc")) {
                chatMessage.fileIcon = (R.mipmap.doc);
            } else if ("xls".equals(extensionName)) {
                //excel
                chatMessage.fileIcon = (R.mipmap.xls);
            } else if (("ppt").equals(extensionName)) {
                //ppt
                chatMessage.fileIcon = (R.mipmap.ppt);
            } else if ("pdf".equals(extensionName)) {
                //pdf
                chatMessage.fileIcon = (R.mipmap.pdf);
            } else if ("jpg".equals(extensionName)) {
                chatMessage.fileIcon = (R.mipmap.jpg);
            } else if ("jpg".equals(extensionName)) {
                chatMessage.fileIcon = (R.mipmap.jpeg);
            } else if ("png".equals(extensionName)) {
                chatMessage.fileIcon = (R.mipmap.png);
            } else if ("bmp".equals(extensionName)) {
                chatMessage.fileIcon = (R.mipmap.bmp);
            } else if ("gif".equals(extensionName)) {
                chatMessage.fileIcon = (R.mipmap.gif);
            } else if ("mp3".equals(extensionName)) {
                //mp3
                chatMessage.fileIcon = (R.mipmap.mp3);
            } else if ("apk".equals(extensionName)) {
                //apk
                chatMessage.fileIcon = (R.mipmap.apk);
            } else if ("zip".equals(extensionName)) {
                chatMessage.fileIcon = (R.mipmap.zip);
            } else if (("rar".equals(extensionName))) {
                chatMessage.fileIcon = (R.mipmap.rar);
            } else {//默认图标
                chatMessage.fileIcon = (R.mipmap.default_icon);
            }
        }
    }


    /**
     * 分段发送文件
     *
     * @param path
     */
    public void sendFenDuanFile(String path) {
        Socket s = null;
        OutputStream out = null;

        try {
            if (null == s) {
                s = new Socket(toIp, 10035);
                Log.e("TAG", toIp + "-------------------------------------------------===============================================");
            }
            s.setSoTimeout(5000);
            out = s.getOutputStream();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final File fileToSend = new File(path);

        fileSize = fileToSend.length();
        fileName = fileToSend.getName();
        String extensionName = FileUtils.getExtensionName(fileName);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.isPb = 1;//让进度条显示
        if (fileName.length() > 23) {
            fileName = fileName.substring(0, 23) + "\n" + fileName.substring(23);
        }
        chatMessage.fileName = fileName;
        chatMessage.fileSize = fileSize;
        chatMessage.filePath = path;

        //设置文件图标
        setFileIcon(chatMessage, extensionName);
        //文件名
        byte[] file = new byte[256];//定义字节数组用于存储文件名字大小
        byte[] tfile = fileToSend.getName().getBytes();
        for (int i = 0; i < tfile.length; i++) {
            file[i] = tfile[i];
        }
        file[tfile.length] = 0;
        try {
            out.write(file, 0, file.length);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //文件本身大小
        //
        byte[] size = new byte[64];
        byte[] tsize = ("" + fileToSend.length()).getBytes();

        for (int i = 0; i < tsize.length; i++) {
            size[i] = tsize[i];
        }

        size[tsize.length] = 0;
        try {
            out.write(size, 0, size.length);//灏嗘枃浠跺ぇ灏忎紶鍒版帴鏀剁
            //TODO
//            pb_send.setMax((int) fileToSend.length());//设置进度条的最大值========璁剧疆鏈€澶у€?
//            pb_send.setVisibility(View.VISIBLE);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //读取文件的输入流
        FileInputStream fis = null;
        byte[] buf = new byte[1024 * 1024];
        try {
            fis = new FileInputStream(fileToSend);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int readsize = 0;
        int prog = 0;
        //TODO
        chatMessage.fromAvatar = myAvatar;
        chatMessage.msgType = 2;
        chatMessage.toIP = toIp;
        chatMessage.fromIP = localIP;
        chatMessage.sendTime = getDateNow();
//        chatMessage.isPb = 0;
        chatMessage.fromNick = username;
        chatMessage.type = "chatP2P";
        mDataArrays.add(chatMessage);
        //发送开始就显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mListViewAdapater != null) {
                    mListViewAdapater.notifyDataSetChanged();
                }
                if (mDataArrays.size() > 0) {
                    lv.setSelection(lv.getCount() - 1);
                }

            }
        });
        try {
            while ((readsize = fis.read(buf, 0, buf.length)) > 0) {

                out.write(buf, 0, readsize);
                //TODO
                prog += readsize;
                Log.e("TAG", readsize + "-------------------------------------------------===============================================");
//                pb_send.setProgress(prog);//更新进度条进度
                //等待一会
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                out.flush();

            }

            //TODO
            //隐藏进度条
            chatMessage.isPb = 0;
            //发送完毕就隐藏
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mListViewAdapater != null) {
                        mListViewAdapater.notifyDataSetChanged();
                    }
                    if (mDataArrays.size() > 0) {
                        lv.setSelection(lv.getCount() - 1);
                    }

                }
            });

            BuddyDao buddyDao = new BuddyDao(ChatActivity.this);
            buddyDao.addP2PMsg(chatMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_FILE) { //选择文件
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    select_file_path = FileUtils.getPath(this, uri);
//                    pb_send.setVisibility(View.VISIBLE);//显示进度条
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            sendFenDuanFile(select_file_path);
                        }
                    }.start();
                }
            }
        }
    }


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
        iv_add = (ImageView) findViewById(R.id.iv_add);
        et_chat = (EditText) findViewById(R.id.et_chat);
        layout_view = (LinearLayout) findViewById(R.id.layout_view);
        nick = (TextView) findViewById(R.id.textView);
        nick.setText(username);
        sendMsg = (Button) findViewById(R.id.btn_sendMsg);
        iv_chatRoom = (ImageView) findViewById(R.id.iv_chatRoom);
        iv_folder = (ImageView) findViewById(R.id.iv_folder);
        iv_photo = (ImageView) findViewById(R.id.iv_photograph);
        iv_pic = (ImageView) findViewById(R.id.iv_pic);
    }


    private void setAdapter() {
        BuddyDao buddyDao = new BuddyDao(ChatActivity.this);
        mDataArrays = buddyDao.findP2PMsgAll();
        mListViewAdapater = new ChatMessageAdapater(getApplicationContext(), mDataArrays);
        lv.setAdapter(mListViewAdapater);
    }

    private void receive(ChatMessage chatMessage) {
        if (chatMessage.fromIP == toIp) {
            chatMessage.fromAvatar = avatar;
            chatMessage.msgType = 1;
            BuddyDao buddyDao = new BuddyDao(ChatActivity.this);
            buddyDao.addP2PMsg(chatMessage);
            mDataArrays.add(chatMessage);
            mListViewAdapater.notifyDataSetChanged();
            lv.setSelection(lv.getCount() - 1);
        }
    }

    private void send() {
        final String contString = et_chat.getText().toString();
        if (contString.length() > 0) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.fromAvatar = myAvatar;
            chatMessage.msgType = 0;
            chatMessage.toIP = toIp;
            chatMessage.fromIP = localIP;
            chatMessage.sendTime = getDateNow();
            chatMessage.content = contString;
            chatMessage.fromNick = username;
            chatMessage.type = "chatP2P";
            final String xml = chatMessage.toXml();
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    SocketUtils.sendUDP(toIp, xml);
                }
            }.start();
            BuddyDao buddyDao = new BuddyDao(ChatActivity.this);
            buddyDao.addP2PMsg(chatMessage);
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
        iv_add.setOnClickListener(this);
        iv_chatRoom.setOnClickListener(this);
        iv_folder.setOnClickListener(this);
        iv_pic.setOnClickListener(this);
        iv_photo.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
        intent.putExtra("Dialog", false);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_man:
                initPop();
                break;
            case R.id.iv_add:
                if (visibility_Flag) {
                    layout_view.setVisibility(View.GONE);
                    visibility_Flag = false;
                } else {
                    layout_view.setVisibility(View.VISIBLE);
                    visibility_Flag = true;
                }
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
            case R.id.iv_chatRoom:
                Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_photograph:
                Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_folder:
                /**
                 * 发送文件
                 * */
                selectFileFromLocal();
                break;
            case R.id.iv_pic:
                selectFileFromLocal();
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
