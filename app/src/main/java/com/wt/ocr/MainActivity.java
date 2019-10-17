package com.wt.ocr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CAMERA        = 454;
    private static final int PERMISSIONS_REQUEST_WRITE_STORAGE = 455;

    static final String  PERMISSION_CAMERA        = Manifest.permission.CAMERA;
    static final String  PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private      Context context;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        imageView = findViewById(R.id.btn_camera);
        imageView.setOnClickListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                deepFile("tessdata");
            }
        }).start();

        sendScreenImageName();
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_camera) {
            checkSelfPermission();
            //google分析
            getTracker().send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("拍照")
                    .build());
        }
    }

    /**
     * 将assets中的文件复制出
     *
     * @param path
     */
    public void deepFile(String path) {
        String newPath = getExternalFilesDir(null) + "/";
        try {
            String str[] = getAssets().list(path);
            if (str.length > 0) {//如果是目录
                File file = new File(newPath + path);
                file.mkdirs();
                for (String string : str) {
                    path = path + "/" + string;
                    deepFile(path);
                    path = path.substring(0, path.lastIndexOf('/'));//回到原来的path
                }
            } else {//如果是文件
                InputStream is = getAssets().open(path);
                FileOutputStream fos = new FileOutputStream(new File(newPath + path));
                byte[] buffer = new byte[1024];
                while (true) {
                    int len = is.read(buffer);
                    if (len == -1) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //google分析
    private void sendScreenImageName() {
        getTracker().setScreenName("Activity-" + "首页");
        getTracker().send(new HitBuilders.ScreenViewBuilder().build());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CAMERA || requestCode == PERMISSIONS_REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(context, TakePhoteActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(context, "请开启权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 检查权限
     */
    void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(this, PERMISSION_CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST_CAMERA);
        } else if (ContextCompat.checkSelfPermission(this, PERMISSION_WRITE_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION_WRITE_STORAGE}, PERMISSIONS_REQUEST_WRITE_STORAGE);
        } else {
            Intent intent = new Intent(context, TakePhoteActivity.class);
            startActivity(intent);
        }
    }
}
