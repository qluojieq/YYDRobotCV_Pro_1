// ISDKCallback.aidl
package com.yongyida.robot.brain.system;

interface ISDKCallback {
     void onAudio(String audio, int audioLen);
     void onReceive(int cmd, String msg);
 }
