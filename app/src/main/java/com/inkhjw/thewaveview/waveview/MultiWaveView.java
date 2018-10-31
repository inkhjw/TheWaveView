package com.inkhjw.thewaveview.waveview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.inkhjw.thewaveview.waveview.drawable.WaveAnimationDrawable;

import java.util.ArrayList;

/**
 * @author hjw
 * 多个波浪的效果
 * 是否需要延时执行，可在开启动画的时候判断是否延时(在重叠的情况下不延时执行，无法直观的显示效果)
 */

public class MultiWaveView extends View {
    private ArrayList<WaveDrawable> waveDrawables = new ArrayList<>();

    public MultiWaveView(Context context) {
        super(context);
        init(context, null);
    }

    public MultiWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MultiWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        WaveView.WaveAttribute waveAttribute = new WaveView.WaveAttribute(context, attrs);
        Paint paint = new Paint();
        paint.setColor(waveAttribute.waveColor);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        setDefaultMultiWaveView(waveAttribute, paint);
    }

    public void setDefaultMultiWaveView(WaveView.WaveAttribute waveAttribute, Paint paint) {
        ArrayList<WaveDrawable> defaultWaves = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            WaveAnimationDrawable waveAnimationDrawable = new WaveAnimationDrawable(waveAttribute, paint);
            waveAnimationDrawable.setφ(-1.0 * (i + 1) * (Math.PI / 4));
            defaultWaves.add(waveAnimationDrawable);
        }
        setWaveDrawables(defaultWaves);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int size = waveDrawables.size();
        for (int i = 0; i < size; i++) {
            WaveDrawable w = waveDrawables.get(i);
            if (i == 0) {
                w.setPaintColor(Color.parseColor("#FAE0E0"));
            } else if (i == 1) {
                w.setPaintColor(Color.parseColor("#FBC7C7"));
            }
            w.draw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int defaultWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
        int defaultHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();

        int width = resolveSize(defaultWidth, widthMeasureSpec);
        int height = resolveSize(defaultHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return (waveDrawables.get(0) != null && who == waveDrawables.get(0))
                || super.verifyDrawable(who);
    }

    /**
     * 由于设置View自带的背景可能会造成波浪效果不是和理想，可重写super.setBackground(null);
     * 取消View自带的android:background属性:
     *
     * @param background 背景资源
     */
    @Override
    public void setBackground(Drawable background) {
        //super.setBackground(background);
        super.setBackground(null);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == GONE || visibility == INVISIBLE) {
            stopAnimation();
        } else {
            startAnimation();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateDrawableBounds(w, h);
    }

    /**
     * 使Drawable的宽高与View的宽高相同
     *
     * @param w 实际的宽度
     * @param h 实际的高度
     */
    private void updateDrawableBounds(int w, int h) {
        w -= getPaddingRight() + getPaddingLeft();
        h -= getPaddingTop() + getPaddingBottom();

        int left = 0;
        int top = 0;
        int right = w;
        int bottom = h;

        //改变实际的Drawable的Rect范围
        for (int i = 0; i < waveDrawables.size(); i++) {
            WaveDrawable waveDrawable = waveDrawables.get(i);
            waveDrawable.setBounds(left, top, right, bottom);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateDrawableState();
    }

    private void updateDrawableState() {
        final int[] state = getDrawableState();
        for (int i = 0; i < waveDrawables.size(); i++) {
            WaveDrawable waveDrawable = waveDrawables.get(i);
            if (waveDrawable != null && waveDrawable.isStateful()) {
                waveDrawable.setState(state);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);

        for (int i = 0; i < waveDrawables.size(); i++) {
            WaveDrawable waveDrawable = waveDrawables.get(i);
            if (waveDrawable != null) {
                waveDrawable.setHotspot(x, y);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }


    private int countLayerColor(int paintColor, int position) {
        int red = (paintColor & 0xff0000) >> 16;
        int green = (paintColor & 0x00ff00) >> 8;
        int blue = (paintColor & 0x0000ff);

        int r = red + 16 * position;
        int g = green + 16 * position;
        int b = blue + 16 * position;

        Log.e("debug", "rgb(" + r + "," + g + "," + b + ")");
        return Color.rgb(r, g, b);
    }

    /**
     * 设置WaveDrawable
     *
     * @param drawables
     */
    public void setWaveDrawables(ArrayList<WaveDrawable> drawables) {
        for (int i = 0; i < waveDrawables.size(); i++) {
            WaveDrawable waveDrawable = waveDrawables.get(i);
            if (waveDrawable != null) {
                waveDrawable.setCallback(null);
                unscheduleDrawable(waveDrawable);
            }
        }
        waveDrawables.clear();
        if (null != drawables) {
            waveDrawables.addAll(drawables);
            for (int i = 0; i < drawables.size(); i++) {
                WaveDrawable waveDrawable = waveDrawables.get(i);
                if (waveDrawable != null) {
                    waveDrawable.setCallback(this);
                }
                postInvalidate();
            }
        }
    }

    /**
     * 多视图动画，采用延时开启动画。实现不同高度的波浪效果
     */
    void startAnimation() {
        if (getVisibility() != VISIBLE) {
            return;
        }
        int size = waveDrawables.size();
        for (int i = 0; i < size; i++) {
            final WaveDrawable waveDrawable = waveDrawables.get(i);
            if (waveDrawable != null) {
                //在开启动画后直接移除Runnable
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        waveDrawable.start();
                        removeCallbacks(this);
                    }
                };
                //根据顺序来延时开启动画，index越小的表示在View的最顶层
                long time = (long) ((1.0f * (size - i) / size) * (waveDrawable.getWaveAttribute().animationTotalHeight / waveDrawable.getWaveAttribute().animationSpeed));
                postDelayed(runnable, time);
            }
        }
    }

    void stopAnimation() {
        for (int i = 0; i < waveDrawables.size(); i++) {
            WaveDrawable waveDrawable = waveDrawables.get(i);
            if (waveDrawable != null) {
                waveDrawable.stop();
            }
        }
    }

    public void smoothToShow() {
        startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
        setVisibility(VISIBLE);
    }

    public void smoothToHide() {
        startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
        setVisibility(GONE);
    }

    public void setWaveAttributes(WaveView.WaveAttribute waveAttribute) {
        for (int i = 0; i < waveDrawables.size(); i++) {
            WaveDrawable waveDrawable = waveDrawables.get(i);
            if (waveDrawable != null) {
                waveDrawable.setWaveAttribute(waveAttribute);
            }
        }
    }

    public WaveView.WaveAttribute getWaveAttributes() {
        for (int i = 0; i < waveDrawables.size(); i++) {
            WaveDrawable waveDrawable = waveDrawables.get(i);
            if (waveDrawable != null) {
                return waveDrawable.getWaveAttribute();
            }
        }
        return null;
    }
}
