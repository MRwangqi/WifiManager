package com.kiwi.connect.callback;

import java.net.InetAddress;
import java.util.HashMap;

public interface ClientCallBack {
    /**
     * 开始搜索
     */
    void startDiscover();

    /**
     * 自定义连接到服务端的条件
     * @param map
     * @return
     */
    boolean connectCondition(HashMap<String, String> map);

    /**
     * 连接成功
     * @param remoteAddress 返回对方的IP
     * @param map  获取对方暴露的参数
     */
    void onSuccess(InetAddress remoteAddress, HashMap<String, String> map);

    /**
     * 搜索、连接、解析失败的回调
     * @param error
     * @param code
     */
    void onFailed(String error, int code);

}
