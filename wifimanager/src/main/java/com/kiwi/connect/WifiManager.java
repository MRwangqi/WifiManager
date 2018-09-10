package com.kiwi.connect;

import android.content.Context;

import com.kiwi.connect.callback.WifiClient;
import com.kiwi.connect.callback.WifiServer;

import java.util.HashMap;

public class WifiManager {
    private String serviceName;
    private String serviceType;
    private HashMap<String, String> map;
    private Context context;

    private WifiServer wifiServer;
    private WifiClient wifiClient;

    private WifiManager() {
    }

    private WifiManager(Builder builder) {
        this.serviceName = builder.serviceName;
        this.serviceType = builder.serviceType;
        this.map = builder.map;
        this.context = builder.context;
    }

    /**
     * register server
     * <p>
     * first remove register,then register
     * resolve register multi server cause client discover multi server
     *
     * @param wifiServer
     */
    public void registerWifiServer(WifiServer wifiServer) {
        unRegisterWifiServer();
        this.wifiServer = wifiServer;
        this.wifiServer.register(context, serviceName, serviceType, map);
    }

    /**
     * 卸载服务
     */
    public void unRegisterWifiServer() {
        if (wifiServer != null)
            this.wifiServer.destroy();
    }

    /**
     * 搜索服务
     */
    public void discoverServer(WifiClient wifiClient) {
        this.wifiClient = wifiClient;
        unDiscoverServer();
        this.wifiClient.discover(context, serviceName, serviceType);
    }

    /**
     * 关闭搜索
     */
    public void unDiscoverServer() {
        if (this.wifiClient != null)
            this.wifiClient.unDiscover();
    }


    public static class Builder {
        private String serviceName;
        private String serviceType;
        private HashMap<String, String> map = new HashMap<>();
        private Context context;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder withServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder withServiceType(String serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public Builder withParam(String key, String value) {
            this.map.put(key, value);
            return this;
        }

        public WifiManager build() {
            return new WifiManager(this);
        }
    }


}
