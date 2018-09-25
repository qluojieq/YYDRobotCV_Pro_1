// ISDKTaskCallback.aidl
package com.yongyida.robot.brain.system;

interface ISDKTaskCallback {
    void OnSuccess(int cmd, String msg);
    void OnFailure(int cmd, String errmsg);
}
