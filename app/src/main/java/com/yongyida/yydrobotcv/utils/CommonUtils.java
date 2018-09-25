package com.yongyida.yydrobotcv.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.yongyida.yydrobotcv.useralbum.UserDataSupport;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Brandon on 2018/4/18
 **/
public class CommonUtils {


    private static String regPhonNum = "^((13[0-9])|(14[4-9])|(15([0-3]|[5-9]))|(166)|(17[0-8])|(18[0-9])|(19[89]))\\d{8}$";//电话号码

//    private String regNameRule = "^[(a-zA-Z0-9\\u4e00-\\u9fa5)]{1,6}$";//取名规则（汉字、数字、字母）
//    private static String regPhonNum = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";

    private static String regName1 = "^[(a-zA-Z0-9\\u4e00-\\u9fa5)]{1,12}$";//排除特殊字符
    private static String regName2 = "^[(0-9\\u4e00-\\u9fa5)]{1,12}$";//数字和汉字
    private static String regName3 = "^[(a-zA-Z0-9]{1,12}$";//数字和字母


    public static boolean isMatchPhone(String data) {
        Pattern p = Pattern.compile(regPhonNum);
        Matcher m = p.matcher(data);
        boolean isMatch = m.matches();
        return isMatch;
    }

    public static boolean isMatchName(String data) {
        Pattern p = Pattern.compile(regName1);
        Matcher m = p.matcher(data);
        boolean isMatch = m.matches();
        return isMatch;
    }

    public static boolean isMatchName1(String data) { // 仅汉字
        Pattern p = Pattern.compile(regName2);
        Matcher m = p.matcher(data);
        boolean isMatch = m.matches();
        return isMatch;
    }

    public static boolean isMatchName2(String data) { // 仅英文
        Pattern p = Pattern.compile(regName3);
        Matcher m = p.matcher(data);
        boolean isMatch = m.matches();
        return isMatch;
    }


    public static int checkoutName(String name,Context context) {
        int ret = 0;// 0 无异常
        int strLength = 0;
        try {
            strLength = name.getBytes("gbk").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(name)){ //字符为空
            ret = -1;
            return  ret;
        }
        if (strLength > 12) {  // 超出长度限制
            ret = -12;
            return ret;
        }
        if (!isMatchName(name)) {  // 检测特殊字符
            ret = -11;
            return ret;
        }

        if (!(isMatchName1(name)||isMatchName2(name))) {  // 汉字、英文混用
            ret = -10;
            return  ret;
        }
        return ret; // 正常
    }

    private static Toast toast;
    public static Handler handler = new Handler(Looper.getMainLooper());

    public static void serviceToast(final Context mContext, final String str) {
        handler.post(new Runnable() {
            public void run() {
                if (toast == null) {
                    toast = Toast.makeText(mContext, str, Toast.LENGTH_LONG);
                    //设置Toast显示位置，居中，向 X、Y轴偏移量均为0
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    //设置显示时长
                    toast.setText(str);
                    //显示
                } else {
                    toast.setText(str);
                }
                toast.show();

            }
        });
    }
}
