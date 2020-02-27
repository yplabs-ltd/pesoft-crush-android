package net.pesofts.crush.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import net.pesofts.crush.R;

/**
 * Created by erkas on 2017. 3. 14..
 */

public class BounceBgView extends View {
    private static final long LIFETIME_DEAFULT = 4500;
    private int mStartRadius;
    private int mBounceColor;
    private Paint mPaint;
    private float animateOffset;
    private ObjectAnimator animator;

    public BounceBgView(Context context) {
        super(context);
        init(context, null);
    }

    public BounceBgView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BounceBgView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public BounceBgView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BounceBgView);
            if (ta != null) {
                mStartRadius = ta.getDimensionPixelSize(R.styleable.BounceBgView_start_width, 100);
                mBounceColor = ta.getColor(R.styleable.BounceBgView_bounce_color, 0xffffffff);
                ta.recycle();
            }
        }

        mPaint = new Paint();
        mPaint.setColor(mBounceColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.f, getResources().getDisplayMetrics()));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int centerX = width / 2;
        int centerY = height / 2;

        mPaint.setColor(mBounceColor);
        canvas.drawCircle(width / 2, height / 2, mStartRadius, mPaint);

        float offset;
        if (animateOffset <= 3.f) {
            offset = animateOffset / 3.f;
            drawBounce(canvas, offset, centerX, centerY);
        }

        if (animateOffset >= 1.f && animateOffset <= 4.f) {
            offset = (animateOffset - 1.f) / 3.f;
            drawBounce(canvas, offset, centerX, centerY);
        }

        if (animateOffset >= 2.f && animateOffset <= 5.f) {
            offset = (animateOffset - 2.f) / 3.f;
            drawBounce(canvas, offset, centerX, centerY);
        }
    }

    private void drawBounce(Canvas canvas, float offset, int centerX, int centerY) {
        int endRadius;
        if (centerX > centerY) {
            endRadius = centerY;
        } else {
            endRadius = centerX;
        }

        float radius = mStartRadius + (endRadius - mStartRadius) * offset;
        int alpha = (int) (0xff * (1.f - offset));
        int color = Color.argb(alpha, Color.red(mBounceColor), Color.green(mBounceColor), Color.blue(mBounceColor));
        mPaint.setColor(color);

        canvas.drawCircle(centerX, centerY, radius, mPaint);
    }

    public void setAnimateOffset(float animateOffset) {
        this.animateOffset = animateOffset;
        postInvalidateOnAnimation();
    }

    public void startBounceAnimation() {
        animator = ObjectAnimator.ofFloat(this, "animateOffset", 0.f, 5.f);

        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setDuration(LIFETIME_DEAFULT);

        animator.start();
    }

    public void stopBounceAnimation() {
        if (animator != null) {
            animator.cancel();
        }
    }
}
