package com.yongyida.yydrobotcv.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.yongyida.robot.communicate.app.common.send.SendClient;
import com.yongyida.robot.communicate.app.common.send.SendResponseListener;
import com.yongyida.robot.communicate.app.hardware.motion.response.data.Ultrasonic;
import com.yongyida.robot.communicate.app.hardware.motion.send.data.QueryUltrasonicControl;
import com.yongyida.yydrobotcv.camera.Camera2Track;
import com.yongyida.yydrobotcv.camera.CameraBase;
import com.yongyida.yydrobotcv.camera.PreviewListener;
import com.yongyida.yydrobotcv.motion.HeadHelper;
import com.yongyida.yydrobotcv.tts.CorpusConstants;
import com.yongyida.yydrobotcv.tts.TTSManager;
import com.yongyida.yydrobotcv.useralbum.User;
import com.yongyida.yydrobotcv.useralbum.UserDataSupport;
import com.yongyida.yydrobotcv.utils.DrawUtil;

import java.util.List;

import dou.utils.DLog;
import dou.utils.ToastUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

public class FaceDetectService extends Service implements PreviewListener{

    public static final String START_TYPE = "startType"; // 启动类型 3 种模式 a、主动交互；b、工厂模式演示测试；c、block测试接口
    public static final String START_TYPE_BLOCKLY = "blockly"; // blockly 编程 使用
    public static final String START_TYPE_ACTIVE_INTERACTION = "active_interaction";  // 主动交互使用
    public static final String START_TYPE_ACTIVE_TEST_START = "startTest";  // 主动交互使用
    public static final String START_TYPE_ACTIVE_TEST_STOP = "stopTest";  // 主动交互使用


    public static final String START_CMD = "cmd"; //  大于 0 ，-1为关闭人脸检测
    public static final String START_MSG = "msg"; //
    public static final String START_TAG = "tag"; //

    // 跟随限定参数
    private static final int TRACK_RANGE_HEIGHT = 360;
    private static final int TRACK_RANGE_WIDTH = 320; // 720过大
    //跟随的上下限
    private static final  int TRACK_TOP = (CameraBase.HEIGHT_PREVIEW - TRACK_RANGE_HEIGHT)/2;
    private static final int TRACK_BOTTOM = CameraBase.HEIGHT_PREVIEW - TRACK_TOP;
    // 跟随的左右限制
    private static final int TRACK_RIGHT = (CameraBase.WIDTH_PREVIEW - TRACK_RANGE_WIDTH)/2;
    private static final int TRACK_LEFT = CameraBase.WIDTH_PREVIEW - TRACK_RIGHT;

    private float trackCenterX = 640;
    private float trackCenterY = 360;

    //人脸检测超时设置
    private static final int FACE_CHECK_COUNT = 50;
    private static final int NO_FACE_CHECK_COUNT = 50;
    private static final int LONG_TIME_NO_FACE = 100; // 没有人脸就停止了
    private int faceCheckCount = 0;
    private int noFaceCheckCount = 0;




    private static final String TAG = FaceDetectService.class.getSimpleName();
    CameraBase mCamera2Track;
    YMFaceTrack faceTrack;
    Context mContext;


    boolean isTrackOn = true;
    String startType = START_TYPE_ACTIVE_INTERACTION;
    String sayHello = "";
    boolean sayOnce = true;

    // blockly块
    int checkOutTime = 2000;
    int checkId = -1;

     Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    if (startType.equals(START_TYPE_BLOCKLY)){
                        blockBack("timeOut");
                    }
                    break;
            }
            return true;
        }
    });



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"onCreate");
        mCamera2Track = Camera2Track.getCameraInstance(this);
        mCamera2Track.setListener(this);

        DrawUtil.updateDataSource(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        faceCheckCount = 0;
        noFaceCheckCount = 0;
        trackingMap = new SimpleArrayMap<>();
        mQueryUltraValueControl.setAndroid(QueryUltrasonicControl.Android.SEND);
        SendClient.getInstance(this).send(this, mQueryUltraValueControl, mSendUltraResponseListener);
        startType = intent.getStringExtra(START_TYPE);
        Log.e(TAG,"startCommand "  + startType);
        switch (startType){
            case START_TYPE_ACTIVE_INTERACTION:
                sayHello = intent.getStringExtra(START_MSG);
                sayOnce = true;
                mCamera2Track.start();
                startTrack();
                break;
            case START_TYPE_BLOCKLY:
                checkId = Integer.parseInt(intent.getStringExtra(START_CMD));
                checkOutTime = Integer.parseInt(intent.getStringExtra(START_TAG));
                if (checkOutTime>0){
                    mHandler.sendEmptyMessageDelayed(1,checkOutTime);// 超时关闭
                    mCamera2Track.start();
                    startTrack();
                }else if (checkOutTime == -1){
                    stopTrack();
                    mCamera2Track.stop();
                }
                break;
            case START_TYPE_ACTIVE_TEST_START:
                mCamera2Track.start();
                startTrack();
                break;
            case START_TYPE_ACTIVE_TEST_STOP:
                stopTrack();
                mCamera2Track.stop();
                break;
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        mCamera2Track.stop();
        stopTrack();
        Log.e(TAG,"onDestroy");
        super.onDestroy();
    }
    private final Object lock = new Object();
    protected boolean stop = false;
    @Override
    public void preview(byte[] bytes) {
        if (!stop) {
            synchronized (lock) {
                runTrack(bytes);
            }
        }
    }

    public void stopTrack() {
        if (faceTrack == null) {
            DLog.d("already release track");
            return;
        }
        stop = true;
        faceTrack.onRelease();
        faceTrack = null;
        DLog.d("release track success");
    }

    public void startTrack() {
        if (faceTrack != null) {
            DLog.d("already init track");
            return;
        }

        stop = false;
        mContext = this;
        faceTrack = new YMFaceTrack();
        faceTrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_FAR);
        int result = faceTrack.initTrack(this, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640);
        DLog.d("getAlbumSize1: " + faceTrack.getEnrolledPersonIds().size());
        boolean needUpdateFaceFeature = faceTrack.isNeedUpdateFaceFeature();
        if (needUpdateFaceFeature) {
            DLog.d("update result: " + faceTrack.updateFaceFeature());
        }

        if (result == 0) {
            faceTrack.setRecognitionConfidence(70);
            new ToastUtil(this).showSingletonToast("初始化检测器成功");
        } else {
            new ToastUtil(this).showSingletonToast("初始化检测器失败");
        }

        DLog.d("getAlbumSize2 :" + faceTrack.getEnrolledPersonIds().size());
    }
    private void runTrack(byte[] data) {
            final List<YMFace> faces = analyse(data, 1280, 720);
            if (faces.size()>0){
                if(startType.equals(START_TYPE_BLOCKLY)){
                    User user = DrawUtil.getUserFromPersonId(checkId);
                    if (user!=null){
                        blockBack(user.getUserName());
                    }
                }else {
                    String name = DrawUtil.getNameFromPersonId(faces.get(0).getPersonId());
//                Log.e(TAG,"人脸识别的 id号码 " + faces.get(0).getPersonId() + "可信度 " + faces.get(0).getConfidence() +  "获取到的人名 " + name);
                    if (!TextUtils.isEmpty(name)){

                        if (sayHello.equals("sayHello")&&sayOnce){
                            sayOnce = false;
                           Log.e(TAG,"性别 "+ faces.get(0).getGender() + "track sex"+ faceTrack.getGender(0)) ;
                            if (faceTrack.getGender(0)==1){
                                TTSManager.TTS(CorpusConstants.SayHelloWords(CorpusConstants.VIP_SIR,name),null);
                            }else {
                                TTSManager.TTS(CorpusConstants.SayHelloWords(CorpusConstants.VIP_MADAM,name),null);
                            }
                            // 子线程中处理
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // 添加数据库
                                    Log.e(TAG,"开始更新数据库 ");
                                    UserDataSupport dataSource =  UserDataSupport.getInstance(FaceDetectService.this);
                                    dataSource.updateIdentifyCount( faces.get(0).getPersonId()+"");
                                    Log.e(TAG,"更新数据库完成 ");
                                }
                            }).start();
                        }
                    }else {
                        faceCheckCount ++;
                        if (faceCheckCount == FACE_CHECK_COUNT){
                            if (sayHello.equals("sayHello")&&sayOnce){
                                sayOnce = false;
                                Log.e(TAG,"性别 "+ faces.get(0).getGender() + "track sex"+ faceTrack.getGender(0)) ;
                                if (faceTrack.getGender(0)==1){
                                    TTSManager.TTS(CorpusConstants.SayHelloWords(CorpusConstants.SIR,null),null);
                                }else {
                                    TTSManager.TTS(CorpusConstants.SayHelloWords(CorpusConstants.MADAM,null),null);
                                }
                            }
                        }

                    }
                }
            }else {
                Log.e(TAG,"没有检测到人脸 " + noFaceCheckCount);
                noFaceCheckCount ++;
                if (noFaceCheckCount == NO_FACE_CHECK_COUNT){
                    Log.e(TAG,sayHello + "::"+ sayOnce) ;
                    if (sayHello.equals("sayHello")&&sayOnce){
                        sayOnce = false;
                        TTSManager.TTS(CorpusConstants.SayHelloWords(CorpusConstants.NO_PERSON,null),null);
                    }
                }
                if (noFaceCheckCount == LONG_TIME_NO_FACE){
                    HeadHelper.headCenter(this);
                    TTSManager.TTS("真无聊，都没人跟我说说话，哎！",null);
                    mCamera2Track.stop();
                    stopTrack();
                }
            }
    }


    private SimpleArrayMap<Integer, YMFace> trackingMap;
    private Thread thread;
    boolean threadBusy = false;
    protected List<YMFace> analyse( byte[] bytes, final int iw, final int ih) {

        if (faceTrack == null) return null;
        final List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);
        if (faces != null && faces.size() > 0) {
            if ( !stop) {
                Log.e(TAG,"记录映射表的 长度 " + trackingMap.size());
                if (trackingMap.size() > 50) trackingMap.clear();
                int maxIndex = 0;
                final YMFace ymFace = faces.get(maxIndex);
                final int anaIndex = maxIndex;
                final int trackId = ymFace.getTrackId();
                final float[] rect = ymFace.getRect();
                final float[] headposes = ymFace.getHeadpose();


                Log.e(TAG,"当前距离 " + ultraDistance);
                if (isTrackOn&&ultraDistance>PirPersonDetectService.STOP_DISTANCE){ // 跟随运动逻辑
                    trackCenterX = (rect[0] - rect[2]/2);
                    trackCenterY = (rect[1] + rect[3]/2);
                   if (trackCenterX>TRACK_LEFT){
                       HeadHelper.headRight(this);
                   }else if (trackCenterX<TRACK_RIGHT){
                       HeadHelper.headLeft(this);
                   }
                   if (trackCenterY<TRACK_TOP){
                       HeadHelper.headUp(this);
                   }else if (trackCenterY>TRACK_BOTTOM){
                       HeadHelper.headDown(this);

                   }
                }

                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            threadBusy = true;
                            boolean next = true;
                            if ((Math.abs(headposes[0]) > 30
                                    || Math.abs(headposes[1]) > 30
                                    || Math.abs(headposes[2]) > 30)) {
                                //角度不佳不再识别
                                next = false;
                            }
                            int faceQuality = faceTrack.getFaceQuality(anaIndex);
                            if (faceQuality < 85) {
                                //人脸质量不佳，不再识别
                                next = false;
                            }
                            long time = System.currentTimeMillis();
                            int identifyPerson = -11;
                            if (next) {
                                final int trackId = ymFace.getTrackId();
                                if (!trackingMap.containsKey(trackId) ||
                                        trackingMap.get(trackId).getPersonId() <= 0) {


                                    identifyPerson = faceTrack.identifyPerson(anaIndex);
                                    int confidence = faceTrack.getRecognitionConfidence();

                                    ymFace.setIdentifiedPerson(identifyPerson, confidence);
                                    trackingMap.put(trackId, ymFace);
                                }
                                next = false;
                                //使用本地就不再使用云端,可直接删除云端部分
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            threadBusy = false;
                        }
                    }
                });
                thread.start();
            }

            for (int i = 0; i < faces.size(); i++) {
                final YMFace ymFace = faces.get(i);
                final int trackId = ymFace.getTrackId();
                if (trackingMap.containsKey(trackId)) {
                    YMFace face = trackingMap.get(trackId);
                    ymFace.setIdentifiedPerson(face.getPersonId(), face.getConfidence());
                }
            }
        }
        return faces;
    }

    public void blockBack(String backMsg){

        mCamera2Track.stop();
        stopTrack();
        mHandler.removeMessages(1);// 删除延时
        Log.e(TAG,startType + "检测结束 " + backMsg );
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.yydrobot.service.blockly", "com.yydrobot.service.blockly.BlocklyService");
        intent.setComponent(componentName);
        intent.putExtra("from", getPackageName());
        intent.putExtra("function", "CMD_MSG");
        intent.putExtra("cmd", 6003);
        intent.putExtra("arg1", 2);
        intent.putExtra("msg", backMsg);
        startService(intent);
    }
    private QueryUltrasonicControl mQueryUltraValueControl = new QueryUltrasonicControl();
    int ultraDistance = 0;
    private SendResponseListener mSendUltraResponseListener = new SendResponseListener<Ultrasonic>() {
        @Override
        public void onSuccess(Ultrasonic ultrasonic) {
            if (ultrasonic != null) {
//                ultraDistance = ultrasonic.getDistances()[5];  // 逻辑值
                ultraDistance = 70;
            }
        }

        @Override
        public void onFail(int i, String s) {

        }
    };
}
