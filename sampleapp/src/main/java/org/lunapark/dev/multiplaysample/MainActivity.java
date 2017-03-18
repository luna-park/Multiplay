package org.lunapark.dev.multiplaysample;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.lunapark.dev.multiplay.Multiplay;
import org.lunapark.dev.multiplay.MultiplayEvent;

import java.util.ArrayList;

public class MainActivity extends Activity implements MultiplayEvent {

    private String TAG = "Multiplay Activity";

    private Multiplay multiplay;
    private TextView tvInfo;
    private ListView lvHosts;
    private Button btnSend;
    private ArrayList<String> hosts;
    private ArrayList<WifiP2pDevice> p2pDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tvInfo = (TextView) findViewById(R.id.tvInfo);
        lvHosts = (ListView) findViewById(R.id.lvHosts);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setVisibility(View.INVISIBLE);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multiplay.send("ME55AG3" + SystemClock.currentThreadTimeMillis());
            }
        });

        hosts = new ArrayList<String>();
//        hosts.add("Sample");
//        hosts.add("Sample");
//        hosts.add("Sample");
//        hosts.add("Sample");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, hosts);
        lvHosts.setAdapter(arrayAdapter);

        lvHosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "Connect with: " + p2pDevices.get(position).deviceName);
                multiplay.connect(p2pDevices.get(position));
                lvHosts.setVisibility(View.GONE);
            }
        });

        multiplay = new Multiplay(this, this, 8888, 256);
        multiplay.discoverPeers();

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
        lvHosts.setVisibility(View.GONE);
        btnSend.setVisibility(View.VISIBLE);

    }

    @Override
    public void onChangeDeviceList(ArrayList<WifiP2pDevice> wifiP2pDevices) {
        Log.e(TAG, "Device list size: " + wifiP2pDevices.size());
        String testDeviceName = "ye_olde_well";
//        String testDeviceName = "l-p_p_d";
//        tvInfo.setText("");
        hosts.clear();
        p2pDevices = wifiP2pDevices;
        for (WifiP2pDevice device : wifiP2pDevices) {
            hosts.add(device.deviceName);
//            tvInfo.append("\n");
//            tvInfo.append(device.deviceName);
//            tvInfo.append("\n");
//            tvInfo.append(device.deviceAddress);
//
//            if (device.deviceName.equals(testDeviceName)) {
////                multiplay.connect(device);
//                break;
//            }
        }
        lvHosts.invalidateViews();
    }

    @Override
    public void onReceiveData(String s) {
        Log.e(TAG, "Data received: " + s);
    }
}
