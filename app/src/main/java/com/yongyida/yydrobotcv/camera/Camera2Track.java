package com.yongyida.yydrobotcv.camera;

import android.content.Context;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;


/**
 * Created by Brandon on 2017/11/8.
 */

public class Camera2Track extends CameraBase {


    private static String TAG = Camera2Track.class.getSimpleName();
    static Camera2Track mCamera2Track;

    private Camera2Track() {
        Log.e(TAG, "构造方法");
        //对相机是否可用进行判断
    }

    public static CameraBase getCameraInstance(Context context) {
        Log.e(TAG, "getCameraInstance");

//        WIDTH_PREVIEW = 1280;
//        HEIGHT_PREVIEW = 720;

        mContext = context;
        if (mCamera2Track != null) {

        } else {
            mCamera2Track = new Camera2Track();
        }
        return mCamera2Track;
    }


    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = null;
//            Log.e(TAG,"ImageAvailableCount");
        try {
                image = reader.acquireLatestImage();
//            image = reader.acquireNextImage();
            if (image == null) {
                return;
            }
            mPreviewListener.preview(ImageUtils.getDataFromImage(image, ImageUtils.COLOR_FormatNV21));
            image.close();
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
        }
    }
}
