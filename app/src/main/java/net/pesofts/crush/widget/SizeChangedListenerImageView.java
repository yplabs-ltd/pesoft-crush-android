package net.pesofts.crush.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SizeChangedListenerImageView extends ImageView {
//	private static final Logger logger = LoggerFactory.getLogger(SizeChangedListenerImageView.class);

    private OnSizeChangedListener onSizeChangedListener;

    public SizeChangedListenerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
        this.onSizeChangedListener = onSizeChangedListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//		logger.debug("onSizeChanged - width : {}, height : {}", w, h);

        if (onSizeChangedListener != null) {
            onSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }

    public interface OnSizeChangedListener {
        public void onSizeChanged(int w, int h, int oldw, int oldh);
    }

}
