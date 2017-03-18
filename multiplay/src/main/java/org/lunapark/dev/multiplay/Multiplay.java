package org.lunapark.dev.multiplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by znak on 11.03.2017.
 */

public class Multiplay implements WifiP2pManager.ActionListener, ReceiverEvent {

    private final WifiP2pManager.ActionListener connectionListener;

    private Context context;
    private String TAG = "Multiplay";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private MultiplayEvent multiplayEvent;
    private boolean connected;
    private WifiP2pInfo info;
    private WifiP2pDevice device;

    // UDP Networking
    private int port, bufferSize;
    private byte[] buffer;
    private ExecutorService executorService;
    private Runnable addressSender, dataSender;
    private DatagramSocket dsReceive, dsSend;
    private DatagramPacket dpReceive, dpSend;
    private final Runnable serverReceiver;
    private InetAddress address;
    private String localAddress;
    private byte[] data;

    /**
     * Initialize Multiplay library
     *
     * @param context        - app context
     * @param multiplayEvent - Multiplay event listener class
     * @param port           - port
     * @param bufferSize     - buffer size in bytes (1024)
     */
    public Multiplay(Context context, final MultiplayEvent multiplayEvent, final int port, int bufferSize) {
        this.context = context;
        this.multiplayEvent = multiplayEvent;
        this.port = port;
        this.bufferSize = bufferSize;
        buffer = new byte[bufferSize];
        try {
            dsReceive = new DatagramSocket(port);
            dsSend = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        dpReceive = new DatagramPacket(buffer, bufferSize);
        dpSend = new DatagramPacket(buffer, bufferSize, null, port);
        executorService = Executors.newCachedThreadPool();
        serverReceiver = new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        dsReceive.receive(dpReceive);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String s = getString(dpReceive);
                    // For client
                    if (address == null) {
                        address = setAddr(s);
                        send(s);
                        Log.e(TAG, "Received server address: " + address.getHostAddress());
                    } else {
                        // For client and server
                        multiplayEvent.onReceiveData(s);
                    }
                }
            }
        };

        addressSender = new Runnable() {
            @Override
            public void run() {
                localAddress = getLocalIPAddress(true);
                Log.e(TAG, "Local address: " + localAddress);
                for (int i = 0; i < 10; i++) {
                    send(localAddress);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.e(TAG, "Stop send address");
            }
        };

        dataSender = new Runnable() {
            @Override
            public void run() {
                dpSend.setData(data);
                dpSend.setLength(data.length);
                try {
                    if (address != null) {
                        dpSend.setAddress(address);
                        dsSend.send(dpSend);
                    } else {
                        Log.e(TAG, "Address is null");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // Broadcast config
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, context.getMainLooper(), null);
        mReceiver = new MultiplayReceiver(mManager, mChannel, this);

        connectionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //success logic
                Log.e(TAG, "Success connection");
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
                connected = false;
                multiplayEvent.onFailure(getErrorReason(reason));
            }
        };

    }

    // TODO Optimize it
    private String getString(DatagramPacket datagramPacket) {
        return new String(datagramPacket.getData()).substring(0, datagramPacket.getLength());
    }

    /* register the broadcast receiver with the intent values to be matched */
    public void onResume() {
        context.registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    public void onPause() {
        context.unregisterReceiver(mReceiver);
    }

    public void discoverPeers() {
        Log.e(TAG, "Start discover");
        mManager.discoverPeers(mChannel, this);
    }

    public void connect(final WifiP2pDevice device) {
        this.device = device;
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, connectionListener);
    }

    public void disconnect() {
        Log.e(TAG, "Disconnect");
        if (dsSend != null) dsSend.close();
        executorService.shutdown();

        if (mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null
                            && group.isGroupOwner()) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
            mManager.cancelConnect(mChannel, connectionListener);
        }

    }


    public void send(String message) {
        send(message.getBytes());
    }

    public void send(byte[] data) {
        this.data = data;
        executorService.submit(dataSender);
    }

    private String getErrorReason(int reason) {
        String sReason = "Wi-fi error.";
        switch (reason) {
            case 1:
                sReason = "Wi-fi p2p is unsupported on the device.";
                break;
            case 2:
                sReason = "Wi-fi is busy or turned off.";
                break;
        }
        Log.e(TAG, "Fail. " + sReason);
        return sReason;
    }

    @Override
    public void onSuccess() {
    }

    @Override
    public void onFailure(int reason) {
        connected = false;
        multiplayEvent.onFailure(getErrorReason(reason));
    }

    @Override
    public void onStateChange() {
    }

    @Override
    public void onPeerChange(ArrayList<WifiP2pDevice> wifiP2pDevices) {
        if (!connected) {
            Log.e(TAG, "Peers change");
            multiplayEvent.onChangeDeviceList(wifiP2pDevices);
        }
    }

    @Override
    public void onConnectionChange(WifiP2pInfo info) {
        this.info = info;
        boolean isClient = info.isGroupOwner;
        connected = true;
        Log.e(TAG, "Connection change. Client: " + isClient);
        if (!isClient) {
            address = info.groupOwnerAddress;
            Log.e(TAG, "Address for connect: " + address.getHostAddress());
            executorService.submit(addressSender);
        } else {

            address = null;
        }
        executorService.submit(serverReceiver);
        multiplayEvent.onConnectionChange(isClient);

    }

    private InetAddress setAddr(String sName) {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(sName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return inetAddress;
    }

    private String getLocalIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    @Override
    public void onDeviceChange() {
    }
}
