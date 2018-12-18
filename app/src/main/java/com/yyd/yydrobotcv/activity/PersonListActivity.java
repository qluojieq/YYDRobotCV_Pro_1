package com.yyd.yydrobotcv.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.yongyida.yydrobotcv.FaceTrackActivity;
import com.yongyida.yydrobotcv.MianListActivity;
import com.yongyida.yydrobotcv.R;

import com.yongyida.yydrobotcv.motion.HeadHelper;
import com.yongyida.yydrobotcv.service.FaceDetectService;
import com.yongyida.yydrobotcv.utils.CommonUtils;

import static com.yongyida.yydrobotcv.motion.HeadHelper.updateSL;
import static com.yongyida.yydrobotcv.service.FaceDetectService.updateOffset;


public class PersonListActivity extends AppCompatActivity {
    private static final String TAG = PersonListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_test_activity);
         width_et = findViewById(R.id.test_w);
         height_et = findViewById(R.id.test_h);
         speed_et = findViewById(R.id.test_s);
         offSet_et = findViewById(R.id.test_l);
    }


    public void startPersonRegist(View view) {
        Log.e(TAG, "启动注册人脸");
        Intent intent = new Intent(this, MianListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }


    public void startPersonDetect(View view) {
        Log.e(TAG, "开始人体检测");
//        Intent intent = new Intent(this, PirPersonDetectService.class);
//        intent.putExtra("startType","start");
//        startService(intent);
        Intent intent = new Intent();
        intent.putExtra("startType", "start");
        ComponentName componentName = new ComponentName("com.yongyida.yydrobotcv", "com.yongyida.yydrobotcv.service.PirPersonDetectService");
        intent.setComponent(componentName);
        startService(intent);
    }

    public void closePersonDetect(View view) {
        Log.e(TAG, "关闭人体检测");
//        Intent intent = new Intent(this, PersonDetectService.class);
//        intent.putExtra("startType","stop");
//        startService(intent);
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.yongyida.yydrobotcv", "com.yongyida.yydrobotcv.service.PirPersonDetectService");
        intent.setComponent(componentName);

//        startService(intent);
//        Intent intent = new Intent(this,PirPersonDetectService.class);
        stopService(intent);
    }

    public void startFaceDetect(View view) {
        Log.e(TAG, "开始人脸检测服务");
//        Intent intent = new Intent(this, FaceDetectService.class);
//        intent.putExtra("startType","active_interaction");
////        intent.putExtra("cmd","1"); //blockly使用：用户id
////        intent.putExtra("tag","10000");//超时时长
//        startService(intent);
//        Intent intent = new Intent(this, FaceTrackActivity.class);
//        startActivity(intent);

        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.yongyida.yydrobotcv", "com.yongyida.yydrobotcv.service.FaceDetectService");
        intent.setComponent(componentName);
        intent.putExtra("startType", "active_interaction");
        intent.putExtra("msg", "notSayHello");
        startService(intent);
    }

    public void stopFaceDetect(View view) {
        Log.e(TAG, "关闭人脸检测服务");
//        Intent intent = new Intent(this, FaceDetectService.class);
//        intent.putExtra("startType","stopTest");
//        intent.putExtra("cmd","1"); //blockly使用用户id
//        intent.putExtra("tag","-1");//-1直接调用停止
//        startService(intent);

//        Intent intent = new Intent();
//        ComponentName componentName = new ComponentName("com.yongyida.yydrobotcv", "com.yongyida.yydrobotcv.service.FaceDetectService");
//        intent.setComponent(componentName);
//        intent.putExtra("startType", "stopTest");
//        startService(intent);


    }

    public void startCCTV(View view) {
//        FindDangersService.startActionFoo(this);
//        Intent intent = new Intent(this, FindDangersActivity.class);
//        startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        callpremission();
    }

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    public void startFFGame(View view) { // 猜拳游戏
//        ComponentName componentName = new ComponentName("com.yydrobo.yydrobofggame","com.yydrobo.yydrobofggame.activity.StartActivity");
        //        intent.setComponent(componentName);

        Intent intent = new Intent(this, FaceTrackActivity.class);
        startActivity(intent);
    }



    public void UpMotion(View view) {
        HeadHelper.headUp(this);
    }

    public void DownMotion(View view) {
        HeadHelper.headDown(this);
    }

    public void RightMotion(View view) {
        HeadHelper.headRightL(this);
    }

    public void LeftMotion(View view) {
        HeadHelper.headLeftH(this);
    }

    public void StopMotion(View view) {
        HeadHelper.headCenter(this);
    }

    public void RightMotionFoot(View view) {
//        HeadHelper.headFootBackRight(this);
        HeadHelper.linkedLeft(this);
    }

    public void LeftMotionFoot(View view) {
//        HeadHelper.headFootBackLeft(this);
        HeadHelper.linkedRight(this);
    }

    // 主动交互 调试
    EditText width_et;
    EditText height_et;
    EditText speed_et;
    EditText offSet_et;
    String w,h,s,o;
    public void setProperty(View view) {
        Log.e(TAG,"设置跟随属性");
        w = width_et.getText().toString();
        h = height_et.getText().toString();
        s = offSet_et.getText().toString();
        o = speed_et.getText().toString();
        if (!TextUtils.isEmpty(w)&&!TextUtils.isEmpty(h)&&!TextUtils.isEmpty(s)&&!TextUtils.isEmpty(o)){
            FaceDetectService.TRACK_RANGE_WIDTH = Integer.parseInt(w);
            FaceDetectService.TRACK_RANGE_HEIGHT = Integer.parseInt(h);
            HeadHelper.stepLRH = Integer.parseInt(s);
            HeadHelper.stepUD = Integer.parseInt(o);
            updateOffset();
            updateSL();
            CommonUtils.serviceToast(this,"设置成功 ");
        }else {
            CommonUtils.serviceToast(this,"不能有空值 ");
        }

    }

    public void resetProperty(View view) {
        width_et.setText("540");
        height_et.setText("360");
        speed_et.setText("10");
        offSet_et.setText("5");
        setProperty(view);
    }


    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    //获取权限
    public  void callpremission() {
        //系统版本号23/6.0之后/api23
        if (Build.VERSION.SDK_INT >= 23) {
            //检查有没有所需的权限 PackageManager.PERMISSION_GRANTED：授权了权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //请求获取所需的权限，第二个参数：需要的权限（可以多个集合）第三个参数：请求码
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE ,Manifest.permission.INTERNET}, REQUIRE_CODE_CALL_CAMERA);
                return;
            }
        }
    }

    private final static int REQUIRE_CODE_CALL_CAMERA = 10;

    //权限获取回调的方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUIRE_CODE_CALL_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("权限log", "回调");
                } else {
                    // Permission Denied拒绝
                    Toast.makeText(this, "CAMERA Denied", Toast.LENGTH_SHORT)
                            .show();
                    SharedPreferences gosetting = getSharedPreferences("gosetting", MODE_PRIVATE);
                    boolean isGoSetting = gosetting.getBoolean("isGoSetting", false);
                    //用户首次拒绝申请权限时，不需弹窗提示去设置申请权限
                    if (isGoSetting) {
                        //当缺少权限时弹窗提示
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setIcon(R.mipmap.ic_launcher)
                                .setTitle("缺少权限")
                                .setMessage("去设置权限")
                                .setPositiveButton("GoSetting", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //打开App的设置
                                        getAppDetailSettingIntent(getBaseContext());
                                    }
                                }).show();
                    }
                    SharedPreferences.Editor edit = gosetting.edit();
                    edit.putBoolean("isGoSetting", true).commit();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //打开App的设置
    private void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(localIntent);
    }

}
