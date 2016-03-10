package com.nexfi.yuanpeigen.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.nexfi.yuanpeigen.bean.ChatMessage;
import com.nexfi.yuanpeigen.bean.ChatUser;
import com.nexfi.yuanpeigen.dao.BuddyDao;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;


//鐩戝惉娑堟伅
public class ReceService extends Service {
    public String localIP;//鏈満IP
    BuddyDao dao;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //閼惧嘲褰囬惂璇茬秿妞ょ敻娼版导鐘虹箖閺夈儳娈戦張顒佹簚IP
        SharedPreferences preferences = getSharedPreferences("IP", Context.MODE_PRIVATE);
        localIP = preferences.getString("useIP", null);
        dao = new BuddyDao(getApplicationContext());
        init();
        initReceUDP();
    }


    //单对单聊天消息的监听
    private void initReceUDP() {
        new Thread() {
            public void run() {

                try {
                    byte[] buf = new byte[1024];
                    DatagramSocket ds = new DatagramSocket(10000);//开始监视12345端口
                    DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName("192.168.1.255"), 10000);//创建接收数据报的实例
                    while (true) {
                        Log.e("TAG", "------while (true)---service");
                        ds.receive(dp);//阻塞,直到收到数据报后将数据装入IP中
                        //转化
                        ChatMessage msg = new ChatMessage();
                        ChatMessage fromXml = (ChatMessage) msg.fromXml(new String(dp.getData()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


//                String receUDP = SocketUtils.receUDP();
//                Log.e("TAG","-----run---------service");

                //存储到数据库
//                dao.addP2PMsg(fromXml);

                //然后，当聊天界面显示数据的时候可以从数据库中取
            }
        }.start();
    }


 /*   @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        localIP = intent.getStringExtra("LOCAL_IP");
        Log.e("TAG", localIP + "---localIP==");
        return super.onStartCommand(intent, flags, startId);
    }
*/
    private void init() {
        new Thread() {
            public void run() {
                try {
                    MulticastSocket ds = new MulticastSocket(8005);
                    InetAddress receiveAddress = InetAddress.getByName("224.0.0.105");
                    ds.joinGroup(receiveAddress);
                    byte[] buff = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buff, buff.length, receiveAddress, 8005);
//                    System.out.println("run-------------");

                    while (true) {
                        ds.receive(dp);

//                        Log.e("TAG", "receive===============" + new String(dp.getData()));


                        ChatUser user = new ChatUser();
                        ChatUser fromXml = (ChatUser) user.fromXml(new String(dp.getData()));
//                        Log.e("TAG", "deserializable===============" + fromXml.toString());


//                        Message msg = handler.obtainMessage();
//                        msg.obj = fromXml.account + "---------------------上线";
//                        msg.what = 1;
//                        handler.sendMessage(msg);


//		                ChatMessage chat_msg=new ChatMessage();
//		                ChatMessage fromXmlMsg = (ChatMessage) chat_msg.fromXml(new String(dp.getData()));
//
                        if ("online".equals(fromXml.type)) {
//                            Log.e("TAG","---online==="+fromXml.account);
//                            Log.e("TAG","---online==="+(!localIP.equals(fromXml.account)));
                            //
                            if (!localIP.equals(fromXml.account)) {//鍒ゆ柇鏄惁鏄湰鏈篒P
//                                Log.e("TAG",localIP+"---equals==="+fromXml.account);
//                                Log.e("TAG",  "---find===" + dao.find(fromXml.account));

                                if (!dao.find(fromXml.account)) {//鍒ゆ柇鏁版嵁搴撲腑鏄惁宸叉湁璇ョ敤鎴?
//                                    Log.e("TAG","---equ==="+fromXml.account);
                                    dao.add(fromXml);
//                                    System.out.println("---tianjia---------------");
                                }
                            }

                        } else if ("offine".equals(fromXml.type)) {
                            //涓嬬嚎娑堟伅,浠庢暟鎹簱涓垹闄よ鐢ㄦ埛鏁版嵁
                            dao.delete(fromXml.account);
                        }

//                		Object obj =SerializableUtils.DeserializeObject(buff);
//                		Log.e("TAG","deserializable==============="+obj);
//                		if(null!=obj){
//                			ChatUser user=(ChatUser) obj;
//                			Log.e("TAG",user.toString());
//                		}

//                		derializable(buff);
//                		ByteArrayInputStream bais = new ByteArrayInputStream(buff,0,dp.getData().length);
//            			ObjectInputStream ois = new ObjectInputStream(bais);
//            			Object obj = ois.readObject();
//            			while((obj=ois.readObject())!=null) {
//                          ChatUser s = (ChatUser)obj;
//                          System.out.println(s.toString());
//                        }
//            			Log.e("TAG","deserializable==============="+obj);
//            			bais.close();
//            			ois.close();


                        //鎺ユ敹鍒版秷鎭箣鍚庯紝闇€瑕佹牴鎹秷鎭被鍨嬭繖涓瓧娈垫潵杩涜鐩稿簲鐨勫鐞?
                        //涓婄嚎锛岃亰澶╋紝涓嬬嚎
                        //鎵€浠ヤ箣鍓嶅垱寤虹殑ChatUser鏁版嵁搴撻渶瑕佹洿鏀癸紝鍘熸潵鍙湁涓変釜瀛楁锛岀幇鍦ㄩ渶瑕佹洿鏀逛负ChatMessage鏁版嵁搴?
                        //杩樻槸鍏堢敤涓や釜琛ㄥ惂锛屼竴涓鎴蜂俊鎭暟鎹?
//		                ChatMessage chat_msg=new ChatMessage();
//		                ChatMessage fromXmlMsg = (ChatMessage) chat_msg.fromXml(new String(dp.getData()));
//		                if("online".equals(fromXmlMsg.type)){
//		                	//濡傛灉鏄笂绾挎秷鎭紝灏卞皢鐢ㄦ埛淇℃伅瀛樺偍鍒版暟鎹簱锛屼絾鏄笉閲嶅娣诲姞
//
//		                }else if("offine".equals(fromXmlMsg.type)){
//		                	//涓嬬嚎娑堟伅锛屼粠鏁版嵁搴撲腑鍒犻櫎鐢ㄦ埛淇℃伅
//
//		                }else if("chatp2p".equals(fromXmlMsg.type)){
//		                	//鍗曞鍗曡亰澶?
//
//		                }else if("chatroom".equals(fromXmlMsg.type)){
//		                	//鑱婂ぉ瀹?
//
//		                }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//			private void derializable(byte[] buf) throws IOException,
//					ClassNotFoundException {
//				ByteArrayInputStream bais=new ByteArrayInputStream(buf);
//				ObjectInputStream ois=new ObjectInputStream(bais);
//				ChatUser s = (ChatUser)ois.readObject();
//				Log.e("TAG",s.toString());
//			}
        }.start();

    }


    private void initRece() {
        //閹恒儲鏁瑰☉鍫熶紖
        new Thread() {
            public void run() {

                try {
                    MulticastSocket ds = new MulticastSocket(8006);
                    InetAddress receiveAddress = InetAddress.getByName("224.0.0.100");
                    ds.joinGroup(receiveAddress);

                    byte buf[] = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length, receiveAddress, 8006);
                    System.out.println("=============================rubn===");
                    //BuddyDao dao=new BuddyDao(getApplicationContext());
                    while (true) {
                        ds.receive(dp);
//		            	String[] dps=(new String(dp.getData())).split("#");
//		            	System.out.println(dps.length+"==="+dps.toString());
//		            	System.out.println(dps[0]+"=============================true==="+dps[1]);
//		            	System.out.println(dps[1]+"--------------------涓婄嚎-");
//		            	Log.e("TAG", "---------receive====================");
                        //ChatUser user = (ChatUser) SerializableUtils.DeserializeObject(buf);
//		            	Object obj =(Object) SerializableUtils.DeserializeObject(buf);
//		            	ChatUser user=null;
//                		if(null!=obj){
//                			user=(ChatUser) obj;
//                			System.out.println(user.toString());
//                		}


                        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                        ObjectInputStream ois = new ObjectInputStream(bais);
                        ChatUser user = (ChatUser) ois.readObject();
                        Log.e("TAG", "---------receive====================");

//		            	System.out.println(user.account+"---------------------");
//		            	Log.e("TAG", user.account+"---------"+user.nick+"----"+user.type);
//                        Message msg = handler.obtainMessage();
//                        msg.obj = user.account + "---------------------涓婄嚎浜?";
//                        msg.what = 1;
//                        handler.sendMessage(msg);


//		            	System.out.println("---nihaye---------------");
//		            	if(!localIP.equals(user.account)){//娑撳秴鐡ㄩ崒銊︽拱閺堣櫣鏁ら幋閿嬫殶閹癸拷
//		            		if(!dao.find(user.account)){//闁灝鍘ら柌宥咁槻鐎涙ê鍋?
////			                	ChatUser user=new ChatUser();
////			                	System.out.println("---chuangjian---------------");
////				            	user.nick=dps[0];
////				            	user.account=dps[1];
////				            	user.type=dps[2];
//		            			dao.add(user);
//		            			System.out.println("---tianjia---------------");
//		            		}
//		            	}

                        //閺嶈宓佸☉鍫熶紖缁鐎烽崠鍝勫焼婢跺嫮鎮?
                        //if(QQMessageType.MSG_TYPE_ONLINE.equals(dps[2])){
                        //閺勵垳娅ヨぐ鏇熺Х閹拷
                        //閺嶈宓両P閺屻儴顕楅弫鐗堝祦鎼存挷鑵戦弰顖氭儊閺堝顕氶弫鐗堝祦
//		            		if(!dao.find(user.account)){
//		            			dao.add(user);
//		            		}
//		            		Log.e("TAG","login==================================");
//		            	}else if(QQMessageType.MSG_TYPE_OFFINE.equals(dps[2])){
//		            		//娑撳鍤庡☉鍫熶紖
//		            		//dao.delete(dps[1]);
//		            		Log.e("TAG","offine==================================");
//		            	}

                        //閹恒儲鏁归崚鐗堢Х閹垰鎮楅弴瀛樻煀UI
//		                Message msg=handler.obtainMessage();
//		                msg.obj=new String(dp.getData());
//		                msg.what=1;
//		                handler.sendMessage(msg);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }.start();
    }

}
