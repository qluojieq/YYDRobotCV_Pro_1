package com.yongyida.yydrobotcv.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO;
import static android.hardware.camera2.CaptureRequest.CONTROL_AF_MODE;

/**
 * Created by Brandon on 2017/10/18.
 */

public abstract class CameraBase implements ImageReader.OnImageAvailableListener {
    private static final String TAG = CameraBase.class.getSimpleName();
    PreviewListener mPreviewListener;
    static Context mContext;

    CameraDevice mCameraDevice;
    CameraManager mCameraManager;
    String CameraIds[];
    ImageReader mImageReader;
    CaptureRequest.Builder captureRequest;
    CameraCaptureSession mCameraCaptureSession;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    public static int WIDTH_PREVIEW = 1280;
    public static int HEIGHT_PREVIEW = 720;
    private static final int RETRY_COUNT = 5;//相机打开失败时重试次数
    private int retryCount = 0;


    @SuppressLint("MissingPermission")
    public void start() {
        retryCount++;
        Log.e(TAG, "startCamea");
        startBackgroundThread();

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                Log.e(TAG, "Time out waiting to lock camera opening");
                mCameraOpenCloseLock.release();
//                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            CameraIds = mCameraManager.getCameraIdList();
            if (CameraIds.length > 0)
                mCameraManager.openCamera(CameraIds[0], new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice cameraDevice) {
                        Log.e(TAG, "Camera打开成功");
                        mCameraOpenCloseLock.release();
                        afterOpenCamera(cameraDevice);
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                        mCameraOpenCloseLock.release();
                        Log.e(TAG, "Camera断开连接");
                    }

                    @Override
                    public void onError(@NonNull CameraDevice cameraDevice, int i) {
                        mCameraOpenCloseLock.release();
                        if (retryCount < RETRY_COUNT) {
                            try {
                                Thread.sleep(1000);
                                start();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.e(TAG, "打开相机重试次数" + retryCount + "Camera打开错误" + i);
                    }
                }, mBackgroundHandler);
            else
                Log.e(TAG, "相机异常");
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera打开失败");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void afterOpenCamera(CameraDevice cameraDevice) {
        mCameraDevice = cameraDevice;
        try {
            captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mImageReader = ImageReader.newInstance(WIDTH_PREVIEW, HEIGHT_PREVIEW, ImageFormat.YUV_420_888, 2);//默认开启ImageReader
            captureRequest.addTarget(mImageReader.getSurface());
            captureRequest.set(CONTROL_AF_MODE,
                    CONTROL_AF_MODE_CONTINUOUS_VIDEO);
            captureRequest.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            mImageReader.setOnImageAvailableListener(this, mBackgroundHandler);

            mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull final CameraCaptureSession cameraCaptureSession) {
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        cameraCaptureSession.setRepeatingRequest(captureRequest.build(), new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                            }

                            @Override
                            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                                Log.e(TAG, "onCaptureFailed");
//                                try {
//                                    cameraCaptureSession.stopRepeating();
//
//                                } catch (CameraAccessException e) {
//                                    e.printStackTrace();
//                                }
                                super.onCaptureFailed(session, request, failure);
                            }

                        }, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.e(TAG, "onConfigureFailed");
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "createSession 失败");
            e.printStackTrace();
        }
    }


    public void stop() {

//        try {
//            mCameraOpenCloseLock.acquire();
//
//            if (mCameraCaptureSession != null) {
//                mCameraCaptureSession.close();
//                mCameraCaptureSession = null;
//                Log.e(TAG, "关闭 CaptureSession");
//            }
//
//            if (mCameraDevice != null) {
//                Log.e(TAG, "关闭 mCameraDevice 前");
//                mCameraDevice.close();
//                mCameraDevice = null;
//                Log.e(TAG, "关闭 mCameraDevice 后");
//            }
//            if (mImageReader != null) {
//                mImageReader.close();
//                mImageReader = null;
//                Log.e(TAG, "关闭 ImageReader");
//            }
//            stopBackgroundThread();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            mCameraOpenCloseLock.release();
//        }
        try {
            if (mCameraCaptureSession!=null)
            mCameraCaptureSession.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (mBackgroundHandler!=null)
        mBackgroundHandler.postDelayed(new Runnable() {//延迟关闭相机
            @Override
            public void run() {
                if (mCameraCaptureSession!=null){
                    mCameraCaptureSession.close();
                    mCameraCaptureSession = null;
                }
                if (mCameraDevice!=null)//注意关闭顺序，先
                    mCameraDevice.close();
                mCameraDevice = null;
                if (mImageReader!=null)//注意关闭顺序，后
                    mImageReader.close();
                mImageReader = null;

//                isCameraOn = false;
                stopBackgroundThread();
            }

        },100);
        Log.e(TAG, "关闭 相机结束");
    }

    ;

    public void setListener(PreviewListener previewLister) {
        mPreviewListener = previewLister;
    }

    ;

    public void unsetListener() {
        mPreviewListener = null;
    }


    protected HandlerThread mBackgroundThread;
    protected Handler mBackgroundHandler;

    protected void startBackgroundThread() {
        Log.e(TAG,"开启CameraBackground 线程");
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {

        if (mBackgroundHandler == null)
            return;

        try {
            mBackgroundThread.quitSafely();
            Log.e(TAG, "关闭 线程 开始1");
            mBackgroundThread.join(1000);
            Log.e(TAG, "关闭 线程 开始2");
            mBackgroundThread = null;
            Log.e(TAG, "关闭 线程 开始3");
            mBackgroundHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "关闭 线程 结束 error");
        }
        Log.e(TAG, "关闭 线程 结束");
    }
}

