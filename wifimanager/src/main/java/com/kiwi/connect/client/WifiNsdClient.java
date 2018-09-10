package com.kiwi.connect.client;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import com.kiwi.connect.callback.ClientCallBack;
import com.kiwi.connect.callback.WifiClient;

import java.util.HashMap;
import java.util.Map;

public class WifiNsdClient extends WifiClient {
    private static final String TAG = WifiNsdClient.class.getName();
    private NsdManager mNsdManager;
    private String serviceName;
    private HashMap<String, String> serviceMap = new HashMap<>();

    public WifiNsdClient(ClientCallBack clientCallBack) {
        super(clientCallBack);
    }

    @Override
    public void discover(Context context, String serviceName, String serviceType) {
        this.serviceName = serviceName;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mNsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, nsDicListener);
    }


    private NsdManager.DiscoveryListener nsDicListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onDiscoveryStarted(String serviceType) {
            Log.e(TAG, "Discovery Started");
            WifiNsdClient.this.clientCallBack.startDiscover();
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Stop Discovery Failed");
        }

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "");
            WifiNsdClient.this.clientCallBack.onFailed("Start Discovery Failed",errorCode);
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            Log.e(TAG, "Service Lost");
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            // judge found the serviceName
            if (serviceInfo.getServiceName().equals(serviceName)) {
                mNsdManager.resolveService(serviceInfo, resolveListener);
            }
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.e(TAG, "Discovery Stopped");
        }
    };

    private NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
            Log.e(TAG, "");
            WifiNsdClient.this.clientCallBack.onFailed("resolve failed",i);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Map<String, byte[]> map = nsdServiceInfo.getAttributes();
                for (Map.Entry<String, byte[]> m : map.entrySet()) {
                    serviceMap.put(m.getKey(), new String(m.getValue(), 0, m.getValue().length));
                }
                if (WifiNsdClient.this.clientCallBack.connectCondition(serviceMap)) {
                    WifiNsdClient.this.clientCallBack.onSuccess(nsdServiceInfo.getHost(), serviceMap);
                }
            } else {
                WifiNsdClient.this.clientCallBack.onSuccess(nsdServiceInfo.getHost(), serviceMap);
            }
        }
    };


    @Override
    public void unDiscover() {
        if (mNsdManager != null) {
            mNsdManager.stopServiceDiscovery(nsDicListener);
        }
    }
}
