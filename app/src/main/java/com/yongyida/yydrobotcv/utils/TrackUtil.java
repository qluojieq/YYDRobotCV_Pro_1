package com.yongyida.yydrobotcv.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;

import com.yongyida.yydrobotcv.R;
import com.yongyida.yydrobotcv.readface.BaseApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dou.utils.DisplayUtil;
import dou.utils.StringUtils;
import mobile.ReadFace.YMFace;

import static com.yongyida.yydrobotcv.readface.BaseApplication.getAppContext;


/**
 * Created by mac on 16/6/23.
 */
public class TrackUtil {

    private static final int count = 20;
    private static final String TAG = TrackUtil.class.getSimpleName();
    private static List<Integer> happy_list = new ArrayList<>();

    public static boolean isHappy(int score) {
        happy_list.add(score);
        if (happy_list.size() <= count)
            return false;

        int count = 0;
        for (int i = 0; i < happy_list.size(); i++) {
            if (happy_list.get(i) == 1) count++;
        }
        happy_list.remove(0);
        return count > 15;
    }


    private static List<Integer> emo_list = new ArrayList<>();//储存每张面部表情的集合

    public static void addFace(YMFace face) {
        if (face == null) return;
        emo_list.add(getMaxFromArr(face.getEmotions()));
        if (emo_list.size() > count) {
            emo_list.remove(0);
        }
    }

    private static int getMaxFromArr(float arr[]) {
        int position = 0;
        float max = 0;
        for (int j = 0; j < arr.length; j++) {
            if (max <= arr[j]) {
                max = arr[j];
                position = j;
            }
        }
        return position;
    }


    public static boolean isSmile() {//微笑拍照
        if (emo_list.size() <= 18) return false;
        return countPosition(emo_list) == 0;
    }


    private static int countPosition(List<Integer> emo_list) {

        Map<Integer, Integer> map = new HashMap();
        for (int i = 0; i < emo_list.size(); i++) {
            int position = emo_list.get(i);
            Integer count = map.get(position);
            map.put(position, (count == null) ? 1 : count + 1);
        }

        int max = 0;
        int position = 0;

        Iterator<Integer> iter = map.keySet().iterator();

        while (iter.hasNext()) {
            int key = iter.next();
            int value = map.get(key);
            if (max <= value) {
                position = key;
                max = value;
            }
        }
        return position;
    }

    static int colorffList[] = {
            0xffffffff
    };
    static int colorList[] = {
            0xffffffff
    };
    public static void drawAnim(List<YMFace> faces, SurfaceView outputView, float scale_bit, int cameraId, String fps, boolean showPoint) {
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
                Log.e(TAG,"显示传送过来的数据"+faces.size());

                int size = DisplayUtil.dip2px(getAppContext(), 2);
                paint.setStrokeWidth(size);
                paint.setStyle(Paint.Style.STROKE);
                YMFace ymFace = faces.get(i);

                if (ymFace.getAge() > 0) {//有跟踪的情况才去跟踪
                float[] rect = ymFace.getRect();
                float[] pose = ymFace.getHeadpose();

                float x1 = viewW - rect[0] * scale_bit - rect[2] * scale_bit;
                if (cameraId == (BaseApplication.yu ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK))
                    x1 = rect[0] * scale_bit;
                float y1 = rect[1] * scale_bit;
                float rect_width = rect[2] * scale_bit;
                paint.setColor(colorList[ymFace.getTrackId() % colorList.length]);
                //draw rect
                RectF rectf = new RectF(x1, y1, x1 + rect_width, y1 + rect_width);
                canvas.drawRect(rectf, paint);

                //draw grid

                int line = 10;
                int per_line = (int) (rect_width / (line + 1));
                int smailSize = DisplayUtil.dip2px(getAppContext(), 1.5f);
                paint.setStrokeWidth(smailSize);
//                for (int j = 1; j < line + 1; j++) {
//                    canvas.drawLine(x1 + per_line * j, y1, x1 + per_line * j, y1 + rect_width, paint);
//                    canvas.drawLine(x1, y1 + per_line * j, x1 + rect_width, y1 + per_line * j, paint);
//                }


//                paint.setColor(colorffList[ymFace.getTrackId() % colorffList.length]);
//                //注意前置后置摄像头问题
//                float length = rect[3] * scale_bit / 5;
//                float width = rect[3] * scale_bit;
//                float heng = size / 2;
//                canvas.drawLine(x1 - heng, y1, x1 + length, y1, paint);
//                canvas.drawLine(x1, y1 - heng, x1, y1 + length, paint);
//
//                x1 = x1 + width;
//                canvas.drawLine(x1 + heng, y1, x1 - length, y1, paint);
//                canvas.drawLine(x1, y1 - heng, x1, y1 + length, paint);
//
//                y1 = y1 + width;
//                canvas.drawLine(x1 + heng, y1, x1 - length, y1, paint);
//                canvas.drawLine(x1, y1 + heng, x1, y1 - length, paint);
//
//                x1 = x1 - width;
//                canvas.drawLine(x1 - heng, y1, x1 + length, y1, paint);
//                canvas.drawLine(x1, y1 + heng, x1, y1 - length, paint);
                if (showPoint) {
                    paint.setColor(Color.rgb(57, 138, 243));
                    size = DisplayUtil.dip2px(getAppContext(), 2.5f);
                    paint.setStrokeWidth(size);
                    float[] points = ymFace.getLandmarks();

                    for (int j = 0; j < points.length / 2; j++) {
                        float x = viewW - points[j * 2] * scale_bit;
                        if (cameraId == (BaseApplication.yu ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK))
                            x = points[j * 2] * scale_bit;
                        float y = points[j * 2 + 1] * scale_bit;
                        canvas.drawPoint(x, y, paint);
                    }
                }
//                float[] headposes = ymFace.getHeadpose();
//                DLog.d(headposes[0]+" : "+headposes[1]+" : "+headposes[2]);


                    x1 = viewW - rect[0] * scale_bit - rect[2] * scale_bit;
                    if (cameraId == (BaseApplication.yu ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK))
                        x1 = rect[0] * scale_bit;
                    y1 = rect[1] * scale_bit;
                    StringBuffer sb = new StringBuffer();

//                    sb.append("personId :  "+ymFace.getPersonId() + " ");
//                    sb.append(ymFace.getGender() == 1 ? "M " : "");
//                    sb.append(ymFace.getGender() == 0 ? "F " : "");
                    sb.append( "颜值"+ymFace.getBeautyScore());
                    sb.append("/");
                    sb.append( "统计值"+ymFace.getFaceQuality());
//                    sb.append("/");
//                    if (ymFace.getGender()==1){
//                        sb.append( arrayMapMale.get(ymFace.getPersonId()-1).name);
//                    }else if (ymFace.getGender()==0){
//                        sb.append( arrayMapMale.get(ymFace.getPersonId()-1).name);
//                    }

                    sb.append("/");
                    //微笑情况
                    sb.append( currentEmtion(ymFace.getEmotions()));
                    sb.append("/");
//                    if (ymFace.getGlassValue()==1){
//                        sb.append( "戴眼镜");
//                    }else {
//                        sb.append( "无眼镜");
//                    }

                    sb.append("/");
                    sb.append(ymFace.getAge());
//                    sb.append(" /" + (int) rect[2]);
//                    sb.append(ymFace.getGlassScore()>0.5 ? "眼镜 " : "");
//                    int happy = (int) (ymFace.getEmotions()[0] * 100);
//                    if (happy >= 70) happystr = "开心";
//                    if (happy <= 20) happystr = "";
//
//                    sb.append(happystr);

//                    sb.append(ymFace.getGender() + "");
//                    if (ymFace.getBeautyScore() != 0)
//                        sb.append(ymFace.getBeautyScore());
                    paint.setColor(colorffList[ymFace.getTrackId() % colorffList.length]);
                    paint.setStrokeWidth(0);
                    paint.setStyle(Paint.Style.FILL);
                    int fontSize = DisplayUtil.dip2px(getAppContext(), 20);
                    paint.setTextSize(fontSize);
                    Bitmap bitmap;
                    if (ymFace.getGender()==0) {//女士用
                        bitmap = BitmapFactory.decodeResource(getAppContext().getResources(), R.mipmap.crown);
                    } else {
                        bitmap = BitmapFactory.decodeResource(getAppContext().getResources(), R.mipmap.king);
                    }
                    Rect rect_text = new Rect();
                    paint.getTextBounds(sb.toString(), 0, sb.toString().length(), rect_text);
                    paint.setColor(Color.rgb(250, 0, 0));
                    //写文字
//                    canvas.drawText(sb.toString()/*+"  width："+rect_width*/, x1-rect_width, y1 + rect_width, paint);

                    //变换
                    android.graphics.Camera camera;
                    camera = new android.graphics.Camera();
                    camera.rotateY(pose[2]/3);
                    camera.rotateX(pose[1]);
                    camera.rotateZ(pose[0]);

//                    camera.rotateX(-50);

                    int wi = bitmap.getWidth();
                    int hi = bitmap.getHeight();
                    float wScale = rect_width / wi;
                    Matrix matrix = new Matrix();
                    camera.getMatrix(matrix);
//                    camera.restore();


                    matrix.postScale(wScale, wScale);
                    Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, wi, hi, matrix, true);

                    canvas.drawBitmap(newBitmap, x1, y1 - newBitmap.getHeight(), null);
                    newBitmap.recycle();//回收下
                    Log.e(TAG,i+"gender预览 绘制皇冠  "+faces.get(i).getGender());
//                    canvas.drawVertices();
                }
            }

            if (!StringUtils.isEmpty(fps)) {
                paint.setColor(Color.RED);
                paint.setStrokeWidth(0);
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);

                int sizet = DisplayUtil.sp2px(getAppContext(), BaseApplication.yu ? 28 : 17);
                paint.setTextSize(sizet);
                canvas.drawText(fps, 20, viewH * 3 / 17, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputView.getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private static String[] emoStrs = {"喜悦", "悲伤", "", "", "惊讶", "愤怒", "正常"};
    public static String currentEmtion(float[] emos){
        if (null==emos)
            return "";
        //寻找输出数组最大值的下标
        int iemos[] = new int[emos.length];
        int max = 0;
        int position = 6;
        for (int i = 0; i < emos.length; i++) {
            iemos[i] = (int) (emos[i] * 100);
            if (max <= iemos[i]) {
                max = iemos[i];
                position = i;
            }
        }
       return emoStrs[position];
    }



    static Bitmap bmp = null;
    static YuvImage image = null;
    static ByteArrayOutputStream stream;
    static int ENLARGE_SCORE = 100;

    public static Bitmap byte2BitmapCrop(byte[] byteData, int width, int height, float[] f) {
        image = new YuvImage(byteData, ImageFormat.NV21, width, height, null);
        if (image != null) {
            try {
                stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
                bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int startX = (int)( (f[0]-ENLARGE_SCORE/2)<=0?0:(f[0]-ENLARGE_SCORE/2));
        int startY = (int)( (f[1]-ENLARGE_SCORE/2)<=0?0:(f[1]-ENLARGE_SCORE/2));
        int w = (int)((startX+ENLARGE_SCORE+f[2])<1280?(f[2]+ENLARGE_SCORE):(1280-startX));
        int h = (int)((startY+ENLARGE_SCORE+f[3])<720?(f[3]+ENLARGE_SCORE):(720-startY));
        Log.e(TAG,"截图时的最值w "+ (startX+ENLARGE_SCORE) +"的值");
        Log.e(TAG,"0 "+ startX+ " 1 "+ startY + " w "+ w + " h "+h);
        Log.e(TAG,"0 "+ f[0]+ " 1 "+ f[1] + " w "+ f[2] + " h "+f[3]);
        Bitmap bitmapCrop = Bitmap.createBitmap(bmp,startX ,startY ,w ,h );
        bmp.recycle();
        return bitmapCrop;
    }
    public static Bitmap byte2Bitmap(byte[] byteData, int width, int height) {
        image = new YuvImage(byteData, ImageFormat.NV21, width, height, null);
        if (image != null) {
            try {
                stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
                bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bmp;
    }
}
