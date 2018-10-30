package com.yongyida.yydrobotcv.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yongyida.robot.brain.system.ITTSCallback;
import com.yongyida.yydrobotcv.R;
import com.yongyida.yydrobotcv.RegisterActivity;
import com.yongyida.yydrobotcv.camera.ImageUtils;
import com.yongyida.yydrobotcv.customview.ErrorDialog;
import com.yongyida.yydrobotcv.customview.SameFaceWarnDialog;
import com.yongyida.yydrobotcv.tts.TTSManager;
import com.yongyida.yydrobotcv.useralbum.User;
import com.yongyida.yydrobotcv.useralbum.UserDataSupport;
import com.yongyida.yydrobotcv.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import dou.helper.CameraHelper;
import dou.helper.CameraParams;
import dou.utils.BitmapUtil;
import dou.utils.DLog;
import dou.utils.DeviceUtil;
import dou.utils.DisplayUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

import static com.yongyida.yydrobotcv.useralbum.UserDataHelper.DATA_PATH;
import static dou.utils.HandleUtil.runOnUiThread;

/**
 * @author Brandon on 2018/4/10
 *  Brandon update  2018/10/25
 **/
public class RegisterCameraFragment extends Fragment implements CameraHelper.PreviewFrameListener {

    private final String TAG = RegisterCameraFragment.class.getSimpleName();

    private static final int BASIC_FRAME_RATE = 5;//可能需要调试确认原20

    private static final int BASIC_FRAME_RATE_PERSON = 10;

    private static boolean checkoutDifferenceFaceOn = false; // 是否开启人脸差异检测


    private static final int NO_PERSON_COUNT_THRESHOLD = BASIC_FRAME_RATE * 60 * 4;//没有人脸的qingk

    private static final int NO_PERSON_DETECT_THRESHOLD = BASIC_FRAME_RATE_PERSON * 8;//
    private static final int SOUND_VOICE_THRESHOLD = BASIC_FRAME_RATE * 4;//
    private static final int NO_PERSON_WARN_THRESHOLD = 4;//


    private long noPersonCount = 0;
    private long voiceOnCount = 0;
    private long voiceWarnCount = 0;

    public SurfaceView preview_surface;

    TextView hintFaceView;
    TextView hintFaceInView;
    protected CameraHelper mCameraHelper;
    protected YMFaceTrack faceTrack;
    Context mContext;

    protected int iw = 1920, ih = 1080;

    private List<Float> timeList = new ArrayList<>();
    protected boolean stop = false;
    //camera_max_width值为-1时, 找大于640分辨率为屏幕宽高等比
    private int camera_max_width = -1;

    int camera_fps;
    int camera_count;
    long camera_long = 0;

    int sw;
    int sh;

    public static Handler mHandler;
    public boolean isVoiceOn = false;

    //对于每种情况预览帧数进程测试,10稍慢
    int TOTAL_STEP = 1;
    //从0开始，0,1,2；共三步
    int currentStep = 0;
    int viewCountStep1 = 0;
    int viewCountStep2 = 0;
    int viewCountStep3 = 0;
    int viewCountStep4 = 0;
    //注册的Id;
    int personId = -1;
    User registerUser;


    // 拍照中的各个步骤的提示语
    final String ttsString1 = "请正对我，将人脸移入框内";
    final String ttsString2 = "很好，请抬头";
    final String ttsString3 = "请录入您的侧脸";
    // 不在框内的提示语
    final String ttsStringNOFrame1 = "没有检测到人脸";
    final String ttsStringNOFrame2 = "请抬头";
    final String ttsStringNOFrame3 = "请录入你的侧脸";
    // 步骤走完
    final String ttsAddSuccess = "人脸录入成功";
    ErrorDialog exitDialog;
    SameFaceWarnDialog faceCheckDialog;
    // 一次检测
    boolean isSameFaceChecked = false;
    boolean isUpdateFace = false;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.enroll_camera_fragment, container, false);
        initCamera(view);
        registerUser = new User();
        exitDialog = new ErrorDialog(RegisterCameraFragment.this.mContext, R.style.custom_dialog, new ErrorDialog.OnCloseListener() {
            @Override
            public void clickConfirm() {
                exitDialog.dismiss();
                RegisterCameraFragment.this.getActivity().finish();
            }
        });


        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        ttsSpeakHint();
                        hintFaceView.setText("正脸");
                        break;
                    case 1:
                        ttsSpeakHint();
                        hintFaceView.setText("抬头");
                        break;
                    case 2:
                        ttsSpeakHint();
                        hintFaceView.setText("侧脸");
                        break;
                    case 3:
                        ttsSpeakHint();
                        hintFaceView.setText("完成，跳转");
                        break;
                    case 4://停止，跳转
                        ((RegisterActivity) RegisterCameraFragment.this.getActivity()).registerBaseInfo(null);
                        ((RegisterActivity) RegisterCameraFragment.this.getActivity()).setRegisterUser(registerUser, 1);
                        break;
                    case 5://test声音
                        ttsSpeakWarn();
                        break;
                    case 6:
                        mCameraHelper.stopPreview();
                        exitDialog.show();
                        if (personId>0){
                            removePersonId(personId+"");
                        }
                        break;

                }
                return true;
            }
        });
        reInit();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        startTrack();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            stopTrack();
        } else {
            startTrack();
        }

    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (camera_long == 0) camera_long = System.currentTimeMillis();
        camera_count++;
        if (System.currentTimeMillis() - camera_long > 1000) {
            camera_fps = camera_count;
            camera_count = 0;
            camera_long = 0;
        }

        Log.e(TAG," 帧率 " + camera_fps);
        if (!stop) {
            runTrack(bytes);
        }
    }

    Thread thread;
    boolean isThreadBusy = false;
    private void runTrack(final byte[] data) {
        try {
            if (!isThreadBusy){
                isThreadBusy = true;
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        analyse(data, iw, ih);
                        isThreadBusy = false;
                    }
                });
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initCamera(View view) {
        mContext = this.getActivity();
        sw = DisplayUtil.getScreenWidthPixels(mContext);
        sh = DisplayUtil.getScreenHeightPixels(mContext);
        preview_surface = view.findViewById(R.id.camera_preview);

        hintFaceView = view.findViewById(R.id.hint_face_side);
        hintFaceInView = view.findViewById(R.id.hint_face_side_up);


        //预设Camera参数，方便扩充
        CameraParams params = new CameraParams();
        //优先使用的camera Id,
        params.firstCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        params.surfaceView = preview_surface;
        params.preview_width = camera_max_width;
        params.preview_width = 1920;
        params.preview_height = 1080;

        params.camera_ori = 0;
        params.camera_ori_front = 0;

        if (DeviceUtil.getModel().equals("Nexus 6")) {
            params.camera_ori_front = 180;

        }

        params.previewFrameListener = this;
        mCameraHelper = new CameraHelper(this.getActivity(), params);
    }

    public synchronized void stopTrack() {

        if (faceTrack == null) {
            DLog.d("already release track");
            return;
        }
        stop = true;
        faceTrack.onRelease();
        faceTrack = null;
        DLog.d("release track success");
    }

    public synchronized void startTrack() {
        if (faceTrack != null) {
            DLog.d("already init track");
            return;
        }

        stop = false;

        faceTrack = new YMFaceTrack();

        //设置人脸检测距离，默认近距离，需要在initTrack之前调用
        faceTrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_NEAR);

//        普通有效期版本初始化
        int result = faceTrack.initTrack(this.getActivity(), YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640,DATA_PATH);

        //设置人脸识别置信度，设置75，不允许修改
        faceTrack.setRecognitionConfidence(75);
        if (result == 0) {
            DLog.d("初始化成功");
        } else {
            DLog.d("初始化失败");
        }
        DLog.d("getAlbumSize: " + faceTrack.getAlbumSize());
    }

    //数据处理
    protected void analyse(byte[] bytes, int iw, int ih) {
        if (faceTrack == null) return ;
        final List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);
        if (isVoiceOn) {//播放音乐中
            voiceOnCount ++;
            Log.e(TAG, "播放音乐中" + voiceOnCount);
            if (voiceOnCount>SOUND_VOICE_THRESHOLD){
                voiceOnCount = 0;
                isVoiceOn = false;
            }
            return ;
        }
        if (faces != null && faces.size() > 0) {
            YMFace ymFace = faces.get(0);
            ymFace.setFaceQuality(faceTrack.getFaceQuality(0));
            Log.e(TAG,"注册时的  画面质量 " + ymFace.getFaceQuality());
            int id = faceTrack.identifyPerson(0);
            if (id>0&&!isSameFaceChecked){
                isSameFaceChecked = true;
                Log.e(TAG,"checked ");
                final User user =  UserDataSupport.getInstance(this.mContext).getUser(id+"");
                if (user.getPhoneNum()!=null){

                    mCameraHelper.stopPreview();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            faceCheckDialog = new SameFaceWarnDialog(RegisterCameraFragment.this.mContext, R.style.custom_dialog, new SameFaceWarnDialog.OnCloseListener() {
                                @Override
                                public void clickConfirm() { // 更新
                                    Log.e(TAG,"更新头像");
                                    faceCheckDialog.dismiss();
                                    isUpdateFace = true;
                                    mCameraHelper.startPreview();
                                    personId = Integer.parseInt(user.getPersonId());
                                }
                                @Override
                                public void clickCancel() {  // 不做任何处理
                                    Log.e(TAG,"新录入");
                                    faceCheckDialog.dismiss();
                                    mCameraHelper.startPreview();
                                }
                            }, "已识别你为"+ user.getUserName()+", " + user.getPhoneNum().substring(0,3) + " * * * " + user.getPhoneNum().substring(7,11));
                            faceCheckDialog.show();
                        }
                    });
                }

            }if(id == -111){
                isSameFaceChecked = true;
            }

            if (isFaceIn(ymFace)) {
                noPersonCount = 0;
                voiceWarnCount = 0;
            } else {
                noPersonDetect();
                return ;
            }
            if (currentStep == 0 && isFrontFace(ymFace)) {
                viewCountStep1++;
                Log.e(TAG, "脸的位置测试： 正脸");
                if (viewCountStep1 == TOTAL_STEP) {
                    if(isUpdateFace){// 是否进行更新
                        addFaceUpdate(bytes, ymFace.getRect());
                    }else {
                        currentStep++;
                        mHandler.sendEmptyMessageDelayed(1, 500);
                        viewCountStep2 = 0;
                        addFace1(bytes, ymFace.getRect());
                    }
                }
            } else if (currentStep == 1 && isRiseFace(ymFace)) {
                viewCountStep2++;
                if (viewCountStep2 == TOTAL_STEP) {
                    viewCountStep3 = 0;
                    currentStep++;
                    mHandler.sendEmptyMessageDelayed(2, 500);
                    int i = faceTrack.updatePerson(personId, 0);
                    Log.e(TAG, "注册侧脸时的返回值 " + i);
                }
                Log.e(TAG, "脸的位置测试： 抬头");
            } else if (currentStep == 2 && isSideFace(ymFace)) {
                viewCountStep3++;
                if (viewCountStep3 == TOTAL_STEP) {
                    currentStep++;
                    viewCountStep4 = 0;
                    mHandler.sendEmptyMessageDelayed(3, 500);
                    int i = faceTrack.updatePerson(personId, 0);
                    Log.e(TAG, "注册抬头时的返回值 " + i);
                }
                Log.e(TAG, "脸的位置测试： 侧脸");
            }

        } else {
            viewCountStep1 = 0;
            viewCountStep2 = 0;
            viewCountStep3 = 0;
//            viewCountStep4 = 0;
            noPersonDetect();
        }
        if (currentStep == 3) {
            viewCountStep4++;
            Log.e(TAG, "注册完成" + viewCountStep4);
            if (viewCountStep4 == TOTAL_STEP) {
                mHandler.sendEmptyMessage(4);
            }
        }
        return ;
    }

    private void noPersonDetect() {
        noPersonCount++;
        if (noPersonCount % NO_PERSON_DETECT_THRESHOLD == 0) {
            voiceWarnCount++;
            if (voiceWarnCount < 5) {
                mHandler.sendEmptyMessage(5);//报告
                Log.e(TAG, "发送警报" + voiceWarnCount);
            }
            if (voiceWarnCount > NO_PERSON_WARN_THRESHOLD) {
                mHandler.sendEmptyMessage(6);//没脸报警并结束
//                CommonUtils.serviceToast(this.getActivity(), "不在框内");
            }
        }
    }

    //判断正脸
    private boolean isFrontFace(YMFace ymFace) {
        boolean ret = false;
        float facePose[] = ymFace.getHeadpose();
        float x = facePose[0];
        float y = facePose[1];
        float z = facePose[2];

        if (Math.abs(x) < 10 && Math.abs(y) < 10 && Math.abs(z) < 10&&ymFace.getFaceQuality()>6){
            ret = true;
            isVoiceOn = true;
        }

        return ret;
    }

    /**
     * @return 0
     * -1 已经注册过了
     * -2 两次人脸不一致
     */

    //判断人脸是不已经注册
    private int isFaceRegistered() {
        int ret = 0;
        if (checkoutDifferenceFaceOn) {
           int id =  faceTrack.identifyPerson(0);
           if (currentStep == 0){
               if (id>0){
                   ret = -1;
               }
           }else {

               if (id>0&&currentRegisterId == personId){

               }else {
                   ret = -2;
               }
           }
            Log.e(TAG,"id "+id + "currentId " + currentRegisterId + " personId " + personId );
        }
        return ret;
    }

    int currentRegisterId = -1;

    //判断侧脸
    private boolean isSideFace(YMFace ymFace) {
        boolean ret = false;
        float facePose[] = ymFace.getHeadpose();
        float z = facePose[2];
        if (Math.abs(z) > 15&&ymFace.getFaceQuality()> 5){
            ret = true;
            isVoiceOn = true;
        }

        return ret;
    }

    //抬头脸
    private boolean isRiseFace(YMFace ymFace) {
        boolean ret = false;
        float facePose[] = ymFace.getHeadpose();
        float y = facePose[1];
        if (y < -10&&ymFace.getFaceQuality()> 6)
        {
            ret = true;
            isVoiceOn = true;
        }
        return ret;
    }

    //添加人脸

    void addFace1(byte[] bytes, float[] rect) {

        personId = faceTrack.addPerson(0);//添加人脸
        registerUser.setAge(faceTrack.getAge(0) + "");
        registerUser.setGender(faceTrack.getGender(0) + "");
        currentRegisterId = personId;
        Log.e(TAG, "起始 年龄" + faceTrack.getAge(0) + "性别 " + faceTrack.getGender(0) + "personId " + personId);

        if (personId > 0) {
            registerUser.setPersonId(personId + "");
            Bitmap image = BitmapUtil.getBitmapFromYuvByte(bytes, iw, ih);
            Bitmap head = Bitmap.createBitmap(image, (int) rect[0], (int) rect[1],
                    (int) rect[2], (int) rect[3], null, true);
            ImageUtils.saveBitmap(DATA_PATH + personId + ".jpg", head);

        } else {
            DLog.d("添加人脸失败！" + personId );
            CommonUtils.serviceToast(mContext,"添加人脸失败！请重新添加" + personId);
            Bitmap image = BitmapUtil.getBitmapFromYuvByte(bytes, iw, ih);
            Bitmap head = Bitmap.createBitmap(image, (int) rect[0], (int) rect[1],
                    (int) rect[2], (int) rect[3], null, true);
            ImageUtils.saveBitmap(DATA_PATH + personId + "test.jpg", head);
        }

    }

    //更新人脸
    void addFaceUpdate(byte[] bytes, float[] rect) {


        int id = faceTrack.updatePerson(personId,0);//更新人脸
        registerUser.setAge(faceTrack.getAge(0) + "");
        registerUser.setGender(faceTrack.getGender(0) + "");
        currentRegisterId = personId;
        Log.e(TAG, "起始 年龄" + faceTrack.getAge(0) + "性别 " + faceTrack.getGender(0));

        if (id >=0) {
            registerUser.setPersonId(personId + "");
            Bitmap image = BitmapUtil.getBitmapFromYuvByte(bytes, iw, ih);
            Bitmap head = Bitmap.createBitmap(image, (int) rect[0], (int) rect[1],
                    (int) rect[2], (int) rect[3], null, true);
            ImageUtils.saveBitmap(DATA_PATH  + personId + ".jpg", head);
//            Toast.makeText(mContext, personId + " 更新人脸信息  " + id, Toast.LENGTH_SHORT).show();
            TTSManager.TTS("人脸更新成功",null);
            RegisterCameraFragment.this.getActivity().setResult(RegisterActivity.ADD_SUCCESS_RESULT_CODE);
            RegisterCameraFragment.this.getActivity().finish();
        } else {
            DLog.d("更新人脸失败！");
            mCameraHelper.stopPreview();
            exitDialog.show();
        }

    }

    public void removePersonId(String personId) {
        if (faceTrack == null) {
            startTrack();
        }
        faceTrack.deletePerson(Integer.parseInt(personId));
        Log.e(TAG, "移除已经注册，成功");
        stopTrack();
    }

    @Override
    public void onDestroy() {//释放
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    public void reInit() {
        mHandler.sendEmptyMessageDelayed(0, 500);
        currentStep = 0;
        viewCountStep1 = 0;
        viewCountStep2 = 0;
        viewCountStep3 = 0;
        viewCountStep4 = 0;
    }

    public boolean isFaceIn(YMFace ymFace) {
        float[] faceRect = ymFace.getRect();
        float x = faceRect[0];
        float y = faceRect[1];
        float w = faceRect[2];
        float h = faceRect[3];
        float centerX = x + w / 2;
        float centerY = y + h / 2;

        boolean ret = false;
        if (600 < centerX && centerX < 1100 && centerY > 300 && centerY < 800) {
            ret = true;
        }
        return ret;
    }


    ITTSCallback ttsCallback = new ITTSCallback.Stub() {
        @Override
        public void OnBegin() throws RemoteException {
            Log.e(TAG, "tts begin");
            isVoiceOn = true;
        }

        @Override
        public void OnPause() throws RemoteException {
            Log.e(TAG, "tts pause");
            isVoiceOn = false;
        }

        @Override
        public void OnResume() throws RemoteException {
            Log.e(TAG, "tts begin");
            isVoiceOn = true;
        }

        @Override
        public void OnComplete(String error, String tag) throws RemoteException {
            Log.e(TAG, "tts complete");
            isVoiceOn = false;
        }
    };

    public void ttsSpeakHint() {
        String stringSpeak = "";
        switch (currentStep) {
            case 0:
                stringSpeak = ttsString1;
                break;
            case 1:
                stringSpeak = ttsString2;
                break;
            case 2:
                stringSpeak = ttsString3;
                break;
            case 3:
                stringSpeak = ttsAddSuccess;
        }
        TTSManager.TTS(stringSpeak, ttsCallback);
    }

    public void ttsSpeakWarn() {
        String stringSpeak = "";
        switch (currentStep) {
            case 0:
                stringSpeak = ttsStringNOFrame1;
                break;
            case 1:
                stringSpeak = ttsStringNOFrame2;
                break;
            case 2:
                stringSpeak = ttsStringNOFrame3;
                break;
        }
        TTSManager.TTS(stringSpeak, ttsCallback);
    }
    @Override
    public void onPause() {
        super.onPause();
        mCameraHelper.stopCamera();
    }
}
