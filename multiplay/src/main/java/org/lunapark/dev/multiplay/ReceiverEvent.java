package org.lunapark.dev.multiplay;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

import java.util.ArrayList;

/**
 * Created by znak on 11.03.2017.
 */

public interface ReceiverEvent {
    void onStateChange();

    void onPeerChange(ArrayList<WifiP2pDevice> wifiP2pDevices);

    void onConnectionChange(WifiP2pInfo info);

    void onDeviceChange();
}
