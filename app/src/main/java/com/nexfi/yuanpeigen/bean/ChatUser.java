package com.nexfi.yuanpeigen.bean;

/**
 * Created by Mark on 2016/2/25.
 */

import java.io.Serializable;

public class ChatUser extends ProtocalObj implements Serializable {
    private static final long serialVersionUID = 40L;
    public String account;//
    public String nick = "";//
    public int avatar;
    public String type = "";//绫诲瀷

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return account + "==" + nick + "==" + type;
    }
}
