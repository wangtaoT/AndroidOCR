package com.wt.ocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.wt.ocr.databinding.ActivityCutoutPhoteBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 裁剪
 */
public class CutOutPhotoActivity extends BaseActivity {

    private ActivityCutoutPhoteBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_cutout_phote);
        String path = getIntent().getStringExtra("path");
        Bitmap bitmap = BitmapFactory.decodeFile(path);

        mBinding.cropImageView.setImageBitmap(bitmap);

        mBinding.btnClosecropper.setOnClickListener(view -> finish());

        mBinding.btnStartcropper.setOnClickListener(view -> {
            Bitmap cropperBitmap = mBinding.cropImageView.getCroppedImage();
            // 图像名称
            String path1 = getCacheDir().getPath();
            String resultPath = saveImage(path1, cropperBitmap);

            Intent intent = new Intent(CutOutPhotoActivity.this, ShowCropperedActivity.class);
            intent.putExtra("path", resultPath);
            intent.putExtra("width", cropperBitmap.getWidth());
            intent.putExtra("height", cropperBitmap.getHeight());
            startActivity(intent);
            cropperBitmap.recycle();
            finish();
        });
    }

    /**
     * 存储图像
     */
    private String saveImage(String path, Bitmap source) {
        OutputStream outputStream = null;
        File file;
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new File(path, System.currentTimeMillis() + ".jpg");
            if (file.createNewFile()) {
                outputStream = new FileOutputStream(file);
                if (source != null) {
                    source.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                }
            }
        } catch (IOException e) {
            return "";
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable t) {
                }
            }
        }

        return file.getPath();
    }
}
