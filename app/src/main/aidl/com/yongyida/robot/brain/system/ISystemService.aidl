package com.yongyida.robot.brain.system;

import com.yongyida.robot.brain.system.MainBean;
import com.yongyida.robot.brain.system.ISystemCallback;
import com.yongyida.robot.brain.system.ITTSCallback;

interface ISystemService {
    // 添加回调
    void addCallback(String key, ISystemCallback callback);
    // 移除回调
    void removeCallback(String key);

    // 同步结果
    MainBean sendCommand(int cmd, String msg);
    // 异步回调
    void sendCmdCallback(int cmd, String msg, ISystemCallback callback);

    // 开始发音
    void startTTS(String words, String from, ITTSCallback callback);
    // 暂停发音
    void pauseTTS();
    // 恢复发音
    void resumeTTS();
    // 停止发音，并清空队列
    void stopTTS(String from);
    // 是否正在发音
    boolean isSpeaking();

}
