package com.nexfi.yuanpeigen.weight;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi.R;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Mark on 2016/2/16.
 */
public class DialogFragment_ModifyUsername extends DialogFragment implements View.OnClickListener {
    private EditText mEditText;
    private Button btn_ensure, btn_cancel;
    private Dialog mDialog;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_modifyusername, null);
        mEditText = (EditText) view.findViewById(R.id.et_modify);
        btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_ensure = (Button) view.findViewById(R.id.btn_ensure);
        btn_ensure.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        builder.setView(view);
        mDialog = builder.show();
        return mDialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                mDialog.dismiss();
                break;
            case R.id.btn_ensure:
                modifyUsername();
                break;
        }
    }

    private void modifyUsername() {
        if (!TextUtils.isEmpty(mEditText.getText())) {
            try {
                File file = new File(getActivity().getFilesDir(), "userinfo.txt");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(("".getBytes()));
                fos.flush();
                fos.write((mEditText.getText().toString()).getBytes());
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        } else {
            Toast.makeText(getActivity(), "请输入新昵称", Toast.LENGTH_SHORT).show();
        }
    }
}

