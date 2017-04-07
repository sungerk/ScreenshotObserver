package org.net.sunger;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.net.sunger.demo.R;


public class MainActivity extends AppCompatActivity {
    private ScreenshotContentObserver screenshotContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        screenshotContentObserver = new ScreenshotContentObserver(this);
        screenshotContentObserver.startObserve(new ScreenshotContentObserver.ICallBack() {
            @Override
            public void onScreenShot(String path) {
                Toast.makeText(MainActivity.this, "截图" + path, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        screenshotContentObserver.stopObserve();
    }
}
