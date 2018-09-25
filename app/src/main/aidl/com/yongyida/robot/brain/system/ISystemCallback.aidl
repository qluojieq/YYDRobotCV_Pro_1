package com.yongyida.robot.brain.system;
interface ISystemCallback {
    void OnSuccess(int cmd, String msg);
    void OnFailure(int cmd, String errmsg);
}
