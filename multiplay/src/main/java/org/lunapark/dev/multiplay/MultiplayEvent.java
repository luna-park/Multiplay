package org.lunapark.dev.multiplay;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.ArrayList;

/**
 * Created by znak on 11.03.2017.
 */

public interface MultiplayEvent {
    void onFailure(String reason);

    void onConnectionChange(boolean isClient);

    void onChangeDeviceList(ArrayList<WifiP2pDevice> wifiP2pDevices);

    void onReceiveData(String s);
}
