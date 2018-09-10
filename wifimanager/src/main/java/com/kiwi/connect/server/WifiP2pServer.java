package com.kiwi.connect.server;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;

import com.kiwi.connect.callback.WifiServer;
import com.kiwi.connect.callback.ServerCallBack;

import java.lang.reflect.Method;
import java.util.HashMap;

public class WifiP2pServer extends WifiServer {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pDnsSdServiceInfo p2pDnsSdServiceInfo;

    public WifiP2pServer(ServerCallBack callBack) {
        super(callBack);
    }

    @Override
    public void register(Context app, String serviceName, String serviceType, HashMap<String, String> map) {
        mManager = (WifiP2pManager) app.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(app, app.getMainLooper(), null);
        p2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceName, serviceType, map);
        mManager.addLocalService(mChannel, p2pDnsSdServiceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                WifiP2pServer.this.callBack.onSuccess();
            }

            @Override
            public void onFailure(int arg0) {
                WifiP2pServer.this.callBack.onError(String.valueOf(arg0));
            }
        });
        mManager.discoverPeers(mChannel, null);
    }


    @Override
    public void destroy() {
        unRegisterServer();
    }

    private void unRegisterServer() {
        if (mManager != null)
            mManager.removeLocalService(mChannel, p2pDnsSdServiceInfo, null);

        if (mManager != null && mChannel != null) {
            //first try the method removeGroup
            mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onFailure(int reasonCode) {
                    Log.e("TAG", "Disconnect failed. Reason :" + reasonCode);
                    //guess the remove method not working. so delete the persistentGroup
                    deletePersistentGroups();
                }

                @Override
                public void onSuccess() {
                    Log.e("TAG", "Disconnect succeed");
                }

            });
        }
    }

    // force to close disconnect wifip2p when disconnect fails return code 2:framework BUSY.
    private void deletePersistentGroups() {
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(mManager, mChannel, netid, null);
                    }
                }
            }
            Log.e("TAG", "deletePersistentGroups onSuccess. Reason :");
        } catch (Exception e) {
            Log.e("TAG", "deletePersistentGroups Exception. Reason :");
            e.printStackTrace();
        }
    }

}
