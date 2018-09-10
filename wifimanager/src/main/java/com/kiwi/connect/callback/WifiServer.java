package com.kiwi.connect.callback;

import android.content.Context;

import com.kiwi.connect.callback.ServerCallBack;

import java.util.HashMap;

public abstract class WifiServer {

    public ServerCallBack callBack;

    public WifiServer(ServerCallBack callBack) {
        this.callBack = callBack;
    }

    /**
     * 注册连接
     *
     * @param app
     * @param serviceName
     * @param serviceType
     * @param map
     */
    public abstract void register(Context app, String serviceName, String serviceType, HashMap<String, String> map);

    /**
     * 关闭注册连接
     */
    public abstract void destroy();

}
