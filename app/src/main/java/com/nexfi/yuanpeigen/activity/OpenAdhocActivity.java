package com.nexfi.yuanpeigen.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.util.ShellUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 2016/4/5.
 */
public class OpenAdhocActivity extends AppCompatActivity implements View.OnClickListener {
    private List<String> commnandList = new ArrayList<String>();
    private Button btn_openAdhoc;
    private RelativeLayout layout;
    private String release;
    private String model;
    private String sdk;
    private boolean isRoot;
    private Thread thread;
    private Handler handler, mHandler;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_openadhoc);
        initView();
        sdk = android.os.Build.VERSION.SDK;
        model = android.os.Build.MODEL;
        release = android.os.Build.VERSION.RELEASE;
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    Toast.makeText(OpenAdhocActivity.this, "恭喜您成功打开NexFi\n" + msg.obj, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(OpenAdhocActivity.this, "打开失败\n" + msg.obj, Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    private void initView() {
        btn_openAdhoc = (Button) findViewById(R.id.btn_openAdhoc);
        btn_openAdhoc.setOnClickListener(this);
        layout = (RelativeLayout) findViewById(R.id.layout_back);
        layout.setOnClickListener(this);
    }

    private void initDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.dialog_loading, null);
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(v);
        mAlertDialog.setCancelable(false);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_back:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("isAdhoc", false);
                intent.putExtra("isSettingsAdhoc", false);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_openAdhoc:
                if (model.equals("HM 2LTE-CMCC")) {
                    createFile(R.raw.batmand_hm, "batmand_hm");
                    initDialog();
                    mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAlertDialog.dismiss();
                            Toast.makeText(OpenAdhocActivity.this, "恭喜您成功打开NexFi", Toast.LENGTH_SHORT).show();
                        }
                    }, 23000);
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            printLog();
                            isRoot = upgradeRootPermission(getPackageCodePath());
                            Log.e("isRoot", isRoot + "");
                            commnandList.add("su");
                            commnandList.add("sleep 1");
                            commnandList.add("mount -o rw,remount /");
                            commnandList.add("sleep 1");
                            commnandList.add("mkdir -p /data/run/");
                            commnandList.add("sleep 1");
                            commnandList.add("mount -o rw,remount /dev/block/bootdevice/by-name/system /system");
                            commnandList.add("sleep 1");
                            commnandList.add("cp /sdcard/batmand_hm /system/bin/");
                            commnandList.add("sleep 1");
                            commnandList.add("rmmod wlan");
                            commnandList.add("sleep 1");
                            commnandList.add("insmod /system/lib/modules/wlan.ko");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 down");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 up");
                            commnandList.add("sleep 1");
                            commnandList.add("iw dev wlan0 set type ibss");
                            commnandList.add("sleep 1");
                            commnandList.add("iw dev wlan0 ibss join imesh 2437 NOHT fixed-freq 00:11:22:33:44:55");
                            commnandList.add("sleep 1");
                            commnandList.add("iw reg set GY");
                            commnandList.add("sleep 1");
                            commnandList.add("iw dev wlan0 set txpower fixed 30mBm");
                            commnandList.add("sleep 1");
                            commnandList.add("iw phy phy0 set distance 114750");
                            commnandList.add("sleep 1");
                            commnandList.add("iw phy phy0 set coverage 255");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 inet 192.168.2.139 netmask 255.255.255.0");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 up");
                            commnandList.add("sleep 1");
                            commnandList.add("chmod 777 /system/bin/batmand_hm");
                            commnandList.add("sleep 1");
                            commnandList.add("/system/bin/batmand_hm -a 192.168.2.139/32 wlan0");
                            ShellUtils.CommandResult result = ShellUtils.execCommand(commnandList, true);
                            String errorMsg = result.errorMsg;
                            String successMsg = result.successMsg;
                            int resultNum = result.result;
                            Log.e("errorMsg", errorMsg);
                            Log.e("successMsg", successMsg);
                            Log.e("resultNum", resultNum + "");
                            if (successMsg != null) {
                                Message message = handler.obtainMessage();
                                message.obj = successMsg;
                                message.what = 1;
                                handler.sendMessage(message);
                            } else {
                                Message message = handler.obtainMessage();
                                message.obj = errorMsg;
                                message.what = 2;
                                handler.sendMessage(message);
                            }
                        }
                    });
                    thread.start();
                } else if (model.equals("MI 4LTE")) {
                    initDialog();
                    createFile(R.raw.batmand_xiaomi4, "batmand_xiaomi4");
                    mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAlertDialog.dismiss();
                            Toast.makeText(OpenAdhocActivity.this, "恭喜您成功打开NexFi", Toast.LENGTH_SHORT).show();
                        }
                    }, 23000);
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            printLog();
                            isRoot = upgradeRootPermission(getPackageCodePath());
                            Log.e("isRoot", isRoot + "");
                            commnandList.add("su");
                            commnandList.add("sleep 1");
                            commnandList.add("mount -o rw,remount /");
                            commnandList.add("sleep 1");
                            commnandList.add("mkdir -p /data/run/");
                            commnandList.add("sleep 1");
                            commnandList.add("mount -o rw,remount /dev/block/bootdevice/by-name/system /system");
                            commnandList.add("sleep 1");
                            commnandList.add("cp /sdcard/batmand_xiaomi4 /system/bin/");
                            commnandList.add("sleep 1");
                            commnandList.add("rmmod wlan");
                            commnandList.add("sleep 1");
                            commnandList.add("insmod /system/lib/modules/wlan.ko");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 down");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 up");
                            commnandList.add("sleep 1");
                            commnandList.add("iw dev wlan0 set type ibss");
                            commnandList.add("sleep 1");
                            commnandList.add("iw dev wlan0 ibss join imesh 2437 HT20 fixed-freq 00:11:22:33:44:55");
                            commnandList.add("sleep 1");
                            commnandList.add("iw reg set GY");
                            commnandList.add("sleep 1");
                            commnandList.add("iw dev wlan0 set txpower fixed 30mBm");
                            commnandList.add("sleep 1");
                            commnandList.add("iw phy phy1 set distance 114750");
                            commnandList.add("sleep 1");
                            commnandList.add("iw phy phy1 set coverage 255");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 inet 192.168.2.140 netmask 255.255.255.0");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 up");
                            commnandList.add("sleep 1");
                            commnandList.add("chmod 777 /system/bin/batmand_xiaomi4");
                            commnandList.add("sleep 1");
                            commnandList.add("/system/bin/batmand_xiaomi4 -a 192.168.2.140/32 wlan0");
//                            for (int i = 0; i < 2; i++) {
                            ShellUtils.CommandResult result = ShellUtils.execCommand(commnandList, true);
                            String errorMsg = result.errorMsg;
                            String successMsg = result.successMsg;
                            int resultNum = result.result;
                            Log.e("errorMsg", errorMsg);
                            Log.e("successMsg", successMsg);
                            Log.e("resultNum", resultNum + "");
                            if (successMsg != null) {
                                Message message = handler.obtainMessage();
                                message.obj = successMsg;
                                message.what = 1;
                                handler.sendMessage(message);
                            } else {
                                Message message = handler.obtainMessage();
                                message.obj = errorMsg;
                                message.what = 2;
                                handler.sendMessage(message);
                            }
                        }
//                        }
                    });
                    thread.start();
                } else if (model.equals("Nexus 5X")) {
                    initDialog();
                    createFile(R.raw.batmand_nexus5x, "batmand_nexus5x");
                    mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAlertDialog.dismiss();
                            Toast.makeText(OpenAdhocActivity.this, "恭喜您成功打开NexFi", Toast.LENGTH_SHORT).show();
                        }
                    }, 20000);
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            printLog();
                            isRoot = upgradeRootPermission(getPackageCodePath());
                            Log.e("isRoot", isRoot + "");
                            commnandList.add("su");
                            commnandList.add("sleep 1");
                            commnandList.add("mount -o rw,remount /");
                            commnandList.add("sleep 1");
                            commnandList.add("mkdir -p /data/run/");
                            commnandList.add("sleep 1");
                            commnandList.add("mount -o rw,remount /dev/block/platform/soc.0/f9824900.sdhci/by-name/system /system");
                            commnandList.add("sleep 1");
                            commnandList.add("cp /sdcard/batmand_nexus5x /system/bin/");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 down");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 up");
                            commnandList.add("sleep 1");
                            commnandList.add("iw dev wlan0 set power_save off");
                            commnandList.add("sleep 1");
                            commnandList.add("iw dev wlan0 set type ibss");
                            commnandList.add("sleep 1");
                            commnandList.add("iw dev wlan0 ibss join imesh 2437 HT20 fixed-freq 00:11:22:33:44:55");
                            commnandList.add("sleep 1");
                            commnandList.add("iw reg set GY");
                            commnandList.add("sleep 1");
                            commnandList.add("iw dev wlan0 set txpower fixed 30mBm");
                            commnandList.add("sleep 1");
                            commnandList.add("iw phy phy0 set distance 114750");
                            commnandList.add("sleep 1");
                            commnandList.add("iw phy phy0 set coverage 255");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 inet 192.168.2.138 netmask 255.255.255.0");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 up");
                            commnandList.add("sleep 1");
                            commnandList.add("chmod 777 /system/bin/batmand_nexus5x");
                            commnandList.add("sleep 1");
                            commnandList.add("/system/bin/batmand_nexus5x -a 192.168.2.138/32 wlan0");
//                            for (int i = 0; i < 2; i++) {
                            ShellUtils.CommandResult result = ShellUtils.execCommand(commnandList, true);
                            String errorMsg = result.errorMsg;
                            String successMsg = result.successMsg;
                            int resultNum = result.result;
                            Log.e("errorMsg", errorMsg);
                            Log.e("successMsg", successMsg);
                            Log.e("resultNum", resultNum + "");
                            if (successMsg != null) {
                                Message message = handler.obtainMessage();
                                message.obj = successMsg;
                                message.what = 1;
                                handler.sendMessage(message);
                            } else {
                                Message message = handler.obtainMessage();
                                message.obj = errorMsg;
                                message.what = 2;
                                handler.sendMessage(message);
                            }
                        }
//                        }
                    });
                    thread.start();
                } else if (model.equals("CHE-TL00H")) {
                    initDialog();
                    createFile(R.raw.batmand_hm, "batmand_hm");
                    mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAlertDialog.dismiss();
                            Toast.makeText(OpenAdhocActivity.this, "恭喜您成功打开NexFi", Toast.LENGTH_SHORT).show();
                        }
                    }, 20000);
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            printLog();
                            isRoot = upgradeRootPermission(getPackageCodePath());
                            Log.e("isRoot", isRoot + "");
                            commnandList.add("su");
                            commnandList.add("mount -o remount /system");
                            commnandList.add("cp /sdcard/batmand_hm /system/bin");
                            commnandList.add("chmod 755 /system/bin/batmand_hm");
                            commnandList.add("sleep 1");
                            commnandList.add("rmmod /system/lib/modules/wlan.ko");
                            commnandList.add("sleep 1");
                            commnandList.add("insmod /system/lib/modules/wlan.ko");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 down");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 up");
                            commnandList.add("sleep 1");
                            commnandList.add("iw dev wlan0 set type ibss");
                            commnandList.add("sleep 1");
                            commnandList.add("iw dev wlan0 ibss join imesh 2437 NOHT fixed-freq 00:11:22:33:44:55");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 inet 192.168.2.139 netmask 255.255.255.0");
                            commnandList.add("sleep 1");
                            commnandList.add("ifconfig wlan0 up");
                            commnandList.add("sleep 1");
                            commnandList.add("mount -o rw,remount /");
                            commnandList.add("sleep 1");
                            commnandList.add("mkdir -p /data/run/");
                            commnandList.add("sleep 1");
                            commnandList.add("mount -o rw,remount /dev/block/bootdevice/by-name/system /system");
                            commnandList.add("sleep 1");
                            commnandList.add("chmod 777 /system/bin/batmand");
                            commnandList.add("sleep 1");
                            commnandList.add("/system/bin/batmand -a 192.168.2.139/32 wlan0");
                            ShellUtils.CommandResult result = ShellUtils.execCommand(commnandList, true);
                            String errorMsg = result.errorMsg;
                            String successMsg = result.successMsg;
                            int resultNum = result.result;
                            Log.e("errorMsg", errorMsg);
                            Log.e("successMsg", successMsg);
                            Log.e("resultNum", resultNum + "");
                            if (successMsg != null) {
                                Message message = handler.obtainMessage();
                                message.obj = successMsg;
                                message.what = 1;
                                handler.sendMessage(message);
                            } else {
                                Message message = handler.obtainMessage();
                                message.obj = errorMsg;
                                message.what = 2;
                                handler.sendMessage(message);
                            }
                        }
                    });
                    thread.start();
                } else {
                    Toast.makeText(OpenAdhocActivity.this, "暂不支持该机型", Toast.LENGTH_SHORT).show();
                    printLog();
                }
                break;
        }

    }

    private void printLog() {
        Log.e("SDK", sdk);
        Log.e("model", model);
        Log.e("release", release);
    }

    /**
     * 应用程序运行命令获取 Root权限
     */
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    public void createFile(int id, String name) {
        String filePath = android.os.Environment
                .getExternalStorageDirectory().getAbsolutePath() + "/" + name;// 文件路径
        try {
            File dir = new File(android.os.Environment
                    .getExternalStorageDirectory().getAbsolutePath());// 目录路径
            if (!dir.exists()) {// 如果不存在，则创建路径名
                System.out.println("要存储的目录不存在");
                if (dir.mkdirs()) {// 创建该路径名，返回true则表示创建成功
                    System.out.println("已经创建文件存储目录");
                } else {
                    System.out.println("创建目录失败");
                }
            }
            // 目录存在，则将apk中raw中的需要的文档复制到该目录下
            File file = new File(filePath);
            if (!file.exists()) {// 文件不存在
                System.out.println("要打开的文件不存在");
                InputStream ins = getResources().openRawResource(
                        id);// 通过raw得到数据资源
                System.out.println("开始读入");
                FileOutputStream fos = new FileOutputStream(file);
                System.out.println("开始写出");
                byte[] buffer = new byte[8192];
                int count = 0;// 循环写出
                while ((count = ins.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                System.out.println("已经创建该文件");
                fos.close();// 关闭流
                ins.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

