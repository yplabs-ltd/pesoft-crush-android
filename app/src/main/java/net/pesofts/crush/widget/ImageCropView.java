package net.pesofts.crush.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.pesofts.crush.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageCropView extends RelativeLayout {
    /**
     * 기본 크기 조정 화살표 마진 dp
     */
    private static final int DEFAULT_ARROW_MARGIN = 11;
    /**
     * 크롭 경계선에서 얼마나 떨어진 영역을 선부분을 클릭한것으로 인지할지 필요한 크기
     */
    private static final int EDGE_OFFSET = 50;

    private static final int MIN_RECT_SIZE = 500;

    /**
     * 딤드된 부분과 크롭 영역을 포함한 레이아웃
     */
    @Bind(R.id.cropLayout)
    protected RelativeLayout cropLayout;
    /**
     * 위쪽 딤드 영역 : 이 뷰의 높이로 크롭 y 좌표가 결정된다.
     */
    @Bind(R.id.topDimmedView)
    protected View topDimmedView;
    /**
     * 왼쪽 딤드 영역 : 이 뷰의 넓이로 x좌표가 결정된다.
     */
    @Bind(R.id.leftDimmedView)
    protected View leftDimmedView;
    /**
     * 크롭 영역
     */
    @Bind(R.id.cropView)
    protected View cropView;

    @Bind(R.id.leftTopArrowImageView)
    protected ImageView leftTopArrowImageView;

    @Bind(R.id.rightTopArrowImageView)
    protected ImageView rightTopArrowImageView;

    @Bind(R.id.leftBottomArrowImageView)
    protected ImageView leftBottomArrowImageView;

    @Bind(R.id.rightBottomArrowImageView)
    protected ImageView rightBottomArrowImageView;

    /**
     * 제스쳐 ACTION_MOVE가 어떤 상태에서 일어나는지 알기 위한 타입
     */
    enum MoveType {
        /**
         * 크롭에 영향을 미치지 않는 상태
         */
        none,
        /**
         * 크롭 영역을 이동하는 상태
         */
        drag,
        /**
         * 아래쪽 경계선을 움직여서 크기를 조정하게 되는 상태
         */
        resize_bottom,
        /**
         * 왼쪽 경계선을 움직여서 크기를 조정하게 되는 상태
         */
        resize_left,
        /**
         * 오른쪽 경계선을 움직여서 크기를 조정하게 되는 상태
         */
        resize_right,
        /**
         * 위쪽 경계선을 움직여서 크기를 조정하게 되는 상태
         */
        resize_top
    }

    ;

    /**
     * 제스쳐 ACTION_MOVE 상태
     */
    private MoveType moveType = MoveType.none;
    /**
     * 마지막 터치된 X좌표
     */
    private float mLastMotionX;
    /**
     * 마지막 터치된 Y좌표
     */
    private float mLastMotionY;

    /**
     * 크롭 영역
     */
    private RectF cropRect = new RectF();
    /**
     * 영역연산을 위해 사용하는 임시 객체
     */
    private RectF tempRect = new RectF();

    /**
     * 크롭 최소 높이
     */
    private int minContentViewHeight = 0;
    /**
     * 크롭 최소 넓이
     */
    private int minContentViewWidth = 0;

    private boolean resizeEnable = true;

    /**
     * 크롭 크기 조정 화살표가 크롭 경계선에서 표시할 마진 (하드코딩으로 사이즈를 설정되어 있다.)
     */
    private int arrowMargin;

    public ImageCropView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ImageCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageCropView(Context context) {
        super(context);
        init();
    }

    public void init() {
        inflate(getContext(), R.layout.image_crop_view, this);

        ButterKnife.bind(this);
        /**
         * 크기 조정 화살표 마진 dp를 픽셀로 변환한다.
         */
        arrowMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_ARROW_MARGIN, getContext()
                .getResources().getDisplayMetrics());
    }

    /**
     * 뷰가 재배치되는 경우 : 크기 조정 화살표를 위치시키고, 크롭 영역을 저장한다.
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        layoutArrowImageView();

        cropRect.left = cropView.getLeft();
        cropRect.top = cropView.getTop();
        cropRect.right = cropView.getRight();
        cropRect.bottom = cropView.getBottom();
//        logger.info("onLayout({}, {}, {}, {}, {}), cropRect : {}, orientation = {}", changed, l, t, r, b,
//                ReflectionToStringBuilder.toString(cropRect), getResources().getConfiguration().orientation);
    }

    /**
     * 크롭 영역을 dx, dy만큼 조정한다. 중심을 기준으로 모든 방향에서 적용되기 때문에 나중 크기는 dx*2, dy*2만큼 적용된다.
     *
     * @param dx +라면 크롭 가로가 커지고, -라면 가로가 작아진다.
     * @param dy +라면 크롭 세로가 커지고, -라면 세로가 작아진다.
     */
    private void resizeCropViewBy(int dx, int dy) {
        int oldWidth = (int) this.cropRect.width();
        int oldHeight = (int) this.cropRect.height();
        tempRect.set(this.cropRect);
        tempRect.inset(-dx, -dy);
        Rect adjustRect = adjustCropViewRect(tempRect, cropLayout.getWidth(), cropLayout.getHeight());
        if (adjustRect.width() != oldWidth && adjustRect.height() != oldHeight) {
            layoutCropView(adjustRect);
        }
    }

    /**
     * 주어진 부모 크기에 맞추어 크롭 영역을 표현한다. 크롭 영역이 부모크기를 벗어날 경우 재조정된다.
     *
     * @param cropRect     크롭 영역
     * @param parentWidth  부모 가로 (전체 크롭 가능한 가로 길이)
     * @param parentHeight 부모 세로 (전체 크롭 가능한 세로 길이)
     */
    private void layoutCropView(RectF cropRect, int parentWidth, int parentHeight) {
        Rect adjustRect = adjustCropViewRect(cropRect, parentWidth, parentHeight);
        layoutCropView(adjustRect);
    }

    /**
     * 주어진 영역으로 크롭 뷰를 위치시킨다. 영역은 반드시 정상적으로 표현할 수 있는 위치여야 하기 때문에
     * adjustScrapViewRect로 수정된 영역을 사용해야 한다.
     *
     * @param adjustRect
     */
    private void layoutCropView(Rect adjustRect) {
        // 위쪽 딤드
        RelativeLayout.LayoutParams topParams = (RelativeLayout.LayoutParams) topDimmedView.getLayoutParams();
        topParams.height = adjustRect.top;
        topDimmedView.setLayoutParams(topParams);

        // 왼쪽 딤드
        RelativeLayout.LayoutParams leftParams = (RelativeLayout.LayoutParams) leftDimmedView.getLayoutParams();
        leftParams.width = adjustRect.left;
        leftDimmedView.setLayoutParams(leftParams);

        // 크롭 영역. 나머지 딤드는 레이아웃으로 자동 구성된다.
        RelativeLayout.LayoutParams cropParam = (RelativeLayout.LayoutParams) cropView.getLayoutParams();
        cropParam.width = adjustRect.width();
        cropParam.height = adjustRect.height();
        cropView.setLayoutParams(cropParam);
    }

    /**
     * 주어진 크롭 영역을 크기를 넘지않도록 수정된 영역을 반환한다.
     *
     * @param cropRect     크롭 영역
     * @param parentWidth  최대 가로 크기
     * @param parentHeight 최대 세로 크기
     * @return 영역에 맞도록 수정된 크롭 영역
     */
    private Rect adjustCropViewRect(RectF cropRect, int parentWidth, int parentHeight) {
        int width = (int) cropRect.width();
        int height = (int) cropRect.height();
        float parentViewRatio = parentWidth / (float) parentHeight;
        float contentViewRatio = minContentViewWidth / (float) minContentViewHeight;

        int scrapWidth;
        int scrapHeight;
        if (parentViewRatio < contentViewRatio) {
            scrapWidth = minMax(width, minContentViewWidth, parentWidth);
            scrapHeight = (int) (scrapWidth / contentViewRatio);
        } else {
            scrapHeight = minMax(height, minContentViewHeight, parentHeight);
            scrapWidth = (int) (contentViewRatio * scrapHeight);
        }

        int left = minMax((int) cropRect.left, 0, parentWidth - scrapWidth);
        int top = minMax((int) cropRect.top, 0, parentHeight - scrapHeight);
        int right = minMax(left + scrapWidth, left + minContentViewWidth, parentWidth);
        int bottom = minMax(top + scrapHeight, top + minContentViewHeight, parentHeight);

        return new Rect(left, top, right, bottom);
    }

    /**
     * 크롭뷰를 주어진 크기만큼 이동한다.
     *
     * @param dx 가로방향으로 이동할 크기
     * @param dy 세로 방향으로 이동할 크기
     */
    private void moveCropViewBy(int dx, int dy) {
//        logger.debug("moveCropViewBy({}, {})", dx, dy);
        tempRect.set(this.cropRect);
        tempRect.offset(dx, dy);
        layoutCropView(tempRect, cropLayout.getWidth(), cropLayout.getHeight());
    }

    /**
     * 크기 조정 화살표 이미지 위치를 결정한다.
     */
    private void layoutArrowImageView() {
        int top = cropView.getTop() - arrowMargin;
        int left = cropView.getLeft() - arrowMargin;
        int bottom = cropView.getBottom() + arrowMargin - leftBottomArrowImageView.getHeight();
        int right = cropView.getRight() + arrowMargin - leftBottomArrowImageView.getWidth();

        leftTopArrowImageView.layout(left, top, left + leftTopArrowImageView.getWidth(),
                top + leftTopArrowImageView.getHeight());
        rightTopArrowImageView.layout(right, top, right + rightTopArrowImageView.getWidth(), top
                + rightTopArrowImageView.getHeight());

        leftBottomArrowImageView.layout(left, bottom, left + leftTopArrowImageView.getWidth(), bottom
                + leftTopArrowImageView.getHeight());
        rightBottomArrowImageView.layout(right, bottom, right + rightTopArrowImageView.getWidth(), bottom
                + rightTopArrowImageView.getHeight());
    }

    /**
     * 사용자가 터치하는 곳이 크롭 영역 경계선 +- EDGE_OFFSET 인경우 크기 조정으로 인식하고 그렇지 않고 안쪽일 경우 위치
     * 이동으로 인식한다.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            moveType = MoveType.none;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                // 첫 터치를 기준으로 사용자 액션 타입을 파악한다.
                moveType = getMoveType(mLastMotionX, mLastMotionY);
                break;

            // 각 타입에 맞추어 크롭 영역에 대해 액션을 수행한다.
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                switch (moveType) {
                    case drag:
                        moveCropViewBy((int) (x - mLastMotionX), (int) (y - mLastMotionY));
                        break;

                    case resize_left:
                        resizeContentViewByX(-(int) (x - mLastMotionX));
                        break;
                    case resize_right:
                        resizeContentViewByX((int) (x - mLastMotionX));
                        break;
                    case resize_top:
                        resizeContentViewByY(-(int) (y - mLastMotionY));
                        break;
                    case resize_bottom:
                        resizeContentViewByY((int) (y - mLastMotionY));
                        break;
                    default:
                        break;
                }
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (moveType != MoveType.none) {
                    moveType = MoveType.none;
                    return true;
                }
                break;

            default:
                break;
        }
        return true;
    }

    /**
     * 위쪽/아래쪽 경계선 부분을 터치하여 y기준으로 크롭 영역 크기가 조정되는 경우
     *
     * @param dy 사용자가 움직인 y좌표 길이
     */
    private void resizeContentViewByY(int dy) {
//        logger.info("resizeContentViewByY({})", dy);
        int dx = (int) (cropRect.width() / cropRect.height() * dy);
        resizeCropViewBy(dx, dy);
    }

    /**
     * 왼쪽/오른쪽 경계선 부분을 터치하여 x기준으로 크롭 영역 크기가 조정되는 경우
     *
     * @param dx 사용자가 움직인 x좌표 길이
     */
    private void resizeContentViewByX(int dx) {
//        logger.info("resizeContentViewByX({})", dx);
        int dy = (int) (cropRect.height() / cropRect.width() * dx);
        resizeCropViewBy(dx, dy);
    }

    /**
     * 최소/최대값 범위에 있는 값을 반환한다.
     *
     * @param value 검사할 값
     * @param min   최소값
     * @param max   최대값
     * @return value가 min보다 작으면 min을 value가 max보다 크면 max를 그렇지 않으면 value자신을 반환한다.
     * 만약 min이 max보다 크다면 바꿔서 실행된다.
     */
    private int minMax(int value, int min, int max) {
        if (max < min)
            return minMax(value, max, min);
        return Math.min(Math.max(min, value), max);
    }

    /**
     * x,y좌표가 크롭 영역에서 어떤 위치에 해당하는지 판단하여 타입을 반환한다.
     *
     * @param x 터치 x
     * @param y 터치 y
     * @return 해당 터치가 이후 이동시 타입
     */
    private MoveType getMoveType(float x, float y) {
//        logger.debug("getMoveType : ({}, {}) in {}", x, y, cropRect.toString());
        MoveType type = MoveType.none;

        cropRect.inset(-EDGE_OFFSET, -EDGE_OFFSET);
        if (cropRect.contains(x, y)) {
            cropRect.inset(EDGE_OFFSET * 2, EDGE_OFFSET * 2);
            if (!resizeEnable || cropRect.contains(x, y)) {
                type = MoveType.drag;
            } else {
                if (x < cropRect.left) {
                    type = MoveType.resize_left;
                } else if (x > cropRect.right) {
                    type = MoveType.resize_right;
                } else if (y < cropRect.top) {
                    type = MoveType.resize_top;
                } else {
                    type = MoveType.resize_bottom;
                }
            }
            cropRect.inset(-EDGE_OFFSET * 2, -EDGE_OFFSET * 2);
        }
        cropRect.inset(EDGE_OFFSET, EDGE_OFFSET);

        return type;
    }

    public RectF getScrapRect() {
        return cropRect;
    }

    public void initCropRect(int width, int height) {
        int minImageSize = width > height ? height : width;

        /**
         * 1. 특정크기 이상의 이미지만 리사이즈가 가능하게 해줍니다.(드래그 기능과 이벤트 겹침) 2. MIN_RECT_SIZE보다
         * 이미지가 작으면 이미지 사이즈를 min사이즈로 합니다.
         */
        if (minImageSize > MIN_RECT_SIZE) {
            resizeEnable = true;
            minContentViewHeight = MIN_RECT_SIZE;
            minContentViewWidth = MIN_RECT_SIZE;
        } else {
            resizeEnable = false;
            minContentViewHeight = minImageSize;
            minContentViewWidth = minImageSize;
        }

        /**
         * 최초에 크롭뷰를 중앙정렬해줍니다.
         */
        int initCropViewOffset = 30;
        int cropViewSize = minImageSize;
        if (minImageSize > MIN_RECT_SIZE + initCropViewOffset * 2) {
            cropViewSize = minImageSize - initCropViewOffset * 2;
        }

        cropView.getLayoutParams().height = cropViewSize;
        cropView.getLayoutParams().width = cropViewSize;

        topDimmedView.getLayoutParams().height = height / 2 - cropViewSize / 2;
        leftDimmedView.getLayoutParams().width = width / 2 - cropViewSize / 2;

//        logger.debug("initCropRect - min : {}, resizeEnable : {}", minImageSize, resizeEnable);
    }

}