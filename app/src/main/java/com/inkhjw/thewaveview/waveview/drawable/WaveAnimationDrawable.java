package com.inkhjw.thewaveview.waveview.drawable;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import com.inkhjw.thewaveview.waveview.WaveDrawable;
import com.inkhjw.thewaveview.waveview.WaveView;

/**
 * @author hjw
 */

public class WaveAnimationDrawable extends WaveDrawable {
    private RectF waveRect = new RectF();
    private RectF backgroundRect = new RectF();

    private int curWaveAnimationHeight = 0;//此次波浪动画高度
    double φ;

    public WaveAnimationDrawable() {
        super();
    }

    public WaveAnimationDrawable(WaveView.WaveAttribute waveAttribute, Paint mPaint) {
        super(waveAttribute, mPaint);
    }

    /**
     * 设置正玄曲线沿X轴的最小正周期
     *
     * @param φ
     */
    public void setφ(double φ) {
        if (φ == 0) {
            this.φ = Math.PI / 2;
            return;
        }
        this.φ = φ;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        drawSinWave(canvas, paint);//先画正玄曲线
        drawBackGround(canvas, paint);//在画背景
    }

    /**
     * 正玄函数：y=Asin(ωx+φ)+h
     * A:决定峰值(即纵向拉伸压缩的倍数)
     * ω:决定周期(最小正周期T=2π/|ω|)
     * φ(初相位):决定波形与X轴位置关系或横向移动距离(左加右减)
     * h:表示波形在Y轴的位置关系或纵向移动距离(上加下减)
     */
    private void drawSinWave(Canvas canvas, Paint paint) {
        //跟随动画变化
        int curWaveTopY = getWaveAttribute().waveHeight - curWaveAnimationHeight;

        double a = getWaveAttribute().waveHeight;
        double ω = 2 * Math.PI / getWaveAttribute().waveWidth;
        double h = 0;

        Path path = new Path();
        PointF point = new PointF();
        float lineY = 0;
        //下面计算点的y值，由于Android坐标系的问题，显示在屏幕上的y都是正值。
        //当y为负数时，则无法显示在屏幕上，我们可以设定一个阈值，令waveTopY>=y的最大值
        //即：waveTopY>=waveHeightIncrease * minWaveHeight
        //最合理的为：waveTopY=waveHeightIncrease * minWaveHeight;
        int width = getIntrinsicWidth();
        for (int x = 0; x <= width; x += 1) {
            //正玄值
            float y = (float) ((a * Math.sin(ω * x + φ)) + h);
            //实际的x坐标
            point.x = x;
            //实际的y坐标
            point.y = y + getWaveAttribute().animationTotalHeight + curWaveTopY;

            if (x == 0) {
                lineY = point.y;
                //path的初始点
                path.moveTo(point.x, lineY + 2 * (getWaveAttribute().animationTotalHeight + curWaveTopY));
            }
            if (x == width) {
                //path的闭合点
                path.lineTo(point.x, lineY + 2 * (getWaveAttribute().animationTotalHeight + curWaveTopY));
                path.close();
                break;
            }
            //canvas.drawPoint(point.x, point.y, paint);
            path.lineTo(point.x, point.y);
        }
        canvas.drawPath(path, paint);
    }

    /**
     * 注意：由于下面画正玄波浪时，将波浪最大值Y向下移动了waveHeight 的距离
     * 所以，此处需要流出2倍正玄波浪的空间
     *
     * @param canvas
     */
    private void drawBackGround(Canvas canvas, Paint paint) {
        backgroundRect.left = 0;
        backgroundRect.top = 2 * getWaveAttribute().waveHeight + getWaveAttribute().animationTotalHeight - curWaveAnimationHeight;
        backgroundRect.right = getIntrinsicWidth();
        backgroundRect.bottom = getWaveAttribute().waveHeight + getIntrinsicHeight();

        canvas.drawRect(backgroundRect, paint);
    }

    @Override
    public ValueAnimator onCreateAnimator() {
        ValueAnimator animator = ValueAnimator.ofInt(0, getWaveAttribute().animationTotalHeight);
        animator.setDuration((long) (getWaveAttribute().animationTotalHeight / getWaveAttribute().animationSpeed));
        animator.setRepeatCount(-1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                curWaveAnimationHeight = (int) animation.getAnimatedValue();
                invalidateSelf();
            }
        });
        return animator;
    }
}
