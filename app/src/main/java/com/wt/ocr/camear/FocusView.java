package com.wt.ocr.camear;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * 聚焦
 * Created by Administrator on 2016/12/8.
 */

public class FocusView extends View {
    private Paint mLinePaint;
    private int mBorderWidth = 4;

    public FocusView(Context context) {
        super(context);
        init();
    }

    public FocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(Color.parseColor("#45ffffff"));
        mLinePaint.setStrokeWidth(mBorderWidth);
        this.setAlpha(0f);  //初始化设置透明
    }

    private void setMainColor() {
        mLinePaint.setColor(Color.parseColor("#52ce90"));
        postInvalidate();
    }

    private void reSet() {
        mLinePaint.setColor(Color.parseColor("#45e0e0e0"));
        postInvalidate();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 2 - mBorderWidth / 2, mLinePaint);
    }

    private AnimatorSet animSet;
    private ObjectAnimator fadeInOut;
    private boolean isFocusing = false;
    public void beginFocus() {
        isFocusing = true;
        if (animSet == null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.3f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.3f, 1f);
            animSet = new AnimatorSet();
            animSet.play(scaleX).with(scaleY);
            animSet.setInterpolator(new LinearInterpolator());
            animSet.setDuration(1000);
            animSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    FocusView.this.setAlpha(1f);
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
                @Override
                public void onAnimationEnd(Animator animation) {
//				FocusView.this.setVisibility(View.GONE);
                    setMainColor();
                    if(fadeInOut == null) {
                        fadeInOut = ObjectAnimator.ofFloat(FocusView.this, "alpha", 1f, 0f);
                        fadeInOut.setDuration(500);
                        fadeInOut.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                            }
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                reSet();
                                isFocusing = false;
                            }
                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }
                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        });
                    }
                    fadeInOut.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
        } else {
            if(animSet.isRunning()) {
                animSet.cancel();
            }
            if(fadeInOut != null && fadeInOut.isRunning()) {
                fadeInOut.cancel();
            }
        }
        animSet.start();
    }

    public boolean isFocusing() {
        return isFocusing;
    }
}
