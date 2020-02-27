package net.pesofts.crush.fragment;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import net.pesofts.crush.R;
import net.pesofts.crush.Util.ImageUtil;
import net.pesofts.crush.activity.ProfileActivity;
import net.pesofts.crush.widget.ImageCropView;
import net.pesofts.crush.widget.SizeChangedListenerImageView;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImageCropDialogFragment extends DialogFragment {

    protected String filePath;

    @Bind(R.id.originalImageView)
    SizeChangedListenerImageView originalImageView;

    @Bind(R.id.cropImageView)
    ImageCropView cropImageView;

    @Bind(R.id.rotateButton)
    Button rotateButton;

    private float loadedImageWidth = 0.0f;
    private float loadedImageHeight = 0.0f;

    private boolean didSetMainLayoutSize = false;

    protected ConfirmCropListener confirmCropListener;

    public void setConfirmListener(final ConfirmCropListener confirmCropListener) {
        this.confirmCropListener = confirmCropListener;
    }

    public interface ConfirmCropListener {
        void onDialogConfirmed(String thumbnailPath);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public static ImageCropDialogFragment newInstance() {
        Bundle args = new Bundle();

        ImageCropDialogFragment fragment = new ImageCropDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainLayout = inflater.inflate(
                R.layout.image_crop_dialog_fragment, container, false);

        ButterKnife.bind(this, mainLayout);

        return mainLayout;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
//		getDialog().getWindow().setWindowAnimations(R.style.MenuDialogAnimation);

        setUpViews();
    }

    void setUpViews() {

        originalImageView.setOnSizeChangedListener(new SizeChangedListenerImageView.OnSizeChangedListener() {

            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                // 이미지로딩보다 onSizeChanged가 늦게 불렷을때 크롭뷰 사이즈 조정
                if (loadedImageWidth > 0 && loadedImageHeight > 0 && !didSetMainLayoutSize) {
                    setCropImageViewSize();
                }
            }
        });

        File imgFile = new File(filePath);
        if (imgFile.exists()) {
            Bitmap myBitmap = ImageUtil.getResizedBitmapFromFile(imgFile, ProfileActivity.ATTACHED_IMAGE_WIDTH);
            if (myBitmap != null) {
                loadedImageWidth = myBitmap.getWidth();
                loadedImageHeight = myBitmap.getHeight();

                setCropImageViewSize();
                originalImageView.setImageBitmap(myBitmap);
            }
        }

    }

    /**
     * 크롭뷰의 크기를 이미지의 크기와 동일하게 맞춰주기 위한 작업.
     */
    void setCropImageViewSize() {
        float viewHeight = originalImageView.getHeight();
        float viewWidth = originalImageView.getWidth();

//		logger.debug("loadedImageWidth : {}, loadedImageHeight : {} , viewWidth : {}, viewHeight : {}",
//		        loadedImageWidth, loadedImageHeight, viewWidth, viewHeight);

        if (viewWidth > 0 && viewHeight > 0) {
            int cropImageViewWidth = (int) viewWidth;
            int cropImageViewHeight = (int) viewHeight;

            float bitmapHeightRatio = loadedImageHeight / loadedImageWidth;
            float bitmapWidthRatio = loadedImageWidth / loadedImageHeight;
            float viewHeightRatio = viewHeight / viewWidth;
            if (bitmapHeightRatio > viewHeightRatio) {
                cropImageView.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
                cropImageView.getLayoutParams().width = (int) (viewHeight * bitmapWidthRatio);
                cropImageViewWidth = (int) (viewHeight * bitmapWidthRatio);
            } else {
                cropImageView.getLayoutParams().height = (int) (viewWidth * bitmapHeightRatio);
                cropImageView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
                cropImageViewHeight = (int) (viewWidth * bitmapHeightRatio);
            }

            cropImageView.initCropRect(cropImageViewWidth, cropImageViewHeight);
            cropImageView.setVisibility(View.VISIBLE);

            didSetMainLayoutSize = true;
        }
    }

    @OnClick(R.id.confirmButton)
    void confirmButtonClicked() {
        Bitmap croppedBitmap = getCroppedBitmap();
        if (croppedBitmap != null) {
            try {
                File tempThumbnailFile = ImageUtil.createTempImageFileForProfile();
                File imageFile = ImageUtil.bitmapToFile(croppedBitmap, tempThumbnailFile);
                if (imageFile != null) {
                    confirmCropListener.onDialogConfirmed(null);
                }
                dismiss();
            } catch (Exception e) {
//				LogUtil.w("getTempUri fail : " + e.getMessage());
            }

        }
    }

    @OnClick(R.id.cancelButton)
    void cancelButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.rotateButton)
    void rotateButtonClicked() {

        if (originalImageView == null || originalImageView.getDrawable() == null) {
            return;
        }

        Bitmap bitmap = ((BitmapDrawable) originalImageView.getDrawable()).getBitmap();
        if (bitmap != null) {
            Bitmap rotateBitmap = ImageUtil.rotate(bitmap, 90);

            if (bitmap != rotateBitmap) {
                originalImageView.setImageBitmap(rotateBitmap);
                loadedImageWidth = rotateBitmap.getWidth();
                loadedImageHeight = rotateBitmap.getHeight();
                setCropImageViewSize();
            }
        }
    }

    private Bitmap getCroppedBitmap() {
        Bitmap croppedBitmap = null;
        try {
            Bitmap bitmap = ((BitmapDrawable) originalImageView.getDrawable()).getBitmap();
            final RectF displayedImageRect = cropImageView.getScrapRect();

            // Get the scale factor between the actual Bitmap dimensions and the
            // displayed dimensions for width.
            final float actualImageWidth = bitmap.getWidth();
            final float displayedImageWidth = cropImageView.getWidth();
            final float scaleFactorWidth = actualImageWidth / displayedImageWidth;

            // Get the scale factor between the actual Bitmap dimensions and the
            // displayed dimensions for height.
            final float actualImageHeight = bitmap.getHeight();
            final float displayedImageHeight = cropImageView.getHeight();
            final float scaleFactorHeight = actualImageHeight / displayedImageHeight;

            // Get crop window position relative to the displayed image.
            final float cropWindowX = displayedImageRect.left;
            final float cropWindowY = displayedImageRect.top;
            final float cropWindowWidth = displayedImageRect.width();
            final float cropWindowHeight = displayedImageRect.height();

            // Scale the crop window position to the actual size of the Bitmap.
            final float actualCropX = cropWindowX * scaleFactorWidth;
            final float actualCropY = cropWindowY * scaleFactorHeight;
            final float actualCropWidth = cropWindowWidth * scaleFactorWidth;
            final float actualCropHeight = cropWindowHeight * scaleFactorHeight;

            // Crop the subset from the original Bitmap.
            croppedBitmap = Bitmap.createBitmap(bitmap, (int) actualCropX, (int) actualCropY, (int) actualCropWidth,
                    (int) actualCropHeight);
        } catch (Exception e) {
//			logger.warn("getCroppedBitmap - Exception", e);
        } catch (OutOfMemoryError e) {
//			logger.warn("getCroppedBitmap - OutOfMemoryError", e);
        }

        return croppedBitmap;
    }

}
