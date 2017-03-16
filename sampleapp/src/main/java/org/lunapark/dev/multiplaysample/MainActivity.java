package org.lunapark.dev.multiplaysample;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.lunapark.dev.multiplay.Multiplay;
import org.lunapark.dev.multiplay.MultiplayEvent;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements MultiplayEvent {

    private Multiplay multiplay;
    private TextView tvInfo;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tvInfo = (TextView) findViewById(R.id.tvInfo);

        multiplay = new Multiplay(this, this, 8888, 256);
        multiplay.discoverPeers();
        executorService = Executors.newSingleThreadExecutor();

    }

    @Override
    protected void onResume() {
        super.onResume();
        multiplay.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        multiplay.onPause();
    }

    @Override
    protected void onStop() {
        executorService.shutdown();
        multiplay.disconnect();
        super.onStop();
    }

    @Override
    public void onFailure(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onConnectionChange(boolean isClient) {
        tvInfo.setText("Connected!");
        if (isClient) {

            for (int i = 0; i < 50; i++) {

                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        multiplay.send("Hola!");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }

    }

    @Override
    public void onChangeDeviceList(ArrayList<WifiP2pDevice> wifiP2pDevices) {
        Log.e("Multiplay Activity", "Device list size: " + wifiP2pDevices.size());
        String testDeviceName = "ye_olde_well";
//        String testDeviceName = "l-p_p_d";
        tvInfo.setText("");
        for (WifiP2pDevice device : wifiP2pDevices) {
            tvInfo.append("\n");
            tvInfo.append(device.deviceName);
            tvInfo.append("\n");
            tvInfo.append(device.deviceAddress);

            if (device.deviceName.equals(testDeviceName)) {
                multiplay.connect(device);
                break;
            }
        }
    }

    @Override
    public void onReceiveData(String s) {
        Log.e("Multiplay Activity", "Data received: " + s);
    }
}
