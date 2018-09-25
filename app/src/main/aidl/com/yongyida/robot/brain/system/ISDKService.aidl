// ISDKService.aidl
package com.yongyida.robot.brain.system;

import com.yongyida.robot.brain.system.ISDKCallback;
import com.yongyida.robot.brain.system.ITTSCallback;
import com.yongyida.robot.brain.system.MainBean;
import com.yongyida.robot.brain.system.ISDKTaskCallback;

interface ISDKService {
    // 添加回调
    void addCallback(String key, ISDKCallback callback);
    // 移除回调
    void removeCallback(String key);

    // 同步调用功能
    MainBean sendCommand(int cmd, String msg);
    // 异步回调
    void sendCmdCallback(int cmd, String msg, ISDKTaskCallback callback);

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
