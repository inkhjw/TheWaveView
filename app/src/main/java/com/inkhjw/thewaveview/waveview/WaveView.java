package com.inkhjw.thewaveview.waveview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.inkhjw.thewaveview.R;
import com.inkhjw.thewaveview.waveview.drawable.WaveAnimationDrawable;

/**
 * @author hjw
 *         单个波浪的效果
 */

public class WaveView extends View {
    private WaveDrawable waveDrawable;

    public WaveView(Context context) {
        super(context);
        init(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WaveView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        WaveAttribute waveAttribute = new WaveAttribute(context, attrs);
        Paint paint = new Paint();
        paint.setColor(waveAttribute.waveColor);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        setWaveDrawable(new WaveAnimationDrawable(waveAttribute, paint));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        waveDrawable.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int defaultWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
        int defaultHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();

        int width = resolveSizeAndState(defaultWidth, widthMeasureSpec, 0);
        int height = resolveSizeAndState(defaultHeight, heightMeasureSpec, 0);
        setMeasuredDimension(width, height);
    }

    public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);
        final int result;
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                } else {
                    result = size;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                result = size;
        }
        return result | (childMeasuredState & MEASURED_STATE_MASK);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == waveDrawable
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
        super.setBackground(background);
        //super.setBackground(null);
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
        waveDrawable.setBounds(left, top, right, bottom);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateDrawableState();
    }

    private void updateDrawableState() {
        final int[] state = getDrawableState();
        if (waveDrawable != null && waveDrawable.isStateful()) {
            waveDrawable.setState(state);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (waveDrawable != null) {
            waveDrawable.setHotspot(x, y);
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

    /**
     * 设置WaveDrawable
     *
     * @param drawable
     */
    public void setWaveDrawable(WaveDrawable drawable) {
        if (waveDrawable != drawable) {
            if (waveDrawable != null) {
                waveDrawable.setCallback(null);
                unscheduleDrawable(waveDrawable);
            }
            waveDrawable = drawable;

            //need to set indicator color again if you didn't specified when you update the indicator .
            if (drawable != null) {
                drawable.setCallback(this);
            }
            postInvalidate();
        }
    }

    void startAnimation() {
        if (getVisibility() != VISIBLE) {
            return;
        }
        waveDrawable.start();
    }

    void stopAnimation() {
        waveDrawable.stop();
    }

    public void smoothToShow() {
        startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
        setVisibility(VISIBLE);
    }

    public void smoothToHide() {
        startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
        setVisibility(GONE);
    }

    public void setWaveAttribute(WaveView.WaveAttribute waveAttribute) {
        this.waveDrawable.setWaveAttribute(waveAttribute);
    }

    public WaveView.WaveAttribute getWaveAttribute() {
        return waveDrawable.getWaveAttribute();
    }

    public static class WaveAttribute {
        static final int DEFAULT_WIDTH = 200;
        static final int DEFAULT_HEIGHT = 10;
        static final int DEFAULT_ANIM_HEIGHT = 30;
        static final float DEFAULT_ANIM_SPEED = 30.0f / 1500.0f;//默认动画高度为30px，时长为1500ms
        static final int DEFAULT_ANIM_COLOR = Color.parseColor("#F69899");

        public int waveWidth;//波浪宽度
        public int waveHeight;//波浪高度
        public int animationTotalHeight;//波浪动画总高度
        public float animationSpeed;//动画的速度
        public int waveColor;//波浪颜色

        public WaveAttribute() {
            this.waveWidth = DEFAULT_WIDTH;
            this.waveHeight = DEFAULT_HEIGHT;
            this.animationTotalHeight = DEFAULT_ANIM_HEIGHT;
            this.animationSpeed = DEFAULT_ANIM_SPEED;
            this.waveColor = DEFAULT_ANIM_COLOR;
        }

        public WaveAttribute(int waveWidth, int waveHeight, int animationTotalHeight,
                             float animationSpeed, int waveColor) {
            this.waveWidth = waveWidth;
            this.waveHeight = waveHeight;
            this.animationTotalHeight = animationTotalHeight;
            this.animationSpeed = animationSpeed;
            this.waveColor = waveColor;
        }

        public WaveAttribute(WaveAttribute source) {
            this.waveWidth = source.waveWidth;
            this.waveHeight = source.waveHeight;
            this.animationTotalHeight = source.animationTotalHeight;
            this.animationSpeed = source.animationSpeed;
            this.waveColor = source.waveColor;
        }

        public WaveAttribute(Context c, AttributeSet attrs) {
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.SpecialWaveView);
            setBaseAttributes(a);
            a.recycle();
        }

        protected void setBaseAttributes(TypedArray a) {
            this.waveWidth = a.getInt(R.styleable.SpecialWaveView_waveWidth, DEFAULT_WIDTH);
            this.waveHeight = a.getInt(R.styleable.SpecialWaveView_waveHeight, DEFAULT_HEIGHT);
            this.animationTotalHeight = a.getInt(R.styleable.SpecialWaveView_anim_totalHeight, DEFAULT_ANIM_HEIGHT);
            this.animationSpeed = a.getFloat(R.styleable.SpecialWaveView_anim_speed, DEFAULT_ANIM_SPEED);
            this.waveColor = a.getColor(R.styleable.SpecialWaveView_anim_color, DEFAULT_ANIM_COLOR);
        }
    }
}
