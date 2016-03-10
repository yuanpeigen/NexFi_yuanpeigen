package com.nexfi.yuanpeigen.weight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.nexfi.yuanpeigen.nexfi.R;

/**
 * Created by Mark on 2016/2/3.
 */
public class MyExpandableListViewAdapter_offline extends BaseExpandableListAdapter {
    private LayoutInflater inflater;
    public  MyExpandableListViewAdapter_offline(Context context){
        inflater = LayoutInflater.from(context);}
    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v =inflater.inflate(R.layout.offline_group,null);
        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v =inflater.inflate(R.layout.offline_child,null);
        return v;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
