package com.wjdtncjs.ass.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wjdtncjs.ass.MyApplcation;
import com.wjdtncjs.ass.R;

public class DisableScreenshotActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove);
        MyApplcation.getInstance().allowUserSaveScreenshot(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplcation.getInstance().registerScreenshotObserver();

    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplcation.getInstance().unregisterScreenshotObserver();
    }
}
