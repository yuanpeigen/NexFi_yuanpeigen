package com.nexfi.yuanpeigen.weight;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.nexfi.yuanpeigen.activity.ChatActivity;
import com.nexfi.yuanpeigen.bean.ChatUser;
import com.nexfi.yuanpeigen.dao.BuddyDao;
import com.nexfi.yuanpeigen.nexfi.R;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_nearby, container, false);
        ex_offline = (ExpandableListView) v.findViewById(R.id.ex_offline);
        ex_online = (ExpandableListView) v.findViewById(R.id.ex_online);
        ex_new = (ExpandableListView) v.findViewById(R.id.ex_new);
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

}
