package com.inkhjw.thewaveview.waveview;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;

import java.util.ArrayList;

/**
 * @author hjw
 */

public abstract class WaveDrawable extends Drawable implements Animatable {
    private Paint mPaint;
    //WaveView的自定义属性
    private WaveView.WaveAttribute waveAttribute;
    //该Drawable的所有动画监听
    ArrayList<ValueAnimator.AnimatorUpdateListener> updateListeners = new ArrayList<>();

    private static final Rect ZERO_BOUNDS_RECT = new Rect();
    protected Rect drawBounds = ZERO_BOUNDS_RECT;

    private ValueAnimator animator;

    public WaveDrawable() {
        waveAttribute = new WaveView.WaveAttribute();
        mPaint = new Paint();
        mPaint.setColor(waveAttribute.waveColor);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public WaveDrawable(WaveView.WaveAttribute waveAttribute, Paint mPaint) {
        this.waveAttribute = waveAttribute;
        this.mPaint = new Paint(mPaint);
    }

    @Override
    public void draw(Canvas canvas) {
        draw(canvas, mPaint);
    }

    public abstract void draw(Canvas canvas, Paint paint);

    public abstract ValueAnimator onCreateAnimator();

    @Override
    public void start() {
        ensureAnimator();
        if (animator == null) {
            throw new NullPointerException("onCreateAnimator return is null");
        }

        // If the animators has not ended, do nothing.
        if (isRunning()) {
            return;
        }
        startAnimators();
        invalidateSelf();
    }

    @Override
    public void stop() {
        if (animator == null) {
            return;
        }
        stopAnimators();
    }

    @Override
    public boolean isRunning() {
        if (animator == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return animator.isStarted() || animator.isRunning();
        }
        return animator.isRunning();
    }

    private void startAnimators() {
        for (int i = 0; i < updateListeners.size(); i++) {
            ValueAnimator.AnimatorUpdateListener updateListener = updateListeners.get(i);
            if (updateListener != null) {
                animator.addUpdateListener(updateListener);
            }
        }
        animator.start();
    }

    private void stopAnimators() {
        if (animator.isRunning()) {
            animator.removeAllUpdateListeners();
            animator.end();
        }
    }

    private void ensureAnimator() {
        if (animator == null) {
            animator = onCreateAnimator();
        }
    }

    /**
     * child View must use this addUpdateListener
     *
     * @param updateListener
     */
    public void addUpdateListener(ValueAnimator.AnimatorUpdateListener updateListener) {
        this.updateListeners.add(updateListener);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        setDrawBounds(bounds);
    }

    public void setDrawBounds(Rect drawBounds) {
        setDrawBounds(drawBounds.left, drawBounds.top, drawBounds.right, drawBounds.bottom);
    }

    public void setDrawBounds(int left, int top, int right, int bottom) {
        this.drawBounds = new Rect(left, top, right, bottom);
    }

    public void setWaveAttribute(WaveView.WaveAttribute waveAttribute) {
        this.waveAttribute = waveAttribute;
    }

    public WaveView.WaveAttribute getWaveAttribute() {
        return waveAttribute;
    }

    @Override
    public int getIntrinsicWidth() {
        return drawBounds.width();
    }

    @Override
    public int getIntrinsicHeight() {
        return drawBounds.height();
    }

    @Override
    public void setAlpha(int alpha) {
        this.mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setPaintColor(@ColorInt int color) {
        mPaint.setColor(color);
        invalidateSelf();
    }

    public int getPaintColor() {
        return mPaint.getColor();
    }
}
