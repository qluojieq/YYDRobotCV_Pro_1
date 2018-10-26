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
import com.yongyida.yydrobotcv.camera.CameraBase;
import com.yongyida.yydrobotcv.motion.HeadHelper;
import com.yongyida.yydrobotcv.tts.CorpusConstants;
import com.yongyida.yydrobotcv.tts.TTSManager;
import com.yongyida.yydrobotcv.useralbum.User;
import com.yongyida.yydrobotcv.useralbum.UserDataSupport;
import com.yongyida.yydrobotcv.utils.CommonUtils;
import com.yongyida.yydrobotcv.utils.DrawUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import brandon.helper.camera.Camera1Helper;
import brandon.helper.camera.CameraHelper;
import dou.utils.DLog;
import dou.utils.ToastUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

public class FaceDetectService extends Service implements CameraHelper.PreviewListener {
    private static final String TAG = FaceDetectService.class.getSimpleName();

    public static final String START_TYPE = "startType"; // 启动类型 3 种模式 a、主动交互；b、工厂模式演示测试；c、block测试接口

    public static final String START_TYPE_BLOCKLY = "blockly"; // blockly 编程 使用
    public static final String START_TYPE_ACTIVE_INTERACTION = "active_interaction";  // 主动交互使用
    public static final String START_TYPE_ACTIVE_TEST_START = "startTest";  // 开始工厂模式使用
    public static final String START_TYPE_ACTIVE_TEST_STOP = "stopTest";  // 结束使用


    public static final String START_CMD = "cmd"; //  大于 0 ，-1为关闭人脸检测
    public static final String START_MSG = "msg"; //
    public static final String START_TAG = "tag"; //

    // 跟随限定参数
    public static final int TRACK_RANGE_HEIGHT = 360;
    public static final int TRACK_RANGE_WIDTH = 320; // 720过大
    //跟随的上下限
    public  static final int TRACK_TOP = (CameraBase.HEIGHT_PREVIEW - TRACK_RANGE_HEIGHT) / 2;
    public  static final int TRACK_BOTTOM = CameraBase.HEIGHT_PREVIEW - TRACK_TOP;
    // 跟随的左右限制
    public static final int TRACK_RIGHT = (CameraBase.WIDTH_PREVIEW - TRACK_RANGE_WIDTH) / 2;
    public  static final int TRACK_LEFT = CameraBase.WIDTH_PREVIEW - TRACK_RIGHT;

    public  static float trackCenterX = 640;
    public static float trackCenterY = 360;

    //人脸检测超时设置
    private static final int FACE_CHECK_COUNT = 10;  // 看很多眼也不认识，默认50
    private static final int LONG_TIME_NO_FACE_THRESHOLD = 1000 * 5; // 没有人脸的处理阈值

    private int faceCheckCount = 0;

    private long withFaceTime = 0;
    private long noFaceTime = 0;


    CameraHelper cameraHelper;
    YMFaceTrack faceTrack;
    Context mContext;


    boolean isTrackOn = true;
    String startType = START_TYPE_ACTIVE_INTERACTION;
    boolean sayOnce = true;
    // 是否已经被说过
    ArrayList<Integer> userVisited;
    ArrayList<float[]> userStrangerVisited;


    // blockly块
    int checkOutTime = 2000;
    int checkId = -1;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (startType.equals(START_TYPE_BLOCKLY)) {
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
        Log.e(TAG, "onCreate");
        cameraHelper = Camera1Helper.getInstance();
        cameraHelper.setPreviewListener(this);
        cameraHelper.setSurfaceView(null);
        trackingMap = new SimpleArrayMap<Integer, YMFace>();
        DrawUtil.updateDataSource(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //初始一下计时器
        noFaceTime = new Date().getTime();
        withFaceTime = new Date().getTime();

        faceCheckCount = 0;
        if (userVisited == null) {
            userVisited = new ArrayList<>();
        }
        if (userStrangerVisited == null) {
            userStrangerVisited = new ArrayList<>();
        }
        startType = intent.getStringExtra(START_TYPE);
        Log.e(TAG, "startCommand " + startType);
        switch (startType) {
            case START_TYPE_ACTIVE_INTERACTION:
                sayOnce = true;
                cameraHelper.stop();
                try {
                    cameraHelper.start();
                }catch (Exception e){
                    CommonUtils.serviceToast(this,"相机打开失败 。。。");
                }
                initFaceTrack();
                break;
            case START_TYPE_BLOCKLY:
                checkId = Integer.parseInt(intent.getStringExtra(START_CMD));
                checkOutTime = Integer.parseInt(intent.getStringExtra(START_TAG));
                if (checkOutTime > 0) {
                    mHandler.sendEmptyMessageDelayed(1, checkOutTime);// 超时关闭
                    cameraHelper.start();
                    initFaceTrack();
                } else if (checkOutTime == -1) {
                    stopTrack();
                    cameraHelper.stop();
                }
                break;
            case START_TYPE_ACTIVE_TEST_START:
                cameraHelper.start();
                initFaceTrack();
                break;
            case START_TYPE_ACTIVE_TEST_STOP:
                stopTrack();
                cameraHelper.stop();
                break;
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {

        cameraHelper.stop();
        stopTrack();
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    private final Object lock = new Object();
    protected boolean stop = false;


    long camera_long = 0;
    int camera_count = 0;
    int camera_fps = 0;

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
//        SendClient.getInstance(this).send(this, mQueryUltraValueControl, null);
        DLog.d("release track success");
    }

    public void initFaceTrack() {
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

    int faceId;

    private void runTrack(byte[] data) {
        if (camera_long == 0) camera_long = System.currentTimeMillis();
        camera_count++;
        if (System.currentTimeMillis() - camera_long > 1000) {
            camera_fps = camera_count;
            camera_count = 0;
            camera_long = 0;
        }
        long startTime = new Date().getTime();
        trackAnalyse(data, 1280, 720);  // 跟踪
        long endTime = new Date().getTime();
        Log.e(TAG, " fps + " + camera_fps + "track cost time " + (endTime - startTime));
    }

    public void recognizerLogic(final YMFace ymFace) {
        if (ymFace != null) {  // 识别
            if (startType.equals(START_TYPE_BLOCKLY)) {
                User user = DrawUtil.getUserFromPersonId(checkId);
                if (user != null) {
                    blockBack(user.getUserName());
                }
            } else if (startType.equals(START_TYPE_ACTIVE_INTERACTION)) {

                faceId = ymFace.getPersonId();
                Log.e(TAG, "  faceId " + faceId);
                boolean next = true;
                if (next) {

                    if (faceId > -1) { // 认识的情况
                        if (userVisited.contains(faceId))// 已经包含直接跳出
                            return;
                        String name = DrawUtil.getNameFromPersonId(faceId);
                        if (!TextUtils.isEmpty(name)) {
                            Log.e(TAG, "存储器中获得的性别 " + DrawUtil.getGenderFromPersonId(faceId));
                            if (ymFace.getGender() == 1) {
                                TTSManager.TTS(CorpusConstants.SayHelloWords(CorpusConstants.VIP_SIR, name), null);
                            } else if (ymFace.getGender() == 0) {
                                TTSManager.TTS(CorpusConstants.SayHelloWords(CorpusConstants.VIP_MADAM, name), null);
                            } else {
                                TTSManager.TTS(CorpusConstants.SayHelloWords(CorpusConstants.NO_SEX_VIP, name), null);
                            }
                            userVisited.add(faceId);
                            // 子线程中处理
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // 添加数据库
                                    Log.e(TAG, "开始更新数据库 ");
                                    UserDataSupport dataSource = UserDataSupport.getInstance(FaceDetectService.this);
                                    dataSource.updateIdentifyCount(ymFace.getPersonId() + "");
                                    Log.e(TAG, "更新数据库完成 ");
                                }
                            }).start();

                        } else {
                            Log.e(TAG, "异常  faceId " + faceId + " 没有查到名字 ");
//                        faceCheckCount = FACE_CHECK_COUNT;
                        }
                    } else { // 不认识的情况
                        faceCheckCount++;
                        Log.e(TAG, "不认识的人脸 faceId " + faceId + " faceCheckCount " + faceCheckCount + " 长度 " + userVisited.size() + "是否包含faceId " + userVisited.contains(faceId));
                        if (faceCheckCount >= FACE_CHECK_COUNT && !isStrangerVisited(ymFace.getAus())) {
                            faceCheckCount = 0;
                            userStrangerVisited.add(ymFace.getAus());
                            if (ymFace.getGenderConfidence() > 70) {
                                if (ymFace.getGender() == 1) {
                                    TTSManager.TTS(CorpusConstants.SayHelloWords(CorpusConstants.SIR, null), null);
                                } else {
                                    TTSManager.TTS(CorpusConstants.SayHelloWords(CorpusConstants.MADAM, null), null);
                                }
                            } else {
                                TTSManager.TTS(CorpusConstants.SayHelloWords(CorpusConstants.NO_SEX, null), null);
                            }
                        }
                    }
                }
            }
        }
    }

    private SimpleArrayMap<Integer, YMFace> trackingMap;
    private Thread thread;
    boolean threadBusy = false;

    public static int speedUpLength = 60;

    protected void trackAnalyse(final byte[] bytes, final int iw, final int ih) {

        if (faceTrack == null) return;
        final List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);

        if (faces != null && faces.size() > 0) {
            Log.e(TAG, "检测到人脸 ");
            withFaceTime = new Date().getTime();
            if (!stop) {
                if (trackingMap.size() > 50) trackingMap.clear();
                //只对最大人脸框进行识别
                int maxIndex = 0;
                for (int i = 1; i < faces.size(); i++) {
                    if (faces.get(maxIndex).getRect()[2] <= faces.get(i).getRect()[2]) {
                        maxIndex = i;
                    }
                }
                final YMFace ymFace = faces.get(maxIndex);
                final int anaIndex = maxIndex;
                final float[] rect = ymFace.getRect();
                final float[] headposes = ymFace.getHeadpose();

                //    此逻辑不利于判断，由于测距本身不准且旁边的没有相应的测距传感器
                if (isTrackOn) { // 跟随运动逻辑
                    CommonUtils.serviceToast(this, "trackId " + ymFace.getTrackId());
                    trackCenterX = (rect[0] - rect[2] / 2);
                    trackCenterY = (rect[1] + rect[3] / 2);
                    if (trackCenterX > TRACK_LEFT) {
                        if (trackCenterX > TRACK_LEFT + speedUpLength) {
                            HeadHelper.headRightH(this);
                        } else {
                            HeadHelper.headRightL(this);
                        }
                    } else if (trackCenterX < TRACK_RIGHT) {
                        if (trackCenterX < TRACK_RIGHT - speedUpLength) {
                            HeadHelper.headLeftH(this);
                        } else {
                            HeadHelper.headLeftL(this);
                        }

                    }
                    if (trackCenterY < TRACK_TOP) {
                        HeadHelper.headUp(this);
                    } else if (trackCenterY > TRACK_BOTTOM) {
                        HeadHelper.headDown(this);

                    }
                }

                if (!threadBusy) {
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                threadBusy = true;
                                boolean next = true;
                                if ((Math.abs(headposes[0]) > 30
                                        || Math.abs(headposes[1]) > 30
                                        || Math.abs(headposes[2]) > 30)) {
                                    next = false;
                                }
                                int faceQuality = faceTrack.getFaceQuality(anaIndex);
                                if (faceQuality < 6) {
                                    next = false;
                                }
                                int genderConfidence = faceTrack.getGenderConfidence(anaIndex);
                                if (genderConfidence<80){
                                    next = false;
                                }
                                if (next) {
                                    final int trackId = ymFace.getTrackId();
                                    if (!trackingMap.containsKey(trackId) ||
                                            trackingMap.get(trackId).getPersonId() <= 0) {
                                        int genderConfidenceRate = faceTrack.getGenderConfidence(anaIndex);
                                        Log.e(TAG, " 性别的置信度 " + genderConfidenceRate);
                                        if (genderConfidenceRate > 85) {
                                            ymFace.setPersonId(faceTrack.identifyPerson(anaIndex));
                                            ymFace.setGender(faceTrack.getGender(anaIndex));
                                            ymFace.setGenderConfidence(genderConfidenceRate);
                                            ymFace.setAUs(faceTrack.getFaceFeature(anaIndex));
                                            trackingMap.put(trackId, ymFace);
                                        }

                                    } else {
                                        YMFace face = trackingMap.get(trackId);
                                        ymFace.setPersonId(face.getPersonId());
                                        ymFace.setGenderConfidence(face.getGenderConfidence());
                                        ymFace.setGender(face.getGender());
                                        ymFace.setAUs(face.getAus());
                                    }
                                    recognizerLogic(ymFace); // 识别逻辑
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

            }

        } else { // 没有人脸的情况
            noFaceTime = new Date().getTime();
            Log.e(TAG, "没有检测到人脸 noFaceTime " + (noFaceTime - withFaceTime));
            //长时间没有看到人脸，感觉没有人了
            if ((noFaceTime - withFaceTime) > LONG_TIME_NO_FACE_THRESHOLD && (noFaceTime - withFaceTime) < LONG_TIME_NO_FACE_THRESHOLD * 24) {  // 没有人脸已经很久
                Intent intent = new Intent(this, PirPersonDetectService.class);
                intent.putExtra("startType", "noFace");
                startService(intent);
                HeadHelper.headCenter(this);
                userVisited.clear();
                userStrangerVisited.clear();
                trackingMap.clear();
                TTSManager.TTS("很高兴为你服务，下次再见", null);
                cameraHelper.stop();
                stopTrack();
            }
        }
    }

    public void blockBack(String backMsg) {

//        mCamera2Track.stop();
        cameraHelper.stop();
        stopTrack();
        mHandler.removeMessages(1);// 删除延时
        Log.e(TAG, startType + "检测结束 " + backMsg);
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

//    // 跟随距离
//    private QueryUltrasonicControl mQueryUltraValueControl = new QueryUltrasonicControl();
//    int ultraDistance = PirPersonDetectService.LOW_DISTANCE - 10;
//    private SendResponseListener mSendUltraResponseListener = new SendResponseListener<Ultrasonic>() {
//        @Override
//        public void onSuccess(Ultrasonic ultrasonic) {
//            if (ultrasonic != null) {
//                if (ultrasonic.getDistances()[5] > ultrasonic.getDistances()[6]) {
//                    ultraDistance = ultrasonic.getDistances()[5];  // 逻辑值
//                } else {
//                    ultraDistance = ultrasonic.getDistances()[6];
//                }
//                Log.e(TAG, "当前检测到的人脸检测中的距离 " + ultraDistance);
//            }
//        }
//
//        @Override
//        public void onFail(int i, String s) {
//
//        }
//    };


    // 是否陌生人访问过默认false；
    private boolean isStrangerVisited(float[] strangerFeature) {
        boolean ret = false;
        if (strangerFeature != null) {
            if (userStrangerVisited.size() > 10)
                userStrangerVisited.clear();
            for (int i = 0; i < userStrangerVisited.size(); i++) {
                float result = faceTrack.compareFaceFeature(strangerFeature, userStrangerVisited.get(i));
                Log.e(TAG, i + " 个脸的比较结果 " + result + " 长度 " + userStrangerVisited.size());
                if (result > 65) {
                    ret = true;
                    break;
                }
            }
            Log.e(TAG, " 比较人脸结果 " + ret);
        } else {
            ret = true;
        }
        return ret;
    }
}
