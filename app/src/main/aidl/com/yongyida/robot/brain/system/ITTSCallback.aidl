package com.yongyida.robot.brain.system;

interface ITTSCallback {
    void OnBegin();
    void OnPause();
    void OnResume();
    // error == null 表示发音正常
    void OnComplete(String error, String from);
}
