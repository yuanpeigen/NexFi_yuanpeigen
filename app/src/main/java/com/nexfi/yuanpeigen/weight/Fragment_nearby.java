package com.nexfi.yuanpeigen.weight;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.nexfi.yuanpeigen.activity.ChatActivity;
import com.nexfi.yuanpeigen.activity.ChatRoomActivity;
import com.nexfi.yuanpeigen.bean.ChatUser;
import com.nexfi.yuanpeigen.dao.BuddyDao;
import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.util.SocketUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 2016/2/4.
 */
public class Fragment_nearby extends Fragment {


    private List<ChatUser> mDataArraysNew = new ArrayList<ChatUser>();
    private List<ChatUser> mDataArraysOnline = new ArrayList<ChatUser>();
    private List<ChatUser> mDataArraysOffline = new ArrayList<ChatUser>();
    private ImageView iv_add;
    private LinearLayout addChatRoom, share;
    private View View_pop, view_share, v_parent;
    private PopupWindow mPopupWindow = null, mPopupWindow_share = null;
    private String localIp;
    private ExpandableListAdapter usrListAdapter;
    private ExpandableListView userList;
    private List<String> groupList = new ArrayList<String>();
    private List<List<ChatUser>> childListNew = new ArrayList<List<ChatUser>>();
    private List<List<ChatUser>> childListOnline = new ArrayList<List<ChatUser>>();
    private List<List<ChatUser>> childListOffline = new ArrayList<List<ChatUser>>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v_parent = inflater.inflate(R.layout.fragment_nearby, container, false);
        userList = (ExpandableListView) v_parent.findViewById(R.id.ex_userlist);
        View_pop = inflater.inflate(R.layout.pop_menu_add, null);
        view_share = inflater.inflate(R.layout.layout_share, null);
        int ipAddress = getIpAddress();
        localIp = intToIp(ipAddress);
        addChatRoom = (LinearLayout) View_pop.findViewById(R.id.add_chatRoom);
        share = (LinearLayout) View_pop.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                initPopShare();
            }
        });
        addChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ////点击按钮，进入聊天室，同时发送多播通知其他聊天室成员(发送的通知包括IP,但是聊天的消息就是之前使用
                SocketUtils.startSendThread(localIp);
                //同时进入聊天室
                Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        iv_add = (ImageView) v_parent.findViewById(R.id.iv_add);
        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPop();
            }
        });


        setAdapter();//给附近的好友设置适配器

        userList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (groupPosition == 2) {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("8", mDataArraysNew.get(childPosition).account);
                    intent.putExtra("1", mDataArraysNew.get(childPosition).nick);
                    intent.putExtra("3", mDataArraysNew.get(childPosition).avatar);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    startActivity(new Intent(getActivity(), ChatActivity.class));
                }
                return true;
            }
        });

        getActivity().getContentResolver().registerContentObserver(
                Uri.parse("content://www.nexfi.com"), true,
                new Myobserve(new Handler()));
        return v_parent;
    }


    private class Myobserve extends ContentObserver {

        public Myobserve(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    BuddyDao buddyDao = new BuddyDao(getActivity());
                    mDataArraysNew = buddyDao.findAll();//查找所有用户
                    Log.e("TAG", "Myobserve----------------------------------------------------------" + mDataArraysNew.size());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setAdapter();
                        }
                    });
                }
            }.start();
            super.onChange(selfChange);
        }
    }


    private void setAdapter() {
        BuddyDao buddyDao = new BuddyDao(this.getActivity());
        mDataArraysNew = buddyDao.findAll();
        Log.e("TAG", "setAdapter----------------------------------------------------------" + mDataArraysNew.size());

        /**
         * Online 虚拟数据
         * */
        ChatUser chatUserOnline1 = new ChatUser();
        chatUserOnline1.nick = "Mark";
        chatUserOnline1.avatar = R.mipmap.user_head_male_1;
        mDataArraysOnline.add(chatUserOnline1);
        ChatUser chatUserOnline2 = new ChatUser();
        chatUserOnline2.nick = "AngelBaby";
        chatUserOnline2.avatar = R.mipmap.user_head_female_3;
        mDataArraysOnline.add(chatUserOnline2);
        ChatUser chatUserOnline3 = new ChatUser();
        chatUserOnline3.nick = "李晨";
        chatUserOnline3.avatar = R.mipmap.user_head_male_2;
        mDataArraysOnline.add(chatUserOnline3);
        ChatUser chatUserOnline4 = new ChatUser();
        chatUserOnline4.nick = "赵薇";
        chatUserOnline4.avatar = R.mipmap.user_head_female_4;
        mDataArraysOnline.add(chatUserOnline4);
        ChatUser chatUserOnline5 = new ChatUser();
        chatUserOnline5.nick = "冯小刚";
        chatUserOnline5.avatar = R.mipmap.user_head_male_3;
        mDataArraysOnline.add(chatUserOnline5);
        ChatUser chatUserOnline6 = new ChatUser();
        chatUserOnline6.nick = "佟丽娅";
        chatUserOnline6.avatar = R.mipmap.user_head_female_3;
        mDataArraysOnline.add(chatUserOnline6);
        ChatUser chatUserOnline7 = new ChatUser();
        chatUserOnline7.nick = "沈腾";
        chatUserOnline7.avatar = R.mipmap.user_head_male_4;
        mDataArraysOnline.add(chatUserOnline7);
        ChatUser chatUserOnline8 = new ChatUser();
        chatUserOnline8.nick = "马云";
        chatUserOnline8.avatar = R.mipmap.user_head_male_3;
        mDataArraysOnline.add(chatUserOnline8);
        ChatUser chatUserOnline9 = new ChatUser();
        chatUserOnline9.nick = "马化腾";
        chatUserOnline9.avatar = R.mipmap.user_head_male_2;
        mDataArraysOnline.add(chatUserOnline9);
        ChatUser chatUserOnline10 = new ChatUser();
        chatUserOnline10.nick = "高圆圆";
        chatUserOnline10.avatar = R.mipmap.user_head_female_3;
        mDataArraysOnline.add(chatUserOnline10);

        /**
         * Offline 虚拟数据
         * */
        ChatUser chatUserOffline1 = new ChatUser();
        chatUserOffline1.nick = "Lights";
        chatUserOffline1.avatar = R.mipmap.user_head_male_1;
        mDataArraysOffline.add(chatUserOffline1);
        ChatUser chatUserOffline2 = new ChatUser();
        chatUserOffline2.nick = "李晨";
        chatUserOffline2.avatar = R.mipmap.user_head_male_2;
        mDataArraysOffline.add(chatUserOffline2);
        ChatUser chatUserOffline3 = new ChatUser();
        chatUserOffline3.nick = "高圆圆";
        chatUserOffline3.avatar = R.mipmap.user_head_female_3;
        mDataArraysOffline.add(chatUserOffline3);
        ChatUser chatUserOffline4 = new ChatUser();
        chatUserOffline4.nick = "马化腾";
        chatUserOffline4.avatar = R.mipmap.user_head_male_2;
        mDataArraysOffline.add(chatUserOffline4);
        ChatUser chatUserOffline5 = new ChatUser();
        chatUserOffline5.nick = "马云";
        chatUserOffline5.avatar = R.mipmap.user_head_male_3;
        mDataArraysOffline.add(chatUserOffline5);
        ChatUser chatUserOffline6 = new ChatUser();
        chatUserOffline6.nick = "赵薇";
        chatUserOffline6.avatar = R.mipmap.user_head_female_4;
        mDataArraysOffline.add(chatUserOffline6);
        ChatUser chatUserOffline7 = new ChatUser();
        chatUserOffline7.nick = "冯小刚";
        chatUserOffline7.avatar = R.mipmap.user_head_male_3;
        mDataArraysOffline.add(chatUserOffline7);
        ChatUser chatUserOffline8 = new ChatUser();
        chatUserOffline8.nick = "AngelBaby";
        chatUserOffline8.avatar = R.mipmap.user_head_female_3;
        mDataArraysOffline.add(chatUserOffline8);
        ChatUser chatUserOffline9 = new ChatUser();
        chatUserOffline9.nick = "沈腾";
        chatUserOffline9.avatar = R.mipmap.user_head_male_4;
        mDataArraysOffline.add(chatUserOffline9);
        ChatUser chatUserOffline10 = new ChatUser();
        chatUserOffline10.nick = "佟丽娅";
        chatUserOffline10.avatar = R.mipmap.user_head_female_3;
        mDataArraysOffline.add(chatUserOffline10);

        /**
         *  Group数据
         * */
        groupList.add("在线好友");
        groupList.add("离线好友");
        groupList.add("附近的人");


        for (int index = 0; index < groupList.size(); ++index) {
            childListNew.add(mDataArraysNew);
            childListOnline.add(mDataArraysOnline);
            childListOffline.add(mDataArraysOffline);
        }
        usrListAdapter = new UserList(this.getActivity(), mDataArraysNew, mDataArraysOnline, mDataArraysOffline, groupList, childListNew, childListOnline, childListOffline);
        userList.setAdapter(usrListAdapter);
    }

    private void initPop() {
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(View_pop, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        }
        mPopupWindow.showAsDropDown(iv_add, 0, 0);
    }

    private void initPopShare() {
        if (mPopupWindow_share == null) {
            mPopupWindow_share = new PopupWindow(view_share, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow_share.setBackgroundDrawable(new ColorDrawable(0x00000000));
        }
        mPopupWindow_share.showAtLocation(v_parent, Gravity.CENTER, 0, 0);
    }

    public String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    private int getIpAddress() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress();
    }

}
