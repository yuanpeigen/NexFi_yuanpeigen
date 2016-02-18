package com.nexfi.yuanpeigen.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi.R;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Mark on 2016/2/4.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mEditText;
    private Button btn_ensure;
    private Dialog mDialog;
    boolean isFirstIn = false;
    private static final String SHAREDPREFERENCES_NAME = "first_pref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiity_login);
        initConfigurationInformation();
        initView();
    }

    private void initView() {
        mDialog = new Dialog(LoginActivity.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.show();
        Window win = mDialog.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);
        mDialog.getWindow().setContentView(R.layout.dialog_username);
        mDialog.setCancelable(false);
        mEditText = (EditText) mDialog.getWindow().findViewById(R.id.et);
        btn_ensure = (Button) mDialog.getWindow().findViewById(R.id.btn_ensure);
        btn_ensure.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ensure:
                if (!TextUtils.isEmpty(mEditText.getText())) {
                    saveUserInfo(LoginActivity.this, mEditText.getText().toString(), null);
                    LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    LoginActivity.this.finish();
                } else {
                    Toast.makeText(this, "请输入昵称", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void initConfigurationInformation() {
        SharedPreferences preferences = getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        isFirstIn = preferences.getBoolean("isFirstIn", true);
        if (!isFirstIn) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            LoginActivity.this.startActivity(intent);
            LoginActivity.this.finish();
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstIn", false);
        editor.commit();
    }

    public static boolean saveUserInfo(Context context, String username, String password) {
        try {
            File file = new File(context.getFilesDir(), "userinfo.txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write((username + "##" + password).getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}

