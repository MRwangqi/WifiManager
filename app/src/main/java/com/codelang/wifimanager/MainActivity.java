package com.codelang.wifimanager;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kiwi.connect.WifiManager;
import com.kiwi.connect.callback.ClientCallBack;
import com.kiwi.connect.callback.ServerCallBack;
import com.kiwi.connect.client.WifiNsdClient;
import com.kiwi.connect.client.WifiP2pClient;
import com.kiwi.connect.server.WifiNsdServer;
import com.kiwi.connect.server.WifiP2pServer;

import java.net.InetAddress;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private final String SERVICE_NAME = "demo";
    private final String SERVICE_TYPE = "_demo._udp";

    WifiManager wifiManager;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = new WifiManager.Builder(this)
                .withServiceName(SERVICE_NAME)
                .withServiceType(SERVICE_TYPE)
                .withParam("name", "zhangsan")
                .build();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("搜索");
        progressDialog.setMessage("搜索中...");
    }

    public void nsdSearch(View view) {
        wifiManager.discoverServer(new WifiNsdClient(new ClientCallBack() {
            @Override
            public void startDiscover() {
                Log.e(TAG, "startDiscover: " + Thread.currentThread());
            }

            @Override
            public boolean connectCondition(HashMap<String, String> map) {
                return "zhangsan".equals(map.get("name"));
            }

            @Override
            public void onSuccess(InetAddress remoteAddress, HashMap<String, String> map) {
                Log.e(TAG, "onSuccess: " + remoteAddress.getHostAddress() + Thread.currentThread());
                Toast.makeText(MainActivity.this, "onSuccess: " + remoteAddress.getHostAddress(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String error, int code) {
                Log.e(TAG, "onFailed: " + error);
                Toast.makeText(MainActivity.this, "onFailed: ", Toast.LENGTH_SHORT).show();
            }
        }));

    }

    public void p2pSearch(View view) {
        wifiManager.discoverServer(new WifiP2pClient(new ClientCallBack() {
            @Override
            public void startDiscover() {
                Log.e(TAG, "startDiscover: ");
                progressDialog.show();
            }

            @Override
            public boolean connectCondition(HashMap<String, String> map) {
                return "zhangsan".equals(map.get("name"));
            }

            @Override
            public void onSuccess(InetAddress remoteAddress, HashMap<String, String> map) {
                Log.e(TAG, "onSuccess: " + remoteAddress.getHostAddress());

                Toast.makeText(MainActivity.this, "onSuccess: " + remoteAddress.getHostAddress(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onFailed(String error, int code) {
                Log.e(TAG, "onFailed: ");
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "onFailed: ", Toast.LENGTH_SHORT).show();
            }
        }));
    }


    public void nsdServer(View view) {
        wifiManager.registerWifiServer(new WifiNsdServer(new ServerCallBack() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "registerWifiServer success: ");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "registerWifiServer onError: ");
            }
        }));
    }

    public void p2pServer(View view) {
        wifiManager.registerWifiServer(new WifiP2pServer(new ServerCallBack() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "registerWifiServer success: ");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "registerWifiServer onError: ");
            }
        }));
    }

}
