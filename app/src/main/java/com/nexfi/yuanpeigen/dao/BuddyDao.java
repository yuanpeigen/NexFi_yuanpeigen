package com.nexfi.yuanpeigen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.bean.ChatUser;
import com.nexfi.yuanpeigen.db.BuddyHelper;

import java.util.ArrayList;
import java.util.List;

public class BuddyDao {
    //閹垮秳缍旈弫鐗堝祦鎼?
    private Context context;
    BuddyHelper helper;

    public BuddyDao(Context context) {
        this.context = context;
        helper = new BuddyHelper(context);
    }

    //濞ｈ濮?----------------------------------------------------------------------

    /**
     * 閹跺﹦鏁ら幋椋庢畱娣団剝浼呭ǎ璇插閸掔増鏆熼幑顔肩氨
     *
     * @param
     */
    public void add(ChatUser user) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("contact_ip", user.account);
        values.put("nick_name", user.nick);
        values.put("avatar", user.avatar);
        values.put("type", user.type);
        db.insert("long", null, values);
        db.close();
        context.getContentResolver().notifyChange(
                Uri.parse("content://www.nexfi.com"), null);
    }

    /**
     * 添加消息到数据库
     */
    public void addP2PMsg(ChatMessage msg) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fromIP", msg.fromIP);
        values.put("fromNick", msg.fromNick);
        values.put("fromAvatar", msg.fromAvatar);
        values.put("content", msg.content);
        values.put("toIP", msg.toIP);
        values.put("type", msg.type);
        values.put("msgType", msg.msgType);
        values.put("sendTime", msg.sendTime);
        //TODO
        values.put("fileName",msg.fileName);
        values.put("fileSize",msg.fileSize);
        values.put("fileIcon",msg.fileIcon);
        values.put("isPb",msg.isPb);
        values.put("filePath",msg.filePath);
        db.insert("chatMsgFilePath", null, values);
        db.close();
    }


    //閸掔娀娅?----------------------------------------------------------------------

    /**
     * 根据IP删除
     */
    public void delete(String contact_ip) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int row = db.delete("long", "contact_ip = ?",
                new String[]{contact_ip});
        db.close();
    }


    /**
     * 根据IP删除聊天信息
     */
    public void deleteP2PMsg(String fromIP) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int row = db.delete("chatMsgFilePath", "fromIP = ?",
                new String[]{fromIP});
        db.close();
    }



    //删除所有用户信息
    public void deleteAll() {
        SQLiteDatabase db = helper.getWritableDatabase();
        int row = db.delete("long", null, null);
        db.close();
    }


    //删除所有聊天信息
    public void deleteP2PMsgAll() {
        SQLiteDatabase db = helper.getWritableDatabase();
        int row = db.delete("chatMsgFilePath", null, null);
        db.close();
    }




    /**
     * 查找所有用户
     *
     * @return
     */
    public List<ChatUser> findAll() {

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("long", null, null, null, null, null, null);
        List<ChatUser> mDatas = new ArrayList<ChatUser>();
        List<ChatUser> mList = new ArrayList<ChatUser>();
        while (cursor.moveToNext()) {
            ChatUser user = new ChatUser();
            user.account = cursor.getString(cursor.getColumnIndex("contact_ip"));
            user.nick = cursor.getString(cursor.getColumnIndex("nick_name"));
            user.type = cursor.getString(cursor.getColumnIndex("type"));
            user.avatar = cursor.getInt(cursor.getColumnIndex("avatar"));
            mList.add(user);
        }
        for (int i = 0; i < mList.size(); i++) {
            if (!mDatas.contains(mList.get(i))) {
                mDatas.add(mList.get(i));
            }
        }
        cursor.close();
        db.close();
        return mDatas;
    }


    /**
     * 查找所有聊天信息
     *
     * @return
     */
    public List<ChatMessage> findP2PMsgAll() {

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("chatMsgFilePath", null, null, null, null, null, null);
        List<ChatMessage> mDatas = new ArrayList<ChatMessage>();
        List<ChatMessage> mList = new ArrayList<ChatMessage>();
        while (cursor.moveToNext()) {
            ChatMessage msg = new ChatMessage();
            msg.fromIP = cursor.getString(cursor.getColumnIndex("fromIP"));
            msg.fromNick = cursor.getString(cursor.getColumnIndex("fromNick"));
            msg.type = cursor.getString(cursor.getColumnIndex("type"));
            msg.content = cursor.getString(cursor.getColumnIndex("content"));
            msg.fromAvatar = cursor.getInt(cursor.getColumnIndex("fromAvatar"));
            msg.toIP = cursor.getString(cursor.getColumnIndex("toIP"));
            msg.msgType=cursor.getInt(cursor.getColumnIndex("msgType"));
            msg.sendTime = cursor.getString(cursor.getColumnIndex("sendTime"));
            //TODO
            msg.fileName = cursor.getString(cursor.getColumnIndex("fileName"));
            msg.fileSize = cursor.getLong(cursor.getColumnIndex("fileSize"));
            msg.fileIcon = cursor.getInt(cursor.getColumnIndex("fileIcon"));
            msg.isPb = cursor.getInt(cursor.getColumnIndex("isPb"));
            msg.filePath=cursor.getString(cursor.getColumnIndex("filePath"));
            mList.add(msg);
        }
        for (int i = 0; i < mList.size(); i++) {
            if (!mDatas.contains(mList.get(i))) {
                mDatas.add(mList.get(i));
            }
        }
        cursor.close();
        db.close();
        return mDatas;
    }


    //閺屻儴顕楁稉鈧稉?--------------------------------------------------------------------------------------


    /**
     * 根据IP查找是否有同样的用户数据
     *
     * @param contact_ip
     */
    public boolean find(String contact_ip) {
        boolean result = false;
        SQLiteDatabase db = helper.getReadableDatabase();
//		Cursor cursor = db.query("bao", null, "contact_ip = ?",
//				new String[] { contact_ip }, null, null, null);
        Cursor cursor = db.query("long", null, "contact_ip = ?", new String[]{contact_ip}, null, null, null);

        if (cursor.moveToNext()) {
            result = true;
        }
        cursor.close();
        db.close();
        return result;
    }


    /**
     * 閺嶈宓両P閺屻儴顕楅懕濠傘亯閺佺増宓侀弰顖氭儊閸︺劍鏆熼幑顔肩氨闁插矂娼?
     *
     * @param contact_ip
     */
//	public boolean find(String fromIP) {
//		boolean result = false;
//		SQLiteDatabase db = helper.getReadableDatabase();
////		Cursor cursor = db.query("bao", null, "contact_ip = ?",
////				new String[] { contact_ip }, null, null, null);
//		Cursor cursor = db.query("chatMessa", null,"fromIP = ?", new String[] { fromIP }, null, null, null);
//
//		if (cursor.moveToNext()) {
//			result = true;
//		}
//		cursor.close();
//		db.close();
//		return result;
//	}
}

	
	
