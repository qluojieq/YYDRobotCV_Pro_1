package com.yongyida.yydrobotcv.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yongyida.robot.communicate.app.common.send.SendClient;
import com.yongyida.robot.communicate.app.common.send.SendResponseListener;
import com.yongyida.robot.communicate.app.hardware.motion.response.data.Ultrasonic;
import com.yongyida.robot.communicate.app.hardware.motion.send.data.QueryUltrasonicControl;
import com.yongyida.robot.communicate.app.hardware.pir.response.data.PirValue;
import com.yongyida.robot.communicate.app.hardware.pir.send.data.QueryPirValueControl;
import com.yongyida.yydrobotcv.tts.TTSManager;

/**
 * @author Brandon on 2018/8/23
 **/
public class PirPersonDetectService extends Service {


    private final static String TAG = PirPersonDetectService.class.getSimpleName();

    public static final int STOP_DISTANCE = 50;
    public static final int LOW_DISTANCE = 150;
    public static final int FAR_DISTANCE = 200;

    private QueryPirValueControl mQueryPirValueControl = new QueryPirValueControl();
    private QueryUltrasonicControl mQueryUltraValueControl = new QueryUltrasonicControl();
    static int ultraDistance = 70;

    boolean isPersonOn = false;

    private SendResponseListener mSendPirResponseListener = new SendResponseListener<PirValue>() {
        @Override
        public void onSuccess(PirValue pirValue) {   // Pir 只检测来人，没人由人脸检测返回
            Log.e(TAG, "pir success " + pirValue.isHasPeople());
            if (pirValue != null && !isPersonOn) {
                isPersonOn = true;

//                    if (isInDistance()) {
                TTSManager.TTS("你好，我是奥丁！", null);
                Intent intent = new Intent(PirPersonDetectService.this, FaceDetectService.class);
                intent.putExtra("startType", "active_interaction");
                intent.putExtra("msg", "sayHello");
                startService(intent);
                // 问候
//                    }
            }

        }

        @Override
        public void onFail(int i, String s) {

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent!=null){
            String startType = intent.getStringExtra("startType");
            if (startType != null && startType.equals("noFace")) {
                isPersonOn = false;
            }else {
                isPersonOn = false;
                SendClient.getInstance(this).send(this, mQueryPirValueControl, mSendPirResponseListener);
                mQueryUltraValueControl.setAndroid(QueryUltrasonicControl.Android.SEND);
                Log.e(TAG, " pir人体检测开启成功 ");
                TTSManager.TTS("红外人体检测开启成功！", null);
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        // 注销pir
        SendClient.getInstance(this).send(null, mQueryPirValueControl, null);
        // 注销激光
        mQueryUltraValueControl.setAndroid(QueryUltrasonicControl.Android.NO_SEND);
        mQueryUltraValueControl.setSlam(QueryUltrasonicControl.Slam.SEND);
        SendClient.getInstance(this).send(this, mQueryUltraValueControl, null);
        Log.e(TAG, "关闭人体检测相关功能 ！");

        Intent intent = new Intent(this, FaceDetectService.class);
        intent.putExtra("startType", "stopTest");
//        intent.putExtra("cmd","1"); //blockly使用用户id
//        intent.putExtra("tag","-1");//-1直接调用停止
//        startService(intent);
        stopService(intent);
        TTSManager.TTS("人体检测关闭", null);
        super.onDestroy();
    }


}
