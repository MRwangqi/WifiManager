package com.kiwi.connect.callback;

import android.content.Context;

public abstract class WifiClient {
    public ClientCallBack clientCallBack;

    public WifiClient(ClientCallBack clientCallBack) {
        this.clientCallBack = clientCallBack;
    }

    public abstract void discover(Context context, String serviceName, String serviceType);

    public abstract void unDiscover();
}

