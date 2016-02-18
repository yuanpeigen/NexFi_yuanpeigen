package com.nexfi.yuanpeigen.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.weight.Fragment_nearby;
import com.nexfi.yuanpeigen.weight.Fragment_settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private Fragment_nearby fragment_nearby;
    private Fragment_settings fragment_settings;
    private ImageView iv_nearby, iv_settings;
    private Handler handler;
    private boolean isExit;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFragment();
        initView();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                isExit = false;
            }
        };
    }

    private void initFragment() {
        mFragmentManager = getFragmentManager();
        fragment_nearby = new Fragment_nearby();
        fragment_settings = new Fragment_settings();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.add(R.id.container, fragment_settings)
                .add(R.id.container, fragment_nearby)
                .hide(fragment_settings).commit();
    }


    private void initView() {
        iv_nearby = (ImageView) findViewById(R.id.iv_nearbay);
        iv_settings = (ImageView) findViewById(R.id.iv_settings);
        iv_nearby.setOnClickListener(this);
        iv_settings.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_nearbay:
                mFragmentManager.beginTransaction()
                        .show(fragment_nearby).hide(fragment_settings).commit();
                break;

            case R.id.iv_settings:
                mFragmentManager.beginTransaction()
                        .show(fragment_settings).hide(fragment_nearby).commit();
                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isExit) {
                isExit = true;
                handler.sendEmptyMessageDelayed(0, 1500);
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public static String getTxtFileInfo(Context context) {
        try {
            File file = new File(context.getFilesDir(), "userinfo.txt");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String content = br.readLine();
            Map<String, Object> map = new HashMap<String, Object>();
            String[] contents = content.split("##");
            map.put("username", contents[0]);
//            map.put("password", contents[1]);
            fis.close();
            br.close();
            String username = (String) map.get("username");
            return username;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
