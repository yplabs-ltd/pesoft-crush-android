package net.pesofts.crush.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

import net.pesofts.crush.R;
import net.pesofts.crush.Util.BlurUtils;

public class CircularNetworkImageView extends NetworkImageView {
    Context mContext;
    public boolean isEnablePressEffect = true;
    private boolean isBlurEffect = false;

    public void setEnablePressEffect(boolean enablePressEffect) {
        isEnablePressEffect = enablePressEffect;
    }

    public void setBlurEffect(boolean blurEffect) {
        isBlurEffect = blurEffect;
    }

    public CircularNetworkImageView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CircularNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        init();
    }

    public CircularNetworkImageView(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    private void init() {
        setDefaultImageResId(R.drawable.home_thumbnail_default);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm == null) return;

        if (isBlurEffect) {
            BlurUtils.blurProcessing(bm, "", 19, new BlurUtils.BlurListener() {
                @Override
                public void onCompleted(Bitmap blurImage) {
                    setImageDrawable(new BitmapDrawable(mContext.getResources(), getCircularBitmap(blurImage)));
                    postInvalidate();
                }
            });
        } else {
            setImageDrawable(new BitmapDrawable(mContext.getResources(), getCircularBitmap(bm)));
        }
    }

    public Bitmap getCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;

        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int x = (bitmap.getWidth() - size) / 2;
        int y = (bitmap.getHeight() - size) / 2;

        Bitmap squared = Bitmap.createBitmap(bitmap, x, y, size, size);
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        return result;
    }

    @Override
    public void setPressed(boolean pressed) {
        if (isEnablePressEffect) {
            if (pressed) {
                setColorFilter(0x25000000, Mode.SRC_ATOP);
            } else {
                clearColorFilter();
            }
        }
        super.setPressed(pressed);
    }

}