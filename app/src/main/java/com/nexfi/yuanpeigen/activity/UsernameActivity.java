package com.nexfi.yuanpeigen.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi.R;
import com.nexfi.yuanpeigen.util.UserInfo;

/**
 * Created by Mark on 2016/3/2.
 */
public class UsernameActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView finish;
    private EditText username;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);
        finish = (TextView) findViewById(R.id.tv_finish);
        username = (EditText) findViewById(R.id.et_username);
        finish.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_finish:
                if (!TextUtils.isEmpty(username.getText())) {
                    UserInfo.saveUsername(UsernameActivity.this, username.getText().toString());
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UsernameActivity.this, LoginActivity.class);
                    intent.putExtra("name", username.getText().toString());
                    setResult(2, intent);
                    UsernameActivity.this.finish();
                } else {
                    Toast.makeText(this, "请输入昵称", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
