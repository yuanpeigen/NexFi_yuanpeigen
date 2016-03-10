package com.nexfi.yuanpeigen.bean;

import java.io.Serializable;

public class ChatMessage extends ProtocalObj implements Serializable {
    //public String type = QQMessageType.MSG_TYPE_CHAT_P2P;//娑堟伅绫诲瀷-鑱婂ぉ锛屽洜涓轰互鍚庡彲鑳戒細鏈夌兢鑱?
    private static final long serialVersionUID = 35L;
    public String fromIP;//璋佸彂閫佺殑
    public String fromNick;//鏄电О
    public int fromAvatar;//澶村儚
    public String content; // 娑堟伅鍐呭

    public String toIP; // 鍙戦€佺粰璋?

    public String type;
    public int isComMeg = 0;
    public String sendTime; //鏃堕棿

    public int getMsgType() {
        return isComMeg;
    }

    public void setMsgType(int isComMsg) {
        isComMeg = isComMsg;
    }
}
