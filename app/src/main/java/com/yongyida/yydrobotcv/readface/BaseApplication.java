package com.yongyida.yydrobotcv.readface;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

//import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.LeakCanary;
import com.yongyida.yydrobotcv.tts.TTSManager;

import mobile.ReadFace.YMUtil;


/**
 * Created by mac on 16/8/15.
 */
public class BaseApplication extends Application {
    private static final String TAG = BaseApplication.class.getSimpleName();

    private static BaseApplication instence;

    //绘制左右翻转
    public static final boolean yu = true;

    public static boolean reverse_180 = false;

    //是否显示logo
    public static boolean useLogo = true;
    public static int screenOri = Configuration.ORIENTATION_LANDSCAPE;
//    public static int  screenOri = Configuration.ORIENTATION_PORTRAIT;

    @Override
    public void onCreate() {
        super.onCreate();
        instence = this;
//        DLog.mSwitch = false;
//        DLog.mWrite = true;
//        YMUtil.setDebug(true);
//        YMUtil.
        TTSManager.bindService(this);
        Log.e(TAG,"application onCreate");
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    public static BaseApplication getAppContext() {
        return instence;
    }

}
