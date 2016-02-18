package com.nexfi.yuanpeigen.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nexfi.yuanpeigen.nexfi.R;

/**
 * Created by Mark on 2016/2/15.
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv;
    private LinearLayout update;
    private Dialog mDialog;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView() {
        iv = (ImageView) findViewById(R.id.back_about);
        update = (LinearLayout) findViewById(R.id.update);
        update.setOnClickListener(this);
        iv.setOnClickListener(this);
    }


    private void initDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.dialog_update, null);
        mDialog = new AlertDialog.Builder(AboutActivity.this).create();
        mDialog.show();
        mDialog.getWindow().setContentView(v);
        mButton = (Button) v.findViewById(R.id.btn_ensure2);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_about:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.update:
                initDialog();
                break;
        }
    }
}
