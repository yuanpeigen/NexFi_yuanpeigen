package com.nexfi.yuanpeigen.weight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.nexfi.R;

import java.util.List;

/**
 * Created by Mark on 2016/2/17.
 */
public class MyListViewAdapater extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<ChatMessage> coll;


    public MyListViewAdapater(Context context, List<ChatMessage> coll) {
        this.coll = coll;
        mInflater = LayoutInflater.from(context);
    }

    public static interface IMsgViewType {
        int IMVT_COM_MSG = 1;
        int IMVT_TO_MSG = 0;
    }

    private static final String TAG = MyListViewAdapater.class.getSimpleName();

    public int getItemViewType(int position) {
        ChatMessage entity = coll.get(position);
        if (entity.getMsgType() == 1) {
            return IMsgViewType.IMVT_COM_MSG;
        } else if (entity.getMsgType() == 0) {
            return IMsgViewType.IMVT_TO_MSG;
        }
        return IMsgViewType.IMVT_COM_MSG;
    }

    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return coll.size();
    }

    @Override
    public Object getItem(int position) {
        return coll.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ChatMessage entity = coll.get(position);
        int isComMsg = entity.getMsgType();
        ViewHolder holder = null;
        if (convertView == null) {
            if (isComMsg == 0) {
                convertView = mInflater.inflate(R.layout.item_chatting_msg_right, null);
            } else {
                convertView = mInflater.inflate(R.layout.item_chatting_msg_left, null);
            }
            holder = new ViewHolder();
            holder.tv_chat = (TextView) convertView.findViewById(R.id.tv_chatcontent);
            holder.tv_sendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
            holder.iv_userhead = (ImageView) convertView.findViewById(R.id.iv_userhead);
            holder.isComMsg = isComMsg;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();

        }
        holder.tv_chat.setText(entity.content);
        holder.tv_sendTime.setText(entity.sendTime);
        holder.iv_userhead.setImageResource(entity.fromAvatar);
        return convertView;
    }

    static class ViewHolder {
        public TextView tv_chat, tv_sendTime;
        public ImageView iv_userhead;
        public int isComMsg = 0;
    }
}