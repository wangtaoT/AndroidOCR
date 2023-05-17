package com.wt.ocr;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;

/**
 * Created by wangt on 2018/3/2.
 */

public class App extends Application implements CameraXConfig.Provider {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }
}
