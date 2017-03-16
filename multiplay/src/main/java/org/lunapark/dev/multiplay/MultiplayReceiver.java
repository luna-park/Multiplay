package org.lunapark.dev.multiplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.ArrayList;

public class MultiplayReceiver extends BroadcastReceiver {

    private WifiP2pManager.PeerListListener peerListListener;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;

    private ReceiverEvent receiverEvent;

    public MultiplayReceiver() {
        super();
    }

    public MultiplayReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, final ReceiverEvent receiverEvent) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.receiverEvent = receiverEvent;

        final ArrayList<WifiP2pDevice> wifiP2pDevices = new ArrayList<WifiP2pDevice>();
        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                wifiP2pDevices.clear();
                wifiP2pDevices.addAll(peers.getDeviceList());
                receiverEvent.onPeerChange(wifiP2pDevices);
            }
        };
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            receiverEvent.onStateChange();

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP

                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        receiverEvent.onConnectionChange(info);
                    }
                });
            }


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            receiverEvent.onDeviceChange();
        }
    }
}
