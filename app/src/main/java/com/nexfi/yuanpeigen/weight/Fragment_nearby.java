package com.nexfi.yuanpeigen.weight;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private ExpandableListView ex_online, ex_offline, ex_new;
    private MyExpandableListViewAdapter_new newAdapter;
    private MyExpandableListViewAdapter_online onlineAdapter;
    private MyExpandableListViewAdapter_offline offlineAdapter;
    private List<ChatUser> mDataArrays = new ArrayList<ChatUser>();
    private ImageView iv_add;
    private LinearLayout addChatRoom;
    private View View_pop;
    private PopupWindow mPopupWindow = null;
    private String localIp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_nearby, container, false);
        ex_offline = (ExpandableListView) v.findViewById(R.id.ex_offline);
        ex_online = (ExpandableListView) v.findViewById(R.id.ex_online);
        ex_new = (ExpandableListView) v.findViewById(R.id.ex_new);
        View_pop = inflater.inflate(R.layout.pop_menu_add, null);
        int ipAddress = getIpAddress();
        localIp = intToIp(ipAddress);
        addChatRoom = (LinearLayout) View_pop.findViewById(R.id.add_chatRoom);
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
        iv_add = (ImageView) v.findViewById(R.id.iv_add);
        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPop();
            }
        });
        setAdapter();
        ex_new.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("8", mDataArrays.get(childPosition).account);
                intent.putExtra("1", mDataArrays.get(childPosition).nick);
                intent.putExtra("3", mDataArrays.get(childPosition).avatar);
                startActivity(intent);
                getActivity().finish();
                return true;
            }
        });

        ex_new.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                newAdapter.notifyDataSetChanged();
                return false;
            }
        });
        ex_offline.setAdapter(new MyExpandableListViewAdapter_offline(this.getActivity()));
        ex_offline.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                startActivity(new Intent(getActivity(), ChatActivity.class));
                return true;
            }
        });


        ex_online.setAdapter(new MyExpandableListViewAdapter_online(this.getActivity()));
        ex_online.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                startActivity(new Intent(getActivity(), ChatActivity.class));
                return true;
            }
        });
        return v;
    }


    private void setAdapter() {
        BuddyDao buddyDao = new BuddyDao(this.getActivity());
        mDataArrays = buddyDao.findAll();
        newAdapter = new MyExpandableListViewAdapter_new(this.getActivity(), mDataArrays);
        ex_new.setAdapter(newAdapter);
    }

    private void initPop() {
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(View_pop, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        }
        mPopupWindow.showAsDropDown(iv_add, 0, 0);
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
