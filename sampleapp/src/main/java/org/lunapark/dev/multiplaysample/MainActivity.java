package org.lunapark.dev.multiplaysample;

import android.app.Activity;
import android.os.Bundle;

import org.lunapark.dev.multiplay.Multiplay;

public class MainActivity extends Activity {

    private Multiplay multiplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        multiplay = new Multiplay(this);

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
}
