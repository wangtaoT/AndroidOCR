package com.wt.ocr;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.wt.ocr.utils.Utils;


/**
 * 显示截图结果
 * 并识别
 * Created by Administrator on 2016/12/10.
 */

public class ShowCropperedActivity extends AppCompatActivity {

    //sd卡路径
    private static String LANGUAGE_PATH = "";
    //识别语言
    private static final String LANGUAGE = "chi_sim";//chi_sim | eng

    private static final String TAG = "ShowCropperedActivity";
    private ImageView imageView;
    private ImageView imageView2;
    private TextView textView;

    private Uri uri;
    private String result;
    private TessBaseAPI baseApi = new TessBaseAPI();
    private Handler handler = new Handler();
    private ProgressDialog dialog;

    int endWidth, endHeight;
    private ColorMatrix colorMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_croppered);

        LANGUAGE_PATH = getExternalFilesDir("") + "/";
        Log.e("---------", LANGUAGE_PATH);

        Thread myThread = new Thread(runnable);
        dialog = new ProgressDialog(this);
        dialog.setMessage("正在识别...");
        dialog.setCancelable(false);
        dialog.show();

        imageView = (ImageView) findViewById(R.id.image);
        imageView2 = (ImageView) findViewById(R.id.image2);
        textView = (TextView) findViewById(R.id.text);

        int width = getIntent().getIntExtra("width", 0);
        int height = getIntent().getIntExtra("height", 0);
        if (width != 0 && height != 0) {
            int screenWidth = Utils.getWidthInPx(this);
            float scale = (float) screenWidth / (float) width;
            final ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            int imgHeight = (int) (scale * height);
            endWidth = screenWidth;
            endHeight = imgHeight;
            lp.height = imgHeight;
            imageView.setLayoutParams(lp);
            Log.e(TAG, "imageView.getLayoutParams().width:" + imageView.getLayoutParams().width);
        }

        uri = getIntent().getData();
        imageView.setImageURI(uri);

        baseApi.init(LANGUAGE_PATH, LANGUAGE);
        //设置设别模式
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);

        myThread.start();
    }


    /**
     * uri转bitmap
     *
     * @param uri
     * @return
     */
    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            // 读取uri所在的图片
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            Log.e("[Android]", e.getMessage());
            Log.e("[Android]", "目录为：" + uri);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 灰度化处理
     *
     * @param bitmap3
     * @return
     */
    public Bitmap convertGray(Bitmap bitmap3) {
        colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);

        Paint paint = new Paint();
        paint.setColorFilter(filter);
        Bitmap result = Bitmap.createBitmap(bitmap3.getWidth(), bitmap3.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        canvas.drawBitmap(bitmap3, 0, 0, paint);
        return result;
    }

    /**
     * 二值化
     *
     * @param bitmap22
     * @return
     */
    private Bitmap binaryzation(Bitmap bitmap22) {
        //bitmapBina = bitmap22.copy(Config.ARGB_8888, true);
        int width = bitmap22.getWidth();
        int height = bitmap22.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int col = bitmap22.getPixel(i, j);
                int alpha = col & 0xFF000000;
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                //用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);

                if (gray <= 160) {
                    gray = 0;
                } else {
                    gray = 255;
                }
                //新的ARGB
                int newColor = alpha | (gray << 16) | (gray << 8) | gray;

                bitmap22.setPixel(i, j, newColor);
            }
        }
        return bitmap22;
    }

    /**
     * 识别线程
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            baseApi.setImage(convertGray(getBitmapFromUri(uri)));
            result = baseApi.getUTF8Text();
            baseApi.end();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageView2.setImageBitmap(convertGray(getBitmapFromUri(uri)));
                    textView.setText(result);
                    dialog.dismiss();
                }
            });
        }
    };


}
