package com.yongyida.yydrobotcv.tts;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.yongyida.robot.brain.system.ISystemService;
import com.yongyida.robot.brain.system.ITTSCallback;


/**
 * @author Brandon on 2018/4/24
 **/
public class TTSManager {
    private static String TAG = TTSManager.class.getSimpleName();
    Context mContext;
    static ISystemService TTSControl;
    static String TTSState = "disconnect";

    public static void bindService(Context context){
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.yongyida.robot.system","com.yongyida.robot.brain.system.SystemService");
        intent.setComponent(componentName);
        context.bindService(intent,connection ,Context.BIND_AUTO_CREATE);
    }
    static ServiceConnection connection = new  ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG,"services is connected");
            TTSState = "connect";
            TTSControl = ISystemService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG,"services is disconnected");
            TTSState = "disconnect";
        }
    };

    public static void unBindService(Context context){
        context.unbindService(connection);
    }

    public static void TTS(String msg, ITTSCallback callback){
        Log.e(TAG,"speak" + msg);
        try {
            if (TTSControl!=null){
                TTSControl.startTTS(msg,null ,callback);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public static void TTSSTop(Context context){
        Log.e(TAG,"stop  speak");
        try {
            if (TTSControl!= null && TTSControl.isSpeaking()){
                TTSControl.stopTTS(context.getPackageName());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
