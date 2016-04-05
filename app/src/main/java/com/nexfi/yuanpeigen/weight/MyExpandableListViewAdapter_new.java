package com.nexfi.yuanpeigen.weight;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.nexfi.yuanpeigen.bean.ChatUser;
import com.nexfi.yuanpeigen.nexfi.R;

import java.util.List;

/**
 * Created by Mark on 2016/2/3.
 */
public class MyExpandableListViewAdapter_new extends BaseExpandableListAdapter {
    private LayoutInflater inflater;
    private List<ChatUser> userinfos;

    public MyExpandableListViewAdapter_new(Context context, List<ChatUser> userinfos) {
        this.userinfos = userinfos;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Log.e("TAG",userinfos.size()+"--------------------------------userinfos--------------------------------");
        return userinfos.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return 0;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return userinfos.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v = inflater.inflate(R.layout.new_group, null);
        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ChatUser entity = userinfos.get(childPosition);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.new_child, null);
            holder = new ViewHolder();
            holder.tv_username = (TextView) convertView.findViewById(R.id.tv_username_new);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_username.setText(entity.nick);
        return convertView;
    }

    static class ViewHolder {
        public TextView tv_username;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}
