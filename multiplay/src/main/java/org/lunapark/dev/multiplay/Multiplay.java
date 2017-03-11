package org.lunapark.dev.multiplay;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by znak on 11.03.2017.
 */

public class Multiplay implements WifiP2pManager.ActionListener {

    private Context context;
    private String TAG = "Multiplay";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;


    public Multiplay(Context context) {
        this.context = context;
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, context.getMainLooper(), null);
        mReceiver = new MultiplayReceiver(mManager, mChannel, context);

        Log.e(TAG, "Start discover");
        mManager.discoverPeers(mChannel, this);
    }

    /* register the broadcast receiver with the intent values to be matched */
    public void onResume() {
        context.registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    public void onPause() {
        context.unregisterReceiver(mReceiver);
    }

    @Override
    public void onSuccess() {
        Log.e(TAG, "Success");
    }

    @Override
    public void onFailure(int reason) {
        Log.e(TAG, "Fail. Code: " + reason);
    }
}
