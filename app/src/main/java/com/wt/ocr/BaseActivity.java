package com.wt.ocr;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

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
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
    }

    public Tracker getTracker() {
        return mTracker;
    }
}
