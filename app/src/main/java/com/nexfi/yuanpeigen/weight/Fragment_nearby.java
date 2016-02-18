package com.nexfi.yuanpeigen.weight;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.nexfi.yuanpeigen.nexfi.R;

/**
 * Created by Mark on 2016/2/4.
 */
public class Fragment_nearby extends Fragment {
    private ExpandableListView ex_online, ex_offline, ex_new;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View v= inflater.inflate(R.layout.fragment_nearby,container,false);
        ex_offline = (ExpandableListView) v.findViewById(R.id.ex_online);
        ex_online = (ExpandableListView) v.findViewById(R.id.ex_offline);
        ex_new = (ExpandableListView) v.findViewById(R.id.ex_new);
        ex_new.setAdapter(new MyExpandableListViewAdapter_new(this.getActivity()));
        ex_offline.setAdapter(new MyExpandableListViewAdapter_offline(this.getActivity()));
        ex_online.setAdapter(new MyExpandableListViewAdapter_online(this.getActivity()));
        return v;
    }
}
