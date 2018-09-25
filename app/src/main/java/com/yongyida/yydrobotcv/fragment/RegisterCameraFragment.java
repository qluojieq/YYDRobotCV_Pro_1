package com.yongyida.yydrobotcv.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
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
import android.widget.Toast;

import com.yongyida.robot.brain.system.ITTSCallback;
import com.yongyida.yydrobotcv.R;
import com.yongyida.yydrobotcv.RegisterActivity;
import com.yongyida.yydrobotcv.camera.ImageUtils;
import com.yongyida.yydrobotcv.customview.ErrorDialog;
import com.yongyida.yydrobotcv.customview.ExitDialog;
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

import static dou.utils.HandleUtil.runOnUiThread;

/**
 * @author Brandon on 2018/4/10
 **/
public class RegisterCameraFragment extends Fragment implements CameraHelper.PreviewFrameListener {

    private final String TAG = RegisterCameraFragment.class.getSimpleName();

    private static final int BASIC_FRAME_RATE = 20;//可能需要调试确认

    private static final int BASIC_FRAME_RATE_PERSON = 10;

    private static boolean checkoutDifferenceFaceOn = false; // 是否开启人脸差异检测


    private static final int NO_PERSON_COUNT_THRESHOLD = BASIC_FRAME_RATE * 60 * 4;//没有人脸的qingk

    private static final int NO_PERSON_DETECT_THRESHOLD = BASIC_FRAME_RATE_PERSON * 8;//
    private static final int SOUND_VOICE_THRESHOLD = BASIC_FRAME_RATE * 4;//
    private static final int NO_PERSON_WARN_THRESHOLD = 4;//


    private long noPersonCount = 0;
    private long voiceOnCount = 0;
    private long voiceWarnCount = 0;

    private final boolean isDrawFrame = false;

    public SurfaceView preview_surface;
    public SurfaceView draw_surface;
    TextView hintFaceView;
    TextView hintFaceInView;
    protected CameraHelper mCameraHelper;
    protected YMFaceTrack faceTrack;
    Context mContext;

    protected int iw = 0, ih;
    private float scale_bit;
    private boolean showFps = false;
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
    int TOTAL_STEP = 5;
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
        exitDialog = new ErrorDialog(getContext(), R.style.custom_dialog, new ErrorDialog.OnCloseListener() {
            @Override
            public void clickConfirm() {
                exitDialog.dismiss();
                RegisterCameraFragment.this.getActivity().finish();
            }
        });
        faceFrame = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_face_frame);
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


    ///使用soundPool发声
    void playSound1(int type) {//咔嚓
        ((RegisterActivity) getActivity()).playSound(type);
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
        initCameraMsg();
        if (!stop) {
            runTrack(bytes);
        }
    }

    private void runTrack(byte[] data) {
        try {

            long time = System.currentTimeMillis();
            final List<YMFace> faces = analyse(data, iw, ih);

            String str = "";
            StringBuilder fps = new StringBuilder();
            if (showFps) {
                fps.append("fps = ");
                long now = System.currentTimeMillis();
                float than = now - time;
                timeList.add(than);
                if (timeList.size() >= 20) {
                    float sum = 0;
                    for (int i = 0; i < timeList.size(); i++) {
                        sum += timeList.get(i);
                    }
                    fps.append(String.valueOf((int) (1000f * timeList.size() / sum)))
                            .append(" camera ")
                            .append(camera_fps);
                    timeList.remove(0);
                }
            }
//            final String fps1 = fps.toString() + str;
            final String fps1 = "";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawAnim(faces, draw_surface, scale_bit, getCameraId(), fps1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initCamera(View view) {
        mContext = this.getActivity();
        sw = DisplayUtil.getScreenWidthPixels(mContext);
        sh = DisplayUtil.getScreenHeightPixels(mContext);
        preview_surface = view.findViewById(R.id.camera_preview);
        draw_surface = view.findViewById(R.id.draw_view);
        hintFaceView = view.findViewById(R.id.hint_face_side);
        hintFaceInView = view.findViewById(R.id.hint_face_side_up);
        draw_surface.setZOrderOnTop(true);
        draw_surface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
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
        clearDrawSurface();
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

        iw = 0;//重新调用initCameraMsg的开关
        faceTrack = new YMFaceTrack();

        //设置人脸检测距离，默认近距离，需要在initTrack之前调用
        faceTrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_NEAR);

//        普通有效期版本初始化
        int result = faceTrack.initTrack(this.getActivity(), YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640);

        //设置人脸识别置信度，设置75，不允许修改

        if (result == 0) {
            DLog.d("初始化成功");
//            new ToastUtil(mContext).showSingletonToast("初始化检测器成功");

        } else {
            DLog.d("初始化失败");
//            new ToastUtil(mContext).showSingletonToast("初始化检测器失败");
        }
        DLog.d("getAlbumSize: " + faceTrack.getAlbumSize());
    }

    private void initCameraMsg() {
        if (iw == 0) {

            int surface_w = preview_surface.getLayoutParams().width;
            int surface_h = preview_surface.getLayoutParams().height;


            iw = mCameraHelper.getPreviewSize().width;
            ih = mCameraHelper.getPreviewSize().height;


            int orientation = 0;
            ////注意横屏竖屏问题
            DLog.d(getResources().getConfiguration().orientation + " : " + Configuration.ORIENTATION_PORTRAIT);
            if (sw < sh) {
                scale_bit = surface_w / (float) ih;
                if (mCameraHelper.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    orientation = YMFaceTrack.FACE_270;
                } else {
                    orientation = YMFaceTrack.FACE_90;
                }
            } else {
                scale_bit = surface_h / (float) ih;
                orientation = YMFaceTrack.FACE_0;
            }
            if (faceTrack == null) {
                iw = 0;
                return;
            }

            faceTrack.setOrientation(orientation);
            Log.e(TAG, "orientation " + orientation);
            ViewGroup.LayoutParams params = draw_surface.getLayoutParams();
            params.width = surface_w;
            params.height = surface_h;
            draw_surface.requestLayout();
            Log.e(TAG, "跟踪时使用的 iw " + iw + " ih" + ih + "surface_w" + surface_w + "surface_h" + surface_h);
        }
    }

    public int getCameraId() {
        return mCameraHelper.getCameraId();
    }

    //动画处理
    protected void drawAnim(List<YMFace> faces, SurfaceView outputView, float scale_bit, int cameraId, String fps) {
        if (isDrawFrame) {
            Log.e(TAG, "drawAnim");

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            Canvas canvas = outputView.getHolder().lockCanvas();

            if (canvas == null) return;
            try {

                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                int viewW = outputView.getLayoutParams().width;
                int viewH = outputView.getLayoutParams().height;
                if (faces == null || faces.size() == 0) return;

                for (int i = 0; i < faces.size(); i++) {
                    int size = DisplayUtil.dip2px(mContext, 2);
                    paint.setStrokeWidth(size);
                    paint.setStyle(Paint.Style.STROKE);
                    YMFace ymFace = faces.get(i);
                    float[] rect = ymFace.getRect();
                    float[] pose = ymFace.getHeadpose();
                    float x1 = rect[0] * scale_bit;
                    float y1 = rect[1] * scale_bit;
                    float rect_width = rect[2] * scale_bit;

                    //draw rect
                    RectF rectf = new RectF(x1, y1, x1 + rect_width, y1 + rect_width);
//                canvas.drawRect(rectf, paint);
                    surfaceDraw(rectf, canvas);

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                outputView.getHolder().unlockCanvasAndPost(canvas);
            }
        }

    }

    //数据处理
    protected List<YMFace> analyse(byte[] bytes, int iw, int ih) {
        if (faceTrack == null) return null;
        final List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);
        if (isVoiceOn) {//播放音乐中
            voiceOnCount++;
            if (voiceOnCount > SOUND_VOICE_THRESHOLD) {
                isVoiceOn = false;
                voiceOnCount = 0;
            }
            Log.e(TAG, "播放音乐中" + voiceOnCount);
            return faces;
        }
        if (faces != null && faces.size() > 0) {
            YMFace ymFace = faces.get(0);

            int id = faceTrack.identifyPerson(0);
            if (id>0&&!isSameFaceChecked){
                isSameFaceChecked = true;
                Log.e(TAG,"checked ");
                final User user =  UserDataSupport.getInstance(this.getContext()).getUser(id+"");
                if (user.getPhoneNum()!=null){

                    mCameraHelper.stopPreview();
                    faceCheckDialog = new SameFaceWarnDialog(this.getContext(), R.style.custom_dialog, new SameFaceWarnDialog.OnCloseListener() {
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

            }if(id == -111){
                isSameFaceChecked = true;
            }



            if (isFaceIn(ymFace)) {
                noPersonCount = 0;
                voiceWarnCount = 0;
            } else {
                noPersonDetect();
                return faces;
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
        return faces;
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
        if (Math.abs(x) < 10 && Math.abs(y) < 10 && Math.abs(z) < 10)
            ret = true;
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
        if (Math.abs(z) > 15)
            ret = true;
        return ret;
    }

    //抬头脸
    private boolean isRiseFace(YMFace ymFace) {
        boolean ret = false;
        float facePose[] = ymFace.getHeadpose();
        float y = facePose[1];
        if (y < -15) ret = true;
        return ret;
    }

    //添加人脸

    void addFace1(byte[] bytes, float[] rect) {


        personId = faceTrack.addPerson(0);//添加人脸
        registerUser.setAge(faceTrack.getAge(0) + "");
        registerUser.setGender(faceTrack.getGender(0) + "");
        currentRegisterId = personId;
        Log.e(TAG, "起始 年龄" + faceTrack.getAge(0) + "性别 " + faceTrack.getGender(0));


        if (personId > 0) {
            registerUser.setPersonId(personId + "");
            Bitmap image = BitmapUtil.getBitmapFromYuvByte(bytes, iw, ih);
            Bitmap head = Bitmap.createBitmap(image, (int) rect[0], (int) rect[1],
                    (int) rect[2], (int) rect[3], null, true);
            ImageUtils.saveBitmap(mContext.getCacheDir() + "/" + personId + ".jpg", head);

        } else {
            DLog.d("添加人脸失败！");
            Toast.makeText(mContext, "添加人脸失败！请重新添加", Toast.LENGTH_SHORT).show();
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
            ImageUtils.saveBitmap(mContext.getCacheDir() + "/" + personId + ".jpg", head);
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
        faceFrame.recycle();
        super.onDestroy();
    }

    Bitmap faceFrame;

    public void surfaceDraw(RectF rect, Canvas canvas) {
        Paint mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2f);
        mPaint.setTextSize(40f);

        if (canvas != null) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas.drawBitmap(faceFrame, null, rect, mPaint);
        }
    }

    public void clearDrawSurface() {
        Canvas canvas = draw_surface.getHolder().lockCanvas();
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        draw_surface.invalidate();
        draw_surface.getHolder().unlockCanvasAndPost(canvas);
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

//        Log.e(TAG,"face  x " + x + "face y " + y + " w " + w + " h " + h);
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
