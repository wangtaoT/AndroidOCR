package com.wt.ocr;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.common.util.concurrent.ListenableFuture;
import com.wt.ocr.databinding.ActivityTakePhoteBinding;
import com.wt.ocr.utils.Utils;

import java.io.File;

/**
 * 拍照界面
 * Created by Administrator on 2016/12/8.
 */
public class TakePhoteActivity extends AppCompatActivity {

    public static final boolean isTransverse = true;

    private static final String TAG = "TakePhoteActivity";
    public static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private ImageCapture imageCapture;

    private ActivityTakePhoteBinding mBinding;

    /**
     * 旋转文字
     */
    private boolean isRotated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_take_phote);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mBinding.btnClose.setOnClickListener(onClickListener);
        mBinding.btnShutter.setOnClickListener(onClickListener);
        mBinding.btnAlbum.setOnClickListener(onClickListener);

        startCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTransverse) {
            if (!isRotated) {
                TextView tvHint = findViewById(R.id.hint);
                ObjectAnimator animator = ObjectAnimator.ofFloat(tvHint, "rotation", 0f, 90f);
                animator.setStartDelay(800);
                animator.setDuration(500);
                animator.setInterpolator(new LinearInterpolator());
                animator.start();

                ImageView btnShutter = findViewById(R.id.btn_shutter);
                ObjectAnimator animator1 = ObjectAnimator.ofFloat(btnShutter, "rotation", 0f, 90f);
                animator1.setStartDelay(800);
                animator1.setDuration(500);
                animator1.setInterpolator(new LinearInterpolator());
                animator1.start();

                ObjectAnimator animator3 = ObjectAnimator.ofFloat(mBinding.btnAlbum, "rotation", 0f, 90f);
                animator3.setStartDelay(800);
                animator3.setDuration(500);
                animator3.setInterpolator(new LinearInterpolator());
                animator3.start();
                isRotated = true;
            }
        }
    }

    /**
     * 开始预览
     */
    private void startCamera() {
        // 将Camera的生命周期和Activity绑定在一起（设定生命周期所有者），这样就不用手动控制相机的启动和关闭。
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // 将你的相机和当前生命周期的所有者绑定所需的对象
                ProcessCameraProvider processCameraProvider = cameraProviderFuture.get();

                // 创建一个Preview 实例，并设置该实例的 surface 提供者（provider）。
                Preview preview = new Preview.Builder()
                        .setTargetRotation(Surface.ROTATION_90)
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();
                preview.setSurfaceProvider(mBinding.preview.getSurfaceProvider());

                // 选择后置摄像头作为默认摄像头
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // 创建拍照所需的实例
                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(Surface.ROTATION_90)
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();

                // 重新绑定用例前先解绑
                processCameraProvider.unbindAll();

                // 绑定用例至相机
                processCameraProvider.bindToLifecycle(this, cameraSelector,
                        preview,
                        imageCapture);

            } catch (Exception e) {
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture != null) {
            // 创建带时间戳的输出文件以保存图片，带时间戳是为了保证文件名唯一
            File photoFile = new File(getCacheDir(), "/" + System.currentTimeMillis() + ".jpg");

            // 创建 output option 对象，用以指定照片的输出方式
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions
                    .Builder(photoFile)
                    .build();

            // 执行takePicture（拍照）方法
            imageCapture.takePicture(outputFileOptions,
                    ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {// 保存照片时的回调
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Uri savedUri = Uri.fromFile(photoFile);
                            launchActivity(savedUri);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                        }
                    });
        }
    }

    /**
     * 拍照界面
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_close: //关闭相机
                    finish();
                    break;
                case R.id.btn_shutter: //拍照
                    takePhoto();
                    break;
                case R.id.btn_album: //相册
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, 1);
                    break;
            }
        }
    };

    /**
     * 获取图片回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.e("uri", uri.toString());
            launchActivity(uri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void launchActivity(Uri uri) {
        String path = Utils.getFilePathByUri(this, uri);
        Intent intent = new Intent(this, CutOutPhotoActivity.class);
        intent.putExtra("path", path);
        startActivity(intent);
    }

}
