package net.pesofts.crush.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import net.pesofts.crush.R;

/**
 * Created by erkas on 2017. 2. 23..
 */

public class VoicePlaySmallView extends ImageView {

    private ObjectAnimator animator;

    public interface VoicePlayListener {
        void onPlay();
        void onStop();
    }

    private static final int STATE_PLAY = 0;
    private static final int STATE_STOP = 1;
    private int mState = STATE_STOP;

    private VoicePlayListener voicePlayListener;
    private Paint mBgPaint;
    private Paint mProgressPaint;

    private float mProgressOffset = 0.f;
    private Path drawPath;
    private RectF arcBounds;

    private int progressBgColor;
    private int progressColor;

    private float px1dp;

    public VoicePlaySmallView(Context context) {
        super(context);
        init(context, null);
    }

    public VoicePlaySmallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public VoicePlaySmallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VoicePlaySmallView);

        int backgoundColor = ta.getColor(R.styleable.VoicePlaySmallView_vp_background, 0x4D000000);

        ta.recycle();

        px1dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.f, getResources().getDisplayMetrics());

        progressBgColor = ResourcesCompat.getColor(getResources(), R.color.story_progress_bg, null);
        progressColor = ResourcesCompat.getColor(getResources(), R.color.story_progress, null);

        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(backgoundColor);

        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(px1dp);

        drawPath = new Path();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (mState) {
                    case STATE_STOP:
                        mState = STATE_PLAY;

                        setImageResource(R.drawable.ico_stop_small);
                        if (voicePlayListener != null) {
                            voicePlayListener.onPlay();
                        }
                        break;
                    case STATE_PLAY:
                        mState = STATE_STOP;

                        setImageResource(R.drawable.ico_play_small);
                        stopVoicePlayProgress();
                        break;
                }
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        arcBounds = new RectF(0, 0, w, h);
        arcBounds.inset(px1dp, px1dp);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        float centerX = arcBounds.centerX();
        float centerY = arcBounds.centerY();

        if (mState == STATE_STOP) {
            canvas.drawCircle(centerX +1f, centerY +1.f, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34.f, getResources().getDisplayMetrics()), mBgPaint);
        }

        mProgressPaint.setColor(progressBgColor);
        canvas.drawCircle(centerX, centerY, arcBounds.centerX() - px1dp, mProgressPaint);

        if (mProgressOffset > 0) {
            Point startPoint = pointOnCircle(width / 2 - px1dp, 0.f, centerX, centerY);

            drawPath.reset();
            drawPath.arcTo(arcBounds, -90.f, 360.f * mProgressOffset);
            drawPath.moveTo(startPoint.x, startPoint.y);
            drawPath.close();

            mProgressPaint.setColor(progressColor);
            canvas.drawPath(drawPath, mProgressPaint);
        }

        super.onDraw(canvas);
    }

    /**
     * 3시 방향이 0도, 12시 방향이 90도
     */
    private Point pointOnCircle(float radius, float angleInDegrees, float centerX, float centerY) {
        // Convert from degrees to radians via multiplication by PI/180
        int x = (int)(radius * Math.cos(angleInDegrees * Math.PI / 180F) + centerX);
        int y = (int)(radius * Math.sin(angleInDegrees * Math.PI / 180F) + centerY);

        return new Point(x, y);
    }

    public void setProgress(float progress) {
        if (progress > 1.f) {
            progress -= 1.f;
        }
        this.mProgressOffset = progress;
        postInvalidateOnAnimation();
    }

    public void setVoicePlayListener(VoicePlayListener voicePlayListener) {
        this.voicePlayListener = voicePlayListener;
    }

    public void startVoicePlayProgress(long duration) {
        animator = ObjectAnimator.ofFloat(this, "progress", 0.f, 1.f);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mState = STATE_STOP;
                setImageResource(R.drawable.ico_play_small);
                if (voicePlayListener != null) {
                    voicePlayListener.onStop();
                }

                postInvalidateOnAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setProgress(0.f);
            }
        });

        animator.start();
    }

    public void stopVoicePlayProgress() {
        setProgress(0.f);
        if (animator != null) {
            animator.cancel();
            animator = null;
        } else {
            mState = STATE_STOP;
            setImageResource(R.drawable.ico_play_small);
            postInvalidate();
        }
    }
}
