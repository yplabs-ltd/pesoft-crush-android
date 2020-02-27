package net.pesofts.crush.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import net.pesofts.crush.R;


/**
 * Created by tommykim on 2015. 11. 4..
 */
public class TintedImageButton extends AppCompatImageButton {

    private ColorStateList tint;

    public TintedImageButton(Context context) {
        super(context);
    }

    public TintedImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public TintedImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TintedImageButton, defStyleAttr, 0);
        if (a != null) {
            tint = a.getColorStateList(R.styleable.TintedImageButton_tib_tint);
            a.recycle();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (tint != null && tint.isStateful())
            updateTintColor();
    }

    public void setColorFilter(ColorStateList tint) {
        this.tint = tint;
        super.setColorFilter(tint.getColorForState(getDrawableState(), 0));
    }

    public void setColorFilterResource(int redId) {
        this.tint = ContextCompat.getColorStateList(getContext(), redId);
        super.setColorFilter(tint.getColorForState(getDrawableState(), 0));
    }

    private void updateTintColor() {
        int color = tint.getColorForState(getDrawableState(), 0);
        setColorFilter(color);
    }
}
