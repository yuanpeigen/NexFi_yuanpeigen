package com.nexfi.yuanpeigen.weight;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nexfi.yuanpeigen.activity.AboutActivity;
import com.nexfi.yuanpeigen.nexfi.R;

/**
 * Created by Mark on 2016/2/4.
 */
public class Fragment_settings extends Fragment implements View.OnClickListener {

    private LinearLayout modify_username, about;
    private View view;
    private DialogFragment_ModifyUsername dialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        modify_username = (LinearLayout) view.findViewById(R.id.modify_username);
        about = (LinearLayout) view.findViewById(R.id.about);
        modify_username.setOnClickListener(this);
        about.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
            case R.id.modify_username:
                dialog = new DialogFragment_ModifyUsername();
                dialog.show(getFragmentManager(), "dialog");
                break;
        }
    }
}
