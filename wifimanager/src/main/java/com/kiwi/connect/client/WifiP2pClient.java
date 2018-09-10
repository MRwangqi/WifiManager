package com.kiwi.connect.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import com.kiwi.connect.callback.ClientCallBack;
import com.kiwi.connect.callback.WifiClient;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class WifiP2pClient extends WifiClient implements WifiP2pManager.ConnectionInfoListener {
    private static final String TAG = WifiP2pClient.class.getName();

    private Context context;

    private IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiBroadCastReceiver broadCastReceiver;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private HashMap<String, String> p2pServices = new HashMap<>();

    public WifiP2pClient(ClientCallBack clientCallBack) {
        super(clientCallBack);
    }

    @Override
    public void discover(Context context, final String serviceName, String serviceType) {
        this.context = context;

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, context.getMainLooper(), null);
        broadCastReceiver = new WifiBroadCastReceiver(mManager, mChannel, this);
        context.registerReceiver(broadCastReceiver, intentFilter);


        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomain, Map record, WifiP2pDevice device) {

                for (Object m : record.entrySet()) {
                    Map.Entry<String, String> entry = ((Map.Entry<String, String>) m);
                    p2pServices.put(entry.getKey(), entry.getValue());
                }

                if (WifiP2pClient.this.clientCallBack.connectCondition(p2pServices) && fullDomain.contains(serviceName)) {
                    p2pConnect(device);
                }

            }
        };

        mManager.setDnsSdResponseListeners(mChannel, null, txtListener);
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mManager.removeServiceRequest(mChannel, serviceRequest, null);
        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int code) {
            }
        });
        //开始搜索
        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                WifiP2pClient.this.clientCallBack.startDiscover();
            }

            @Override
            public void onFailure(int code) {
                WifiP2pClient.this.clientCallBack.onFailed("discoverServices onFailure", code);
            }
        });
    }

    /**
     * 搜索到设备后开始连接设备，触发广播的WIFI_P2P_CONNECTION_CHANGED_ACTION行为
     *
     * @param srcDevice
     */
    private void p2pConnect(WifiP2pDevice srcDevice) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = srcDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        // 0 indicates the least inclination to be a group owner.
        config.groupOwnerIntent = 0;

        if (serviceRequest != null)
            mManager.removeServiceRequest(mChannel, serviceRequest,
                    new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(int arg0) {
                        }
                    });
        //会触发广播，广播会触发onConnectionInfoAvailable的回调
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int errorCode) {
                WifiP2pClient.this.clientCallBack.onFailed("connect onFailure", errorCode);
            }
        });
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        try {
            WifiP2pClient.this.clientCallBack.onSuccess(InetAddress.getByName(info.groupOwnerAddress.getHostAddress()), p2pServices);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unDiscover() {
        if (broadCastReceiver != null) {
            context.unregisterReceiver(broadCastReceiver);
        }
        if (mManager != null && mChannel != null) {
            //first try the method removeGroup
            mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                @Override
                public void onFailure(int reasonCode) {
                    deletePersistentGroups();
                }

                @Override
                public void onSuccess() {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class WifiBroadCastReceiver extends BroadcastReceiver {
        WifiP2pManager mManager;
        WifiP2pManager.Channel mChannel;
        WifiP2pManager.ConnectionInfoListener listener;

        public WifiBroadCastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, WifiP2pManager.ConnectionInfoListener listener) {
            this.listener = listener;
            this.mChannel = mChannel;
            this.mManager = mManager;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                if (mManager == null) {
                    return;
                }
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    mManager.requestConnectionInfo(mChannel, listener);
                }
            }
        }
    }
}
