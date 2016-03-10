package com.nexfi.yuanpeigen.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BuddyHelper extends SQLiteOpenHelper {

    public BuddyHelper(Context context) {
        super(context, "budd.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table long (_id integer primary key autoincrement,contact_ip varchar(20),nick_name varchar(20),type varchar(20),avatar Integer(20))");
        db.execSQL("create table chatMesg (_id integer primary key autoincrement,fromIP varchar(20),fromNick varchar(20),fromAvatar Integer(20),toIP varchar(20),content varchar(20),type varchar(20),isComMeg Integer(20),sendTime varchar(20))");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
