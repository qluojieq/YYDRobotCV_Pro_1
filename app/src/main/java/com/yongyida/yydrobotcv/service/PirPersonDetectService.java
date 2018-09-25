package com.yongyida.yydrobotcv.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yongyida.robot.communicate.app.common.response.BaseResponseControl;
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

    public static final int STOP_DISTANCE = 50;
    private static final int LOW_DISTANCE = 80;
    private static final int FAR_DISTANCE = 200;
    private final static String TAG = PirPersonDetectService.class.getSimpleName();
    private QueryPirValueControl mQueryPirValueControl = new QueryPirValueControl();
    private QueryUltrasonicControl mQueryUltraValueControl = new QueryUltrasonicControl();

    int ultraDistance = 70;
    private SendResponseListener mSendPirResponseListener = new SendResponseListener<PirValue>() {

        @Override
        public void onSuccess(PirValue pirValue) {
            Log.e(TAG, "pir success " + pirValue.isHasPeople());
            if (pirValue != null) {
                if (ultraDistance < FAR_DISTANCE) {
                    if (ultraDistance < LOW_DISTANCE) {
                        TTSManager.TTS("你好"+" 让我好好看看", null);
                        Intent intent = new Intent(PirPersonDetectService.this,FaceDetectService.class);
                        intent.putExtra("startType", "active_interaction");
                        intent.putExtra("msg","sayHello");
                        startService(intent);
                        // 问候
                    } else {
                        TTSManager.TTS("你好!", null);
                        Intent intent = new Intent(PirPersonDetectService.this,FaceDetectService.class);
                        intent.putExtra("startType", "active_interaction");
                        intent.putExtra("msg","notSayHello");
                        startService(intent);
                        // 启动追踪
                    }

                } else {
//                    TTSManager.TTS("人已经离开", null);
                }
            }

        }

        @Override
        public void onFail(int i, String s) {

        }
    };
    private SendResponseListener mSendUltraResponseListener = new SendResponseListener<Ultrasonic>() {
        @Override
        public void onSuccess(Ultrasonic ultrasonic) {
            if (ultrasonic != null) {
                ultraDistance = ultrasonic.getDistances()[5];
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

        SendClient.getInstance(this).send(this, mQueryPirValueControl, mSendPirResponseListener);
        mQueryUltraValueControl.setAndroid(QueryUltrasonicControl.Android.SEND);
        SendClient.getInstance(this).send(this, mQueryUltraValueControl, mSendUltraResponseListener);
        Log.e(TAG, " pir人体检测开启成功 ");
        TTSManager.TTS("红外人体检测开启成功！",null);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        // 注销pir
        SendClient.getInstance(this).send(null, mQueryPirValueControl, null);
        // 注销激光
        mQueryUltraValueControl.setAndroid(QueryUltrasonicControl.Android.NO_SEND);
        mQueryUltraValueControl.setSlam(QueryUltrasonicControl.Slam.SEND);
        SendClient.getInstance(this).send(this, mQueryUltraValueControl, null);
        Log.e(TAG,"关闭人体检测相关功能 ！");

        Intent intent = new Intent(this, FaceDetectService.class);
        intent.putExtra("startType","stopTest");
//        intent.putExtra("cmd","1"); //blockly使用用户id
//        intent.putExtra("tag","-1");//-1直接调用停止
//        startService(intent);
        stopService(intent);
        TTSManager.TTS("人体检测关闭",null);
        super.onDestroy();
    }


}
