package com.yongyida.yydrobotcv;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.yongyida.yydrobotcv.customview.ExitDialog;
import com.yongyida.yydrobotcv.fragment.RegisterBaseInfoFragment;
import com.yongyida.yydrobotcv.fragment.RegisterCameraFragment;
import com.yongyida.yydrobotcv.fragment.RegisterVipFragment;
import com.yongyida.yydrobotcv.tts.TTSManager;
import com.yongyida.yydrobotcv.useralbum.User;
import com.yongyida.yydrobotcv.useralbum.UserDataSupport;
import com.yongyida.yydrobotcv.utils.CommonUtils;


import java.io.IOException;

public class RegisterActivity extends FragmentActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    public static final int ADD_SUCCESS_RESULT_CODE = 1;//不做设定的情况下返回的值是0
    FrameLayout registerFrame;
    FragmentManager fm;
    FragmentTransaction ft;
    //
    RegisterVipFragment rVipInfoFrame;

    RegisterCameraFragment rCameraInfoFrame;
    RegisterBaseInfoFragment rBaseInfoFrame;
    User registerUser;
    int currentStep = 0;
    UserDataSupport userDataSupport;

    ExitDialog exitDialog;
    ExitDialog checkCameraFrame;

    int whichCheck = 1;

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initSoundPool();
        setContentView(R.layout.main_enroll_activity);
        registerFrame = findViewById(R.id.register_frame);
        registerFrame.removeAllViews();
        fm = getFragmentManager();
        registerUser = new User();
        registerUser.setUserName("Brandon");//设定默认值
        registerUser.setPersonId("-1");
        userDataSupport =  UserDataSupport.getInstance(this);
        rVipInfoFrame = new RegisterVipFragment();
        rCameraInfoFrame = new RegisterCameraFragment();
        rBaseInfoFrame = new RegisterBaseInfoFragment();
        registerCamera(null);
        exitDialog = new ExitDialog(this, R.style.custom_dialog, new ExitDialog.OnCloseListener() {
            @Override
            public void clickConfirm() {
                if (!registerUser.getPersonId().equals("-1")&&!registerUser.getPersonId().equals("-2")) {
                    rCameraInfoFrame.removePersonId(registerUser.getPersonId());
                }
                exitDialog.dismiss();
                RegisterActivity.this.finish();
            }
            @Override
            public void clickCancel() {
                exitDialog.dismiss();
            }
        });
        checkCameraFrame = new ExitDialog(this, R.style.custom_dialog, new ExitDialog.OnCloseListener() {
            @Override
            public void clickConfirm() {
                if (!registerUser.getPersonId().equals("-1")) {
                    rCameraInfoFrame.removePersonId(registerUser.getPersonId());
                }
                registerUser.setPersonId("-1");
                checkCameraFrame.dismiss();
                rCameraInfoFrame.reInit();
                registerCamera(null);
            }

            @Override
            public void clickCancel() {
                checkCameraFrame.dismiss();
            }
        },"确认重新录入人脸吗？");

    }

    public void registerBack(View view) {
        exitDialog.show();
    }


    //跳转到录入fragment
    public void registerCamera(View view) {
        if (registerUser.getPersonId().equals("-1")) {
            whichCheck = 1;
            currentStep = 1;
            ft = fm.beginTransaction();
            if (!rBaseInfoFrame.isHidden()) {
                ft.hide(rBaseInfoFrame);
            }
            if (!rVipInfoFrame.isHidden()) {
                ft.hide(rVipInfoFrame);
            }

            if (rCameraInfoFrame.isAdded()) {
                ft.show(rCameraInfoFrame).commit();
            } else {
                ft.add(R.id.register_frame, rCameraInfoFrame).show(rCameraInfoFrame).commit();
            }

        }else {
            checkCameraFrame.show();
        }

    }

    //跳转到基础信息录入fragment
    public void registerBaseInfo(View view) {
        whichCheck = 2;
        currentStep = 2;
        ft = fm.beginTransaction();

        if (!rCameraInfoFrame.isHidden()) {
            ft.hide(rCameraInfoFrame);
        }
        if (!rVipInfoFrame.isHidden()) {
            ft.hide(rVipInfoFrame);
        }

        if (rBaseInfoFrame.isAdded()) {
            ft.show(rBaseInfoFrame).commit();
        } else {
            ft.add(R.id.register_frame, rBaseInfoFrame).show(rBaseInfoFrame).commit();
        }
    }

    //跳转到vip信息录入界面
    public void registerVipRate(View view) {
        whichCheck = 3;
        currentStep = 3;
        ft = fm.beginTransaction();
        if (!rCameraInfoFrame.isHidden()) {
            ft.hide(rCameraInfoFrame);
        }
        if (!rBaseInfoFrame.isHidden()) {
            ft.hide(rBaseInfoFrame);
        }

        if (rVipInfoFrame.isAdded()) {
            ft.show(rVipInfoFrame).commit();
        } else {
            ft.add(R.id.register_frame, rVipInfoFrame).show(rVipInfoFrame).commit();
        }
    }

    public User getRegisterUser() {
        return registerUser;
    }

    public boolean isUserNameOk(){
        boolean isOk = false;
        String userName = registerUser.getUserName();
        if (!TextUtils.isEmpty(userName)&& CommonUtils.isMatchName(userName))
            isOk = true;
        return  isOk;
    }

    //每一步完成不同的信息录入
    public void setRegisterUser(User registerUser, int step) {
        switch (step) {
            case 1:
                this.registerUser.setPersonId(registerUser.getPersonId());
                this.registerUser.setGender(registerUser.getGender());
                this.registerUser.setAge(registerUser.getAge());
                break;
            case 2:
                this.registerUser.setUserName(registerUser.getUserName());
                this.registerUser.setGender(registerUser.getGender());
                this.registerUser.setPhoneNum(registerUser.getPhoneNum());
                this.registerUser.setBirthDay(registerUser.getBirthDay());
                break;
            case 3:
                this.registerUser.setVipRate(registerUser.getVipRate());
                break;
        }
    }

    //最后注册到数据库
    public long doEnd() {
        long ret = userDataSupport.insertUser(registerUser);
        return ret;
    }

    private SoundPool soundPool;
    //使用声音的提示
    public void initSoundPool(){
        AudioAttributes aab = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(aab)
                .build();
        try {
            AssetFileDescriptor afdHutter1 = getResources().getAssets().openFd("amera_hutter_001.wav");
            AssetFileDescriptor afdHutter2 = getResources().getAssets().openFd("pubtest.mp3");
            AssetFileDescriptor afdHutter3 = getResources().getAssets().openFd("warning.mp3");


            soundPool.load(afdHutter1,1);
            soundPool.load(afdHutter2,1);
            soundPool.load(afdHutter3,1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }

    public void playSound(int type){
        soundPool.play(type, 0.5f, 0.5f, 0, 0, 1.0f);
        Log.e(TAG,"发声，咔嚓！");
    }

    public void pauseSound(){
        soundPool.autoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                Log.d(TAG, "menu key clicked!");
                Toast.makeText(RegisterActivity.this, "菜单键点击", Toast.LENGTH_SHORT).show();
                break;

            case KeyEvent.KEYCODE_BACK:
                Log.d(TAG, "back key clicked!");
                exitDialog.show();
//                Toast.makeText(RegisterActivity.this, "返回键点击", Toast.LENGTH_SHORT).show();
                return  false;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }
}
