package com.yongyida.yydrobotcv.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigkoo.pickerview.adapter.ArrayWheelAdapter;


import com.bigkoo.pickerview.lib.WheelView;
import com.yongyida.yydrobotcv.R;
import com.yongyida.yydrobotcv.RegisterActivity;
import com.yongyida.yydrobotcv.useralbum.User;
import com.yongyida.yydrobotcv.utils.CommonUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import dou.utils.ToastUtil;

/**
 * @author Brandon on 2018/3/15
 * update 18/4/10
 **/
public class RegisterVipFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = RegisterVipFragment.class.getSimpleName();
    TextView btnFinish;
    User registerUser;
    WheelView vipChoice;
    ArrayList <String> arrayList;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        registerUser = new User();
        View view = inflater.inflate(R.layout.enroll__vipinfo_fragment,container,false);
//        return super.onCreateView(inflater, container, savedInstanceState);
         vipChoice = view.findViewById(R.id.vip_choice);
        btnFinish = view.findViewById(R.id.btn_finsih);
        btnFinish.setOnClickListener(this);
        arrayList = new ArrayList();
        arrayList.add("VIP0");
        arrayList.add("VIP1");
        arrayList.add("VIP2");
        arrayList.add("VIP3");
        arrayList.add("VIP4");
        arrayList.add("VIP5");
        arrayList.add("VIP6");

        vipChoice.setAdapter(new ArrayWheelAdapter(arrayList));// 设置"年"的显示数据
        vipChoice.setLabel("");// 添加文字
        vipChoice.setCurrentItem(0);// 初始化时显示的数据
        vipChoice.setGravity(Gravity.CENTER);
        vipChoice.setTextSize(16);
        vipChoice.setTextColorCenter(getResources().getColor(R.color.colorTextWrite));
        vipChoice.setCyclic(true);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e(TAG,"onHiddenChanged"+hidden);
        if (hidden){

        }else {

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_finsih:
                String vipType = arrayList.get(vipChoice.getCurrentItem());
                Log.e(TAG,"vip " + vipType);
                registerUser.setVipRate(vipType);
                ((RegisterActivity)this.getActivity()).setRegisterUser(registerUser,3);
                if (((RegisterActivity)this.getActivity()).getRegisterUser().getPersonId().equals("-1")){
                    new ToastUtil(this.getActivity()).showSingletonToast("还没有注册人脸 ！");
                    ((RegisterActivity)this.getActivity()).registerCamera(null);

                }else if (!((RegisterActivity)this.getActivity()).isUserNameOk()){
                    new ToastUtil(this.getActivity()).showSingletonToast("人名不符合规则！");
                    ((RegisterActivity)this.getActivity()).registerBaseInfo(null);
                }else {
                    long ret = ((RegisterActivity)this.getActivity()).doEnd();
                    if (ret>0){
                        ((RegisterActivity)this.getActivity()).setResult(RegisterActivity.ADD_SUCCESS_RESULT_CODE);
                        this.getActivity().finish();
                    }else {

                    }
                    Log.e(TAG,"最后生成 " + ret);
                }

                break;
        }
    }

    // 名字规范判定
    public boolean checkName(String name){
        boolean ret = false;
        int bytLength  = 0;

        try {
            bytLength = name.getBytes("gbk").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        //
        if (bytLength>12){
            Log.e(TAG,"超出限制");
            return false;
        }

        if (CommonUtils.isMatchName(name)){// 判断特殊字符

        }


        return ret;
    }


}
