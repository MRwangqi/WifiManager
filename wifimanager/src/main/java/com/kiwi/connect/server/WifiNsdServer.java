package com.kiwi.connect.server;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import com.kiwi.connect.callback.WifiServer;
import com.kiwi.connect.callback.ServerCallBack;

import java.util.HashMap;
import java.util.Map;

public class WifiNsdServer extends WifiServer {

    private static final String TAG = WifiNsdServer.class.getName();

    private NsdManager.RegistrationListener mRegistrationListener;
    private NsdServiceInfo serviceInfo;
    private NsdManager mNsdManager;

    public WifiNsdServer(ServerCallBack callBack) {
        super(callBack);
    }

    @Override
    public void register(Context app, String serviceName, String serviceType, HashMap<String, String> map) {

        mNsdManager = (NsdManager) app.getSystemService(Context.NSD_SERVICE);
        serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(serviceType);
        serviceInfo.setPort(10000);//port must be >0

        if (map != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Map.Entry<String, String> m : map.entrySet()) {
                serviceInfo.setAttribute(m.getKey(), m.getValue());
            }
        } else {
            Log.e(TAG, "params require sdk 21");
        }


        mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                String mServiceName = NsdServiceInfo.getServiceName();
                Log.e(TAG, "Registered service. Actual name used: " + mServiceName);
                WifiNsdServer.this.callBack.onSuccess();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Failed to register service");
                WifiNsdServer.this.callBack.onError(String.valueOf(errorCode));
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Log.e(TAG, "Unregistered service");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Service unregistration failed");
            }
        };
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }


    @Override
    public void destroy() {
        if (mNsdManager != null)
            mNsdManager.unregisterService(mRegistrationListener);
    }
}
