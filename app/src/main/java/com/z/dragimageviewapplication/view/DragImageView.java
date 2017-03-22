package com.z.dragimageviewapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.widget.ImageView;


/**
 * Created by Administrator on 14-5-8.
 */
public class DragImageView extends ImageView {

    private static final String TAG = "DragImageView";

    private float MAX_SCALE = 3f;//图片放大最大倍数
    private float MIN_SCALE = 0.5f;//图片缩小最小倍数
    private float NORMAL_SCALE=1f;//正常倍数，缩放效果为屏幕宽度

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();//
    private PointF start = new PointF();//记录单指触摸屏幕点

    private int screen_W, screen_H;// 可见屏幕的宽高度
    private float bitmap_W, bitmap_H;// 当前图片宽高
    private float beforeDistance, afterDistance;// 两触点距离

    private boolean isScaleRestore = false;// 是否需要缩放还原
    private boolean isNeedIntercept=false;//是否需要父类组件拦截处理

    private float scale_temp;// 缩放比例
    private float xCenterPoint;//缩放中心
    private float yCenterPoint;//缩放中心

    private float afterScale;//图片拖放后的比例
    private float xAfterCoordinate;//图片拖放后，左上顶点x坐标
    private float yAfterCoordinate;//图片拖放后，左上顶点y坐标

    private float beforeScale;//图片拖前的比例
    private float xBeforeCoordinate;

    private float beforeMatrixValues[] = new float[9];//图片移动前的矩阵
    private float afterMatrixValues[] = new float[9];
    private float saveMatrixValues[] = new float[9];

    private MODE mode = MODE.NONE;// 默认模式
    /**
     * 模式 NONE：无 DRAG：拖拽. ZOOM:缩放
     *
     */
    private enum MODE {
        NONE, DRAG, ZOOM
    };

    public DragImageView(Context context) {
        super(context);
    }

    public DragImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置显示图片
     * @param bm
     */
    public void setImageBitmap(Bitmap bm) {

        /** 获取图片宽高 **/
        bitmap_W = bm.getWidth();
        bitmap_H = bm.getHeight();

        //设置图片缩放比例
        if (screen_W > 0) {
            //一般比例，即缩放效果为屏幕大小
            NORMAL_SCALE =   screen_W / bitmap_W;
            MIN_SCALE = NORMAL_SCALE / 2;
            MAX_SCALE = NORMAL_SCALE * 3;
        }

        super.setImageBitmap(bm);
        initImgSize();
    }

    /**
     * 可见屏幕宽度 *
     */
    public void setScreen_W(int screen_W) {
        this.screen_W = screen_W;
        this.xCenterPoint = screen_W / 2;
    }

    /**
     * 可见屏幕高度 *
     */
    public void setScreen_H(int screen_H) {
        this.screen_H = screen_H;
        this.yCenterPoint = screen_H / 2;
    }

    /**
     * 初始化图片尺寸
     * 缩放至屏幕宽度，居中
     */
    private void initImgSize(){

        Matrix matrix = getImageMatrix();
        float matrixValue[] = new float[9];
        matrix.getValues(matrixValue);
        float scale = 1 / matrixValue[0] * NORMAL_SCALE;
        matrix.postScale(scale, scale);

        //缩放图片宽度至屏幕宽度
        matrix.getValues(matrixValue);

        float xCenterCoordinate = (screen_W - bitmap_W * scale) / 2;
        float yCenterCoordinate = (screen_H - bitmap_H * scale) / 2;

        float dx = xCenterCoordinate - matrixValue[2];
        float dy = yCenterCoordinate - matrixValue[5];

        //移动图片到屏幕中心
        matrix.postTranslate(dx, dy);

        setImageMatrix(matrix);
    }


    /**
     * touch 事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        /** 处理单点、多点触摸 **/
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                onPointerDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                //处理手指移动时的事件
                onTouchMove(event);

                if (isNeedIntercept) {
//                    返回false，让父类控件处理
                    isNeedIntercept=false;
                    return false;
                }

                break;
            case MotionEvent.ACTION_UP:
                mode = MODE.NONE;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE.NONE;
                /** 执行缩放还原 **/
                if (isScaleRestore) {
                    doScaleAnim();
                    isScaleRestore=false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        setImageMatrix(matrix);
        return true;
    }

    /**
     * 单指按下 *
     */
    void onTouchDown(MotionEvent event) {
        mode = MODE.DRAG;

        getParent().requestDisallowInterceptTouchEvent(true);
        matrix.set(getImageMatrix());
        savedMatrix.set(matrix);
        savedMatrix.getValues(saveMatrixValues);//保存移动前的数据到saveMatrixValues数组
        start.set(event.getX(), event.getY());
    }

    /**
     * 两个手指操作，缩放模式 *
     */
    void onPointerDown(MotionEvent event) {


        beforeDistance = getDistance(event);// 获取两点的距离
        //两只手指，且指间隙大于10f
        if (event.getPointerCount() == 2 && beforeDistance > 10f) {
            savedMatrix.set(matrix);
            savedMatrix.getValues(saveMatrixValues);//保存移动前的数据到saveMatrixValues数组
            mode = MODE.ZOOM;
        }
    }

    /**
     * 移动的处理 *
     */
    void onTouchMove(MotionEvent event) {

        matrix.getValues(beforeMatrixValues);
        beforeScale = beforeMatrixValues[0];//图片左上顶点x坐标
        xBeforeCoordinate = beforeMatrixValues[2];

        /** 处理拖动 **/
        if (mode == MODE.DRAG) {

//            在这里要进行判断处理，防止在drag时候越界
            //图片宽度超过屏幕宽度可以移动
            boolean isWidthBeyond=beforeScale * bitmap_W >= screen_W;
            //图片高度超过屏幕高度可以移动
            boolean isHeightBeyond=beforeScale * bitmap_H > screen_H;

            if (isWidthBeyond||isHeightBeyond) {

                float dx=event.getX() - start.x;
                float dy=event.getY()- start.y;

                matrix.set(savedMatrix);//还原拖动前的值，这里的移动值是相对值，不是绝对坐标值
                matrix.postTranslate(dx,dy);

            }
            getAfterMatrixValues();

            doDragBack();

            //左拖动，且处于超过屏幕左边缘
            boolean isLeftBeyond=(xAfterCoordinate >= 0 && xAfterCoordinate - xBeforeCoordinate >= 0);
            //右拖动，且处于超过屏幕右边缘
            boolean isRightBeyond=(bitmap_W * afterScale + xAfterCoordinate <= screen_W && xAfterCoordinate - xBeforeCoordinate < 0);

            if (isLeftBeyond ||isRightBeyond) {

                setImageMatrix(matrix);
                //调用父类控件进行touchEvent拦截，让父类控件处理该事件
                getParent().requestDisallowInterceptTouchEvent(false);
                isNeedIntercept=true;

            }

        }else if (mode == MODE.ZOOM) {
            /** 处理缩放 **/

            afterDistance = getDistance(event);// 获取两点的距离

            float gapLenght = afterDistance - beforeDistance;// 变化的长度

            if (Math.abs(gapLenght) > 5f) {

                scale_temp = afterDistance / beforeDistance;// 求的缩放的比例
                this.setScale(scale_temp);

            }
            matrix.getValues(afterMatrixValues);
        }
    }

    /**
     * 获取两点的距离 *
     */
    float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * 获取矩阵变化后的矩阵值
     */
    void getAfterMatrixValues(){

        matrix.getValues(afterMatrixValues);

        afterScale = afterMatrixValues[0];
        xAfterCoordinate = afterMatrixValues[2];//图片左上顶点x坐标
        yAfterCoordinate = afterMatrixValues[5];//图片左上顶点y坐标

    }

    /**
     * 缩小还原大小，缩放至图片宽度
     */
    public void doScaleAnim() {
        if (afterScale < NORMAL_SCALE) {
            //放大1/afterScale倍
            float scale = 1 / afterScale * NORMAL_SCALE;
            matrix.postScale(scale, scale, xCenterPoint, yCenterPoint);
        }
    }

    /**
     * 处理缩放 *
     */
    void setScale(float scale) {

        boolean isCanScale = false;
        if (scale > NORMAL_SCALE && beforeScale <= MAX_SCALE) {
            // 放大
            isCanScale = true;
        }else if (scale < NORMAL_SCALE && beforeScale >= MIN_SCALE) {
            // 缩小
            isCanScale = true;
            isScaleRestore = true;
        }
        if (!isCanScale)
            return;

        matrix.set(savedMatrix);//还原放大前的值，这里的放大倍数是绝对值，不是相对值
        matrix.postScale(scale, scale, xCenterPoint, yCenterPoint);
        getAfterMatrixValues();
        doDragBack();

    }

    /**
     * 位置处理，图片超过边缘，则返回边缘，图片尺寸小于屏幕，则返回中间
     */
    public void doDragBack() {


        float imgWidth = bitmap_W * afterScale;//图片宽度=图片原始宽度x缩放倍数
        float imgHeight = bitmap_H * afterScale;//图片高度=图片原始高度度x缩放倍数

        if (mode == MODE.DRAG) {
            //在拖动模式下，如果图片大于屏幕，，不处理返回
            boolean isDeal = imgWidth >= screen_W || imgHeight >= screen_H;

            if (!isDeal) {
                return;
            }
        }


        boolean isDragBackHorizontal = false;//能否图片水平方向变变化
        boolean isDragBackVertical = false;//能否图片垂直方向变化


        float xCenterCoordinate = (screen_W - imgWidth) / 2;//图片x轴中心点
        float yCenterCoordinate = (screen_H - imgHeight) / 2;//图片y轴中心点

        float xEdgeCoordinate=xAfterCoordinate + imgWidth;//图片x轴边沿坐标
        float yEdgeCoordinate=yAfterCoordinate + imgHeight;//图片y轴边沿坐标

        float dx = 0;//水平方向调整距离
        float dy = 0;//垂直方向调整距离

        /** 水平进行判断 **/
        if (xAfterCoordinate > 0) {
            //是否图片右边越过左边屏幕
            dx = -xAfterCoordinate;
            isDragBackHorizontal = true;
        }else if (xEdgeCoordinate< screen_W) {
            //是否图片左边越过右边屏幕
            dx = screen_W - xEdgeCoordinate;
            isDragBackHorizontal = true;

        }

        //如果图片宽度小于屏幕宽度，返回中间
        if (imgWidth < screen_W) {
            dx = xCenterCoordinate - xAfterCoordinate;
            isDragBackHorizontal = true;
        }


        /** 垂直进行判断 **/
        if (yAfterCoordinate > 0) {
            //是否图片上面越过上边屏幕
            dy = -yAfterCoordinate;
            isDragBackVertical = true;
        }else if (yEdgeCoordinate < screen_H) {
            //是否图片下面越过下边屏幕
            dy = screen_H -yEdgeCoordinate;
            isDragBackVertical = true;

        }
        //如果图片高度小于屏幕高度，返回中间
        if (imgHeight < screen_H) {
            dy = yCenterCoordinate - yAfterCoordinate;
            isDragBackVertical = true;
        }
        if (isDragBackHorizontal || isDragBackVertical)
            matrix.postTranslate(dx, dy);
    }


}
