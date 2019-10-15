package com.wt.ocr;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.analytics.Tracker;

/**
 * Created by wangt on 2018/3/2.
 */

public class BaseActivity extends AppCompatActivity {

    private Tracker mTracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //google分析
        App application = (App) getApplication();
        mTracker = application.getDefaultTracker();
    }

    public Tracker getTracker() {
        return mTracker;
    }
}
