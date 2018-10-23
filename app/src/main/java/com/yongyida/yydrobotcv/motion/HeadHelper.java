package com.yongyida.yydrobotcv.motion;

import android.content.Context;
import android.util.Log;

import com.yongyida.robot.communicate.app.common.send.SendClient;
import com.yongyida.robot.communicate.app.hardware.motion.send.data.FootControl;
import com.yongyida.robot.communicate.app.hardware.motion.send.data.HeadControl;
import com.yongyida.robot.communicate.app.hardware.motion.send.data.SteeringControl;

/**
 * @author Brandon on 2018/8/2
 * 控制头部运动
 **/
public class HeadHelper {

    private final static String TAG = HeadHelper.class.getCanonicalName();
    private static FootControl mFoodControl = new FootControl();
    private static HeadControl mHeadControl = new HeadControl();
    private static SteeringControl mHeadLeftRight = mHeadControl.getHeadLeftRightControl();
    private static SteeringControl mHeadUpDown = mHeadControl.getHeadUpDownControl();

    public static int stepLRH = 25; //默认 12
    public static int stepLRL= 10; //默认 12

    public static int stepUD = 10;

// 跟踪运动控制
    public static void initLRheadL(Context context) {
        mHeadControl.setAction(HeadControl.Action.LEFT_RIGHT);
        mHeadLeftRight.setMode(SteeringControl.Mode.DISTANCE_SPEED);
        mHeadLeftRight.getDistance().setType(SteeringControl.Distance.Type.BY);
        mHeadLeftRight.getDistance().setValue(10 * stepLRL);
        mHeadLeftRight.getSpeed().setUnit(SteeringControl.Speed.Unit.PERCENT);
        mHeadLeftRight.getSpeed().setValue(10);
        SendClient.getInstance(context).send(null, mHeadControl, null);
    }

    // 跟踪运动控制
    public static void initLRheadH(Context context) {
        mHeadControl.setAction(HeadControl.Action.LEFT_RIGHT);
        mHeadLeftRight.setMode(SteeringControl.Mode.DISTANCE_SPEED);
        mHeadLeftRight.getDistance().setType(SteeringControl.Distance.Type.BY);
        mHeadLeftRight.getDistance().setValue(10 * stepLRH);
        mHeadLeftRight.getSpeed().setUnit(SteeringControl.Speed.Unit.PERCENT);
        mHeadLeftRight.getSpeed().setValue(100);
        SendClient.getInstance(context).send(null, mHeadControl, null);
    }

    // 回正使用
    public static void initLRheadBack(Context context) {
        mHeadControl.setAction(HeadControl.Action.LEFT_RIGHT);
        mHeadLeftRight.setMode(SteeringControl.Mode.DISTANCE_SPEED);
        mHeadLeftRight.getDistance().setType(SteeringControl.Distance.Type.BY);
        mHeadLeftRight.getDistance().setUnit(SteeringControl.Distance.Unit.PERCENT);
        mHeadLeftRight.getDistance().setValue(50);
        mHeadLeftRight.getSpeed().setUnit(SteeringControl.Speed.Unit.PERCENT);
        mHeadLeftRight.getSpeed().setValue(70);
        SendClient.getInstance(context).send(null, mHeadControl, null);
    }
    public static void initLRFoot(SteeringControl control){
        control.setMode(SteeringControl.Mode.DISTANCE_SPEED);
        control.getDistance().setType(SteeringControl.Distance.Type.BY);
        control.getDistance().setUnit(SteeringControl.Distance.Unit.ANGLE);
        control.getDistance().setValue(80);
        control.getSpeed().setUnit(SteeringControl.Speed.Unit.PERCENT);
        control.getSpeed().setValue(70);
    }

    public static void headFootBackLeft(Context context){
        mHeadLeftRight.setNegative(false);
        initLRheadBack(context);

        initLRFoot(mFoodControl.getFoot());
        mFoodControl.setAction(FootControl.Action.LEFT);
        SendClient.getInstance(context).send(null,mFoodControl,null);
    }

    public static void headFootBackRight(Context context){
        mHeadLeftRight.setNegative(true);
        initLRheadBack(context);

        initLRFoot(mFoodControl.getFoot());
        mFoodControl.setAction(FootControl.Action.RIGHT);
        SendClient.getInstance(context).send(null,mFoodControl,null);
    }

    public static void headLeftL(Context context) {
        mHeadLeftRight.setNegative(true);
        initLRheadL(context);
        Log.e(TAG, "headLeft");
    }

    public static void headRightL(Context context) {
        mHeadLeftRight.setNegative(false);
        initLRheadL(context);
        Log.e(TAG, "headRight");
    }

    public static void headLeftH(Context context) {
        mHeadLeftRight.setNegative(true);
        initLRheadH(context);
        Log.e(TAG, "headLeft");
    }

    public static void headRightH(Context context) {
        mHeadLeftRight.setNegative(false);
        initLRheadH(context);
        Log.e(TAG, "headRight");
    }

    public static void headUp(Context context) {

        mHeadControl.setAction(HeadControl.Action.UP_DOWN);
        mHeadUpDown.setMode(SteeringControl.Mode.DISTANCE_SPEED);
        mHeadUpDown.getDistance().setType(SteeringControl.Distance.Type.BY);
        mHeadUpDown.setNegative(true);
        mHeadUpDown.getDistance().setValue(10 * stepUD);
        SendClient.getInstance(context).send(null, mHeadControl, null);
        Log.e(TAG, "headUp");

    }

    public static void headDown(Context context) {

        mHeadControl.setAction(HeadControl.Action.UP_DOWN);
        mHeadUpDown.setMode(SteeringControl.Mode.DISTANCE_SPEED);
        mHeadUpDown.getDistance().setType(SteeringControl.Distance.Type.BY);
        mHeadUpDown.setNegative(false);
        mHeadUpDown.getDistance().setValue(10 * stepUD);
        SendClient.getInstance(context).send(null, mHeadControl, null);
        Log.e(TAG, "headDown");

    }


    public static void stopMotin(Context context) {
        mHeadControl.setAction(HeadControl.Action.UP_DOWN);
        mHeadUpDown.setMode(SteeringControl.Mode.STOP);

        SendClient.getInstance(context).send(null, mHeadControl, null);

    }

    public static void headCenter(Context context){
        mHeadControl.setAction(HeadControl.Action.LEFT_RIGHT);
        mHeadLeftRight.setMode(SteeringControl.Mode.RESET);
        SendClient.getInstance(context).send(null,mHeadControl,null);
    }
}
