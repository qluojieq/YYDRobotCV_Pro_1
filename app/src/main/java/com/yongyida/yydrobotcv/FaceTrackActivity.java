package com.yongyida.yydrobotcv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.yongyida.yydrobotcv.service.PersonDetectService;
import com.yongyida.yydrobotcv.utils.TrackUtil;

import java.util.List;

import dou.utils.DLog;
import mobile.ReadFace.YMFace;

/**
 * Created by mac on 16/7/4.
 */
public class FaceTrackActivity extends BaseCameraActivity {

    public static final String TAG = "PointActivity";
    boolean showPoint = false;
    boolean preFrame = false;
    private boolean threadStart;


    private  int noFaceCount = 0;
    private final int NO_FACE_THRESHOLD  = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main31);
        setCamera_max_width(-1);
        initCamera();
        showFps(true);
        totalCount = 0;
        trackingMap = new SimpleArrayMap<>();
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        noFaceCount = 0;
//        requestPermissions(new String[]{Manifest.permission.CAMERA},10);
    }


    private SimpleArrayMap<Integer, YMFace> trackingMap;
    private int totalCount = 0;
    @Override
    protected List<YMFace> analyse(byte[] bytes, final int iw, final int ih) {

        if (faceTrack == null) return null;
        List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);


        if (faces != null && faces.size() > 0) {
            Log.e(TAG,"face");

            if (!preFrame) {
                if (!threadStart) {
                    threadStart = true;

                    if (trackingMap.size() > 50) trackingMap.clear();
                    //找到最大人脸框
                    int maxIndex = 0;
                    for (int i = 1; i < faces.size(); i++) {
                        if (faces.get(maxIndex).getRect()[2] <= faces.get(i).getRect()[2]) {
                            maxIndex = i;
                        }
                    }

                    final YMFace ymFace = faces.get(maxIndex);
                    final int anaIndex = maxIndex;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final int trackId = ymFace.getTrackId();
                                final String name = identifyName();
                                if (!trackingMap.containsKey(trackId)) {
                                    float[] headposes = ymFace.getHeadpose();
                                    if (!(Math.abs(headposes[0]) > 30
                                            || Math.abs(headposes[1]) > 30
                                            || Math.abs(headposes[2]) > 30)) {
                                        int gender = faceTrack.getGender(anaIndex);
                                        int gender_confidence = faceTrack.getGenderConfidence(anaIndex);
                                        //识别


                                        float [] feature1 = faceTrack.getFaceFeature(0);
                                        int personId =  getMostSimilar(feature1,gender);
                                        ymFace.setPersonId(personId);
                                        //人流统计
                                        totalCount ++;
                                        //颜值UnsatisfiedLinkError
                                        Log.e(TAG,"颜值"+trackId);
                                        ymFace.setBeautyScore(faceTrack.getFaceBeautyScore(anaIndex));
                                        //眼镜
//                                        ymFace.setGlassValue(faceTrack.getGlassValue(anaIndex));
                                        //有可能获取性别可信度不够高，需重新获取
                                        DLog.d(gender + " ：" + gender_confidence);
                                        if (gender_confidence >= 90) {
                                            ymFace.setAge(faceTrack.getAge(anaIndex));
                                            ymFace.setGender(gender);
                                            trackingMap.put(trackId, ymFace);
                                        }

                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                threadStart = false;
                            }
                        }
                    }).start();
                }

                for (int i = 0; i < faces.size(); i++) {
                    YMFace ymFace = faces.get(i);
                    int trackId = ymFace.getTrackId();
                    if (trackingMap.containsKey(trackId)) {
                        YMFace face = trackingMap.get(trackId);
                        ymFace.setEmotions(faceTrack.getEmotion(i));
                        ymFace.setAge(face.getAge());
                        ymFace.setFaceQuality(totalCount);
                        ymFace.setBeautyScore(face.getBeautyScore());
//                        ymFace.setGlassValue(faceTrack.getGlassValue(i));
                        ymFace.setPersonId(face.getPersonId());
                        ymFace.setGender(face.getGender());
                    }
                }
            } else {

                for (int i = 0; i < faces.size(); i++) {
                    YMFace ymFace = faces.get(i);
                    ymFace.setAge(faceTrack.getAge(i));
                    ymFace.setGender(faceTrack.getGender(i));
                    ymFace.setGenderConfidence(faceTrack.getGenderConfidence(i));
                }
            }
            noFaceCount = 0;// 有则取零
        }else {

            noFaceCount ++; // 没有就叠加
            if (noFaceCount == NO_FACE_THRESHOLD){
                Intent intent = new Intent(this, PersonDetectService.class);
                intent.putExtra("startType","start");
                startService(intent);
                Log.e(TAG,"结束人脸检测，开始人体检测");
                FaceTrackActivity.this.finish();
            }
            Log.e(TAG,"no face " + noFaceCount);
        }
        return faces;
    }

    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps) {
        TrackUtil.drawAnim(faces, draw_view, scale_bit, cameraId, fps, showPoint);
    }
    public String identifyName(){
        String name = "";
        return  name;
    }





}
