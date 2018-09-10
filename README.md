# WifiManager

本库主要是为了解决两端手机点对点搜索问题，对于两端手机的数据传输，需要自行实现socket通信

>WifiP2p直连


[Wi-Fi P2P 官方介绍](https://developer.android.google.cn/guide/topics/connectivity/wifip2p)

wifip2p直连方式针对的是android手机，两端只需要打开wifi，通过一端注册服务一端搜索，就可以搜索到对方的IP地址和暴露的参数

>NSD(network service discovery)

[Wi-Fi NSD官方介绍](https://developer.android.google.cn/training/connect-devices-wirelessly/nsd)
```
Network service discovery (NSD) gives your app access to services that other devices provide on a local network
```
正如官往介绍，NSD要想实现两端手机的通信必须是在一个局域网环境下才能搜索到对方。</br>
NSD方式显然没有wifip2p那么便捷，需要自己去构建一个局域网，局域网环境可以通过一台设备开启热点，让另一台设备连接。NSD还有一个过人之处，那就是跨平台，可以搜索到```iOS设备```暴露出去服务，拿到对方的IP和端口，github也有示例demo [ios-android-bonjour](https://github.com/jaanus/ios-android-bonjour)

### server注册

> NSD server
```
        WifiManager wifiManager = new WifiManager.Builder(null)
                .withServiceName("demo")
                .withServiceType("_demo._udp")
                .withParam("image_port", "1000")
                .build();

        //注册服务
        wifiManager.registerWifiServer(new WifiNsdServer(new ServerCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(String error) {

            }
        }));
```
> WifiP2p server
```
 WifiManager wifiManager = new WifiManager.Builder(null)
                .withServiceName("demo")
                .withServiceType("_demo._udp")
                .withParam("image_port", "1000")
                .build();

        //注册服务
        wifiManager.registerWifiServer(new WifiP2pServer(new ServerCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(String error) {

            }
        }));
```

### client搜索
>NSD client
```
 WifiManager wifiManager = new WifiManager.Builder(null)
                .withServiceName("demo")
                .withServiceType("_demo._udp")
                .build();

        //发现服务
        wifiManager.discoverServer(new WifiNsdClient(new ClientCallBack() {
            @Override
            public void startDiscover() {

            }

            @Override
            public boolean connectCondition(HashMap<String, String> map) {
                  return "image_port".equals(map.get("1000"));
            }

            @Override
            public void onSuccess(InetAddress remoteAddress, HashMap<String, String> map) {

            }

            @Override
            public void onFailed(String error, int code) {

            }
        }));
```
>WifiP2p client
```
 WifiManager wifiManager = new WifiManager.Builder(null)
                .withServiceName("demo")
                .withServiceType("_demo._udp")
                .build();

        //发现服务
        wifiManager.discoverServer(new WifiP2pClient(new ClientCallBack() {
            @Override
            public void startDiscover() {

            }

            @Override
            public boolean connectCondition(HashMap<String, String> map) {
                return true;
            }

            @Override
            public void onSuccess(InetAddress remoteAddress, HashMap<String, String> map) {

            }

            @Override
            public void onFailed(String error, int code) {

            }
        }));
```
