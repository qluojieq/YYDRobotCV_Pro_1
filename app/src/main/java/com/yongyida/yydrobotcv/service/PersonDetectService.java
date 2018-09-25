package com.yongyida.yydrobotcv.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.orbbec.astrakernel.AstraContext;
import com.orbbec.astrakernel.PermissionCallbacks;
import com.orbbec.astrastartlibs.DepthData;
import com.orbbec.astrastartlibs.UserTracker;
import com.yongyida.yydrobotcv.tts.TTSManager;
import com.yongyida.yydrobotcv.utils.CommonUtils;

import org.openni.IObservable;
import org.openni.IObserver;
import org.openni.Point3D;
import org.openni.UserEventArgs;

public class PersonDetectService extends Service {
    private static final String TAG = PersonDetectService.class.getSimpleName();

    private static final int MAX_DISTANCE = 2500;// 一米五
    private static final int MIN_DISTANCE = 1000;// 80厘米

    private String helloWords = "  我叫小勇，很高兴为您服务！"; // 小于最小值主动招呼语

    private boolean wordOnce = true;

    private String startType = "start";// 'start ' 开始 ； ' stop ' 结束
    private boolean isCameraStarted = false;

   @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG,"onBind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.e(TAG,"onCreate");
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG,"onStartCommand");
        mExit = false;
        mContext = new AstraContext(this,permissionCallbacks);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"onDestroy");
        mContext.close();
        super.onDestroy();
    }
    AstraContext mContext;
    DepthData mDepthData;
    UserTracker mUserTracker;
    private Thread m_analyzeThread;
    boolean mExit = false;
    boolean mInitOk = false;


    PermissionCallbacks permissionCallbacks = new PermissionCallbacks() {
        @Override
        public void onDevicePermissionGranted() {
            Log.e(TAG,"permission granted start Camera");
            mInitOk = true;
            mDepthData = new DepthData(mContext);
            mDepthData.setMapOutputMode(320,240,30);
            mUserTracker = new UserTracker(mContext);
            mUserTracker.addUserDetectObserver(new NewUserObserver());
            if (!isCameraStarted){
                mContext.start();
                isCameraStarted  = true;
            }
//            mContext.waitAnyUpdateAll();
            if (m_analyzeThread==null){
                m_analyzeThread = new Thread(new AnalyzeRunable());
                m_analyzeThread.start();
            }
        }

        @Override
        public void onDevicePermissionDenied() {
            Log.e(TAG,"permission denied");

        }
    };
    class NewUserObserver implements IObserver<UserEventArgs> {

        @Override
        public void update(IObservable<UserEventArgs> iObservable, UserEventArgs userEventArgs) {
            Log.e(TAG,"newUserObserver update"+ userEventArgs.getId());
        }
    }

    boolean isPerson = true;
    long [] oneTime = {0,0,0,0,0,0,0,0,0,0};//  一次到来的人数
    long [] oneTimeGone = {0,0,0,0,0,0,0,0,0,0}; // 离开的人数
    class AnalyzeRunable implements Runnable{
        @Override
        public void run() {
            while (!mExit){
                mContext.waitAnyUpdateAll();
                int [] data  = mUserTracker.getUsers();
                Log.e(TAG,"当前人数 ： " + data.length);
                if (data.length<=0){
                    Log.e(TAG," 没有人 ");
                    wordOnce = true;
                    continue;
                }

                for (int i=0;i<data.length;i++){
                    Point3D head = mDepthData
                            .convertRealWorldToProjective(mUserTracker
                                    .getCoM(data[i]));
                    if (head.getZ()<MAX_DISTANCE){ //小于150 人脸跟踪
                        isPerson = true;
                        oneTime[i]++;
                        oneTimeGone[i] = 0;
                        if (isPerson&&oneTime[i]==1){
//                            startFaceDetect("startTest");
                            Log.e(TAG,"人来 " + i);
                            CommonUtils.serviceToast(PersonDetectService.this,"有人");
                        }
                        if (head.getZ()<MIN_DISTANCE){
                            if (wordOnce){
                                wordOnce = false;
                                TTSManager.TTS(helloWords, null);//  有人靠近
                            }
                        }
                    }else {
                        isPerson = false;
                        oneTime[i] = 0;
                        oneTimeGone[i]++;
                        if (oneTimeGone[i]==1){
//                         startFaceDetect("stopTest");
                            TTSManager.TTSSTop(PersonDetectService.this);
                            CommonUtils.serviceToast(PersonDetectService.this,"离开");
                            Log.e(TAG,"人离开" + i);

                        }
                    }

                }
            }
        }
    }

    //获取当前人数
    public int getCurrentPersonCount(){
        int ret = 0;
        for (int i = 0;i<oneTime.length;i++){
            if (oneTime[i]>0){
                ret++;
            }
        }
        return  ret;
    }

    //启动人脸检测服务
    public void startFaceDetect(String type){

        Log.e(TAG,"启动人脸检测  " + getCurrentPersonCount());
        if (getCurrentPersonCount()==1){
            Intent intent = new Intent(this,FaceDetectService.class);
            intent.putExtra("startType",type);
            startService(intent);
            // 启动人脸检测
            Log.e(TAG,"结束人体检测，开始人脸检测");
//            mExit = true;
//            mContext.close();
//            stopSelf();
//            Intent intent = new Intent(this, FaceTrackActivity.class);
//            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
        }
    }
}
