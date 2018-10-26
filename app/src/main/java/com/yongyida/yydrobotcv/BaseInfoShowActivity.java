package com.yongyida.yydrobotcv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yongyida.yydrobotcv.useralbum.User;
import com.yongyida.yydrobotcv.useralbum.UserDataSupport;

import java.io.File;

public class BaseInfoShowActivity extends AppCompatActivity {

    private final String TAG = BaseInfoShowActivity.class.getSimpleName();
    public static final int DELETE_SUCCESS_RESULT_CODE = 1;
    public static final int DELETE_FAILED_RESULT_CODE = -1;

    ImageView ivPortraitImage;
    TextView tvVipRate;
    TextView tvName;
    TextView tvBirthday;
    TextView tvGender;
    TextView tvVisitedCount;
    TextView tvPhoneNum;
    User user;
    UserDataSupport dataSupport;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_user_info_activity);
        user = (User) getIntent().getSerializableExtra("one_user");
        dataSupport = UserDataSupport.getInstance(this);
        initView();
        initData();
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        BaseInfoShowActivity.this.finish();
                        break;
                }
                return true;
            }
        });

    }

    public void initData() {
        Bitmap bigMap;

        //圆形的头像


        File avaterFile = new File(this.getCacheDir() + "/" + user.getPersonId() + ".jpg");
        if (avaterFile.exists()) {
            bigMap = BitmapFactory.decodeFile(avaterFile.getAbsolutePath());
        } else {
            bigMap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_success);
        }

        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bigMap);
        roundedBitmapDrawable.setCircular(true);
        ivPortraitImage.setImageDrawable(roundedBitmapDrawable);
        tvName.setText("姓名：" + user.getUserName());
        tvGender.setText("性别：" + user.getGender());
        tvPhoneNum.setText("电话：" + user.getPhoneNum());
        tvBirthday.setText("生日：" + user.getBirthDay());
        String identifyCount = user.getIdentifyCount();
        if (TextUtils.isEmpty(identifyCount)){
            identifyCount = "0";
        }
        tvVisitedCount.setText("被访问次数：" + identifyCount);

        tvVipRate.setText(user.getVipRate());
    }

    public void baseinfoBace(View view) {
        finish();
    }


    public void baseinfoDelete(View view) {
        long ret = dataSupport.deleteUser(user.getPersonId());
        if (ret > 0) {
            setResult(DELETE_SUCCESS_RESULT_CODE);
            mHandler.sendEmptyMessageDelayed(1,2000);
            makeText(this,"删除成功");
        } else {
            setResult(DELETE_FAILED_RESULT_CODE);
            mHandler.sendEmptyMessageDelayed(2,2000);
            makeText(this,"删除失败");
        }

        Log.e(TAG, "删除的返回结果" + ret);
    }
    Handler mHandler;

    public void initView() {
        ivPortraitImage = findViewById(R.id.base_show_head_img);
        tvVipRate = findViewById(R.id.base_show_vip_rate);
        tvName = findViewById(R.id.base_show_name);
        tvBirthday = findViewById(R.id.base_show_birthday);
        tvGender = findViewById(R.id.base_show_gender);
        tvVisitedCount = findViewById(R.id.base_show_recognizer_count);
        tvPhoneNum = findViewById(R.id.base_show_phone);
    }


    //删除成功的自定义Toast
    public void makeText(Context context,String text) {
        Toast customToast = new Toast(context);
        //获得view的布局
        View customView = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
        TextView textView = customView.findViewById(R.id.show_message_toast);
        textView.setText(text);

        //设置textView中的文字
        //设置toast的View,Duration,Gravity最后显示
        customToast.setView(customView);
        customToast.setDuration(Toast.LENGTH_SHORT);
        customToast.setGravity(Gravity.CENTER, 0, 0);
        customToast.show();
    }
}
