package info.camposha.loves;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import java.lang.ref.WeakReference;


public class LoveView extends View {

    //The `Paint` is a class that holds the style and color information about
    // how to draw geometries, text and bitmaps.
    private Paint mPaint,mPaintBackground,mPaintLove,mPaintBrokenLine;
    //`RectF` is a class that holds 4 float coordinates for a rectangle.
    // The rectangle is represented by the coordinates of its 4 edges
    // (left, top, right bottom).
    private RectF rectFBg;
    private LoveType mUnLoveType = LoveType.broken;
    private int mTagKey;
    private int mBrokenAngle = 13;
    //colors
    private int edgeColor = Color.BLACK;
    private int fillColor = Color.rgb(229, 115, 108);
    private int cracksColor, backgroundColor = Color.WHITE;
    private Bitmap mBitmapBrokenLeftLove,mBitmapBrokenRightLove = null;
    private OnLove mOnLove;
    //`ValueAnimator` is a class that provides a simple timing engine for
    // running animations which calculate animated values and set them on
    // target objects.
    private ValueAnimator valueAnimator;
    private float loveSize = 0.8f;
    private float mAnimatedLoveValue,mAnimatedBrokenValue = 0f;
    float MaxSize = 1.2f;
    //`WeakReference` is a class that allows us prevent our View from being reclaimed
    private WeakReference<View> mHeartView;

    public void setHeartView(View heartView) {
        this.mHeartView = new WeakReference<>(heartView);
    }
    public View getHeartView() {
        return mHeartView != null ? mHeartView.get() : null;
    }
    public void setUnLikeType(LoveType type) {
        this.mUnLoveType = type;
    }
    public void setEdgeColor(int color) {
        this.edgeColor = color;
    }
    public void setFillColor(int color) {
        this.fillColor = color;
    }
    public void setCracksColor(int color) {
        this.cracksColor = color;
    }
    public void setOnThumbUp(OnLove onThumbUp) {
        this.mOnLove = onThumbUp;
    }
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }

    /**
     * Let's come define 3 constructors with various arguments
     * @param context
     */
    public LoveView(Context context) {
        this(context, null);
    }
    public LoveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public LoveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * Let's come resolve attributes we defined in the love_style.xml.
     * @param attrs
     */
    private void resolveAttributes(AttributeSet attrs){
        //We obtain those attributes using `obtainStyledAttributes()` method then
        //hold them in a container of type TypedArray.
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LoveView);
        if (typedArray != null) {
            edgeColor = typedArray.getColor(R.styleable.LoveView_edgeColor, edgeColor);
            fillColor = typedArray.getColor(R.styleable.LoveView_fillColor, fillColor);
            cracksColor = typedArray.getColor(R.styleable.LoveView_cracksColor, cracksColor);
            backgroundColor = typedArray.getColor(R.styleable.LoveView_backgroundColor, backgroundColor);

            int type = typedArray.getInteger(R.styleable.LoveView_unLoveType, 0);
            
            if (type == 0) { mUnLoveType = LoveType.broken;
            } else { mUnLoveType = LoveType.unLove;}

            //we free up our TypedArray
            typedArray.recycle();
        }
    }
    
    private void init(AttributeSet attrs) {
        this.resolveAttributes(attrs);
        this.initPaint();
    }

    /**
     * This method is responsible for initializing several `Paint` objects
     */
    private void initPaint() {
        mTagKey = getId();
        setHeartView(this);

        //Paint Object with stroke
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        //Paint object for Background fill
        mPaintBackground = new Paint();
        mPaintBackground.setAntiAlias(true);
        mPaintBackground.setStyle(Paint.Style.FILL);

        mPaintLove = new Paint();
        mPaintLove.setAntiAlias(true);
        mPaintLove.setStyle(Paint.Style.FILL);

        mPaintBrokenLine = new Paint();
        mPaintBrokenLine.setAntiAlias(true);
        mPaintBrokenLine.setStyle(Paint.Style.STROKE);

        rectFBg = new RectF();
    }

    /**
     * Let's come create a helper method to convert dp to px. dp stands for density
     * independent pixels while px is pixels. dp or dip is an abstract unit of
     * measuring pixels based on density.
     * @param dpValue
     * @return
     */
    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * Let's override a method from View class called `onMeasure()`
     * It allows us Measure the view and its content to determine the measured
     * width and the
     * measured height.
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST
                && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(dip2px(30), dip2px(30));
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(heightSpecSize, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, widthSpecSize);
        }
    }

    /**
     * Then we come ovveride the `onSizedChanged()` method
     * It is called during layout when the size of this view has changed.
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    RectF rectFloveBg = new RectF();

    /**
     * Let's create a method to draw us the LOVE symbol and fill it
     * with background
     * @param canvas
     * @param mPaint
     * @param mAnimatedValue
     * @param fill
     */
    private void drawLove(Canvas canvas, Paint mPaint, float mAnimatedValue, boolean fill) {
        if (mAnimatedValue - 1 > (MaxSize - 1) / 2f) {
            mAnimatedValue = 1 + (MaxSize - mAnimatedValue);
        }
        mAnimatedValue = mAnimatedValue * loveSize;

        RectF loveRectF = new RectF();
        loveRectF.top = rectFBg.centerY() - (rectFBg.height() / 2f + mPaint.getStrokeWidth()) * mAnimatedValue;//* 0.5f;
        loveRectF.bottom = rectFBg.centerY() + (rectFBg.height() / 2f + mPaint.getStrokeWidth()) * mAnimatedValue;// * 0.5f;
        loveRectF.left = rectFBg.centerX() - (rectFBg.width() / 2f + mPaint.getStrokeWidth()) * mAnimatedValue;// * 0.5f;
        loveRectF.right = rectFBg.centerX() + (rectFBg.width() / 2f + mPaint.getStrokeWidth()) * mAnimatedValue;//* 0.5f;

        float realWidth = loveRectF.width();
        float realHeight = loveRectF.height();
        loveRectF.top = loveRectF.top + realHeight * (1 - 0.8f) / 2f;
        Path path = new Path();

        float startYScale = 0;
        if (fill) {
            startYScale = 0.17f;
        } else {
            startYScale = 0.185f;
        }

        path.moveTo((float) (0.5 * realWidth) + loveRectF.left, (float) (startYScale * realHeight) + loveRectF.top);
        path.cubicTo((float) (0.15 * realWidth) + loveRectF.left, (float) (-0.35 * realHeight + loveRectF.top),
                (float) (-0.4 * realWidth) + loveRectF.left, (float) (0.45 * realHeight) + loveRectF.top,
                (float) (0.5 * realWidth) + loveRectF.left, (float) (realHeight * 0.8 + loveRectF.top));

        path.cubicTo((float) (realWidth + 0.4 * realWidth) + loveRectF.left, (float) (0.45 * realHeight) + loveRectF.top,
                (float) (realWidth - 0.15 * realWidth) + loveRectF.left, (float) (-0.35 * realHeight) + loveRectF.top,
                (float) (0.5 * realWidth) + loveRectF.left, (float) (startYScale * realHeight) + loveRectF.top);
        path.close();

        //Let's draw the specified path using tha Paint objects we supply
        canvas.drawPath(path, mPaintBackground);
        canvas.drawPath(path, mPaint);
    }


    /**
     * Let's create a mtehod to draw a broken love
     * @param canvasMain
     * @param mAnimatedBrokenValue
     */
    private void drawBrokenLove(Canvas canvasMain, float mAnimatedBrokenValue) {
        Canvas canvas;
       
        RectF loveRectF = new RectF();
        loveRectF.top = rectFBg.centerY() - (rectFBg.height() / 2f + mPaintLove.getStrokeWidth()) * loveSize;//* 0.5f;
        loveRectF.bottom = rectFBg.centerY() + (rectFBg.height() / 2f + mPaintLove.getStrokeWidth()) * loveSize;// * 0.5f;
        loveRectF.left = rectFBg.centerX() - (rectFBg.width() / 2f + mPaintLove.getStrokeWidth()) * loveSize;// * 0.5f;
        loveRectF.right = rectFBg.centerX() + (rectFBg.width() / 2f + mPaintLove.getStrokeWidth()) * loveSize;//* 0.5f;
        
        float realWidth = loveRectF.width();
        float realHeight = loveRectF.height();
        loveRectF.top = loveRectF.top + realHeight * (1 - 0.8f) / 2f;
        
        float fristX = (float) (0.5 * realWidth) + loveRectF.left;
        float fristY = (float) (0.17 * realHeight) + loveRectF.top;
        float lastX = (float) (0.5 * realWidth) + loveRectF.left;
        float lastY = (float) (realHeight * 0.8 + loveRectF.top);
        float secondX = lastX + realWidth / 14f;
        float secondY = fristY + (lastY - fristY) / 4f;
        float thirdX = lastX - realWidth / 12f;
        float thirdY = fristY + (lastY - fristY) / 2.5f;

        mBitmapBrokenLeftLove = Bitmap.createBitmap(getMeasuredWidth(), (int) lastY, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(mBitmapBrokenLeftLove);
        canvas.rotate(-1 * mBrokenAngle * mAnimatedBrokenValue, lastX, lastY);

        Path path = new Path();
        path.moveTo((float) (0.5 * realWidth) + loveRectF.left, (float) (0.17 * realHeight) + loveRectF.top);
        path.cubicTo((float) (0.15 * realWidth) + loveRectF.left, (float) (-0.35 * realHeight + loveRectF.top),
                (float) (-0.4 * realWidth) + loveRectF.left, (float) (0.45 * realHeight) + loveRectF.top,
                (float) (0.5 * realWidth) + loveRectF.left, (float) (realHeight * 0.8 + loveRectF.top));
        path.lineTo(thirdX, thirdY);
        path.lineTo(secondX, secondY);
        path.close();
        canvas.drawPath(path, mPaintLove);

        mBitmapBrokenRightLove = Bitmap.createBitmap(getMeasuredWidth(), (int) lastY, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(mBitmapBrokenRightLove);
        canvas.rotate(mBrokenAngle * mAnimatedBrokenValue, lastX, lastY);

        path.reset();
        path.moveTo((float) (0.5 * realWidth) + loveRectF.left, (float) (realHeight * 0.8 + loveRectF.top));
        path.cubicTo((float) (realWidth + 0.4 * realWidth) + loveRectF.left, (float) (0.45 * realHeight) + loveRectF.top,
                (float) (realWidth - 0.15 * realWidth) + loveRectF.left, (float) (-0.35 * realHeight) + loveRectF.top,
                (float) (0.5 * realWidth) + loveRectF.left, (float) (0.17 * realHeight) + loveRectF.top);
        path.lineTo(secondX, secondY);
        path.lineTo(thirdX, thirdY);

        path.close();
        canvas.drawPath(path, mPaintLove);
        canvasMain.drawBitmap(mBitmapBrokenLeftLove, 0, 0, mPaint);
        canvasMain.drawBitmap(mBitmapBrokenRightLove, 0, 0, mPaint);
    }

    /**
     * Let's define method to draw broken line.
     * @param canvas
     * @param mAnimatedBrokenValue
     */
    private void drawBrokenLine(Canvas canvas, float mAnimatedBrokenValue) {
        RectF loveRectF = new RectF();
        loveRectF.top = rectFBg.centerY() - (rectFBg.height() / 2f + mPaintLove.getStrokeWidth()) * loveSize;//* 0.5f;
        loveRectF.bottom = rectFBg.centerY() + (rectFBg.height() / 2f + mPaintLove.getStrokeWidth()) * loveSize;// * 0.5f;
        loveRectF.left = rectFBg.centerX() - (rectFBg.width() / 2f + mPaintLove.getStrokeWidth()) * loveSize;// * 0.5f;
        loveRectF.right = rectFBg.centerX() + (rectFBg.width() / 2f + mPaintLove.getStrokeWidth()) * loveSize;//* 0.5f;
        float realWidth = loveRectF.width();
        float realHeight = loveRectF.height();
        loveRectF.top = loveRectF.top + realHeight * (1 - 0.8f) / 2f;
        
        float fristX = (float) (0.5 * realWidth) + loveRectF.left;
        float fristY = (float) (0.17 * realHeight) + loveRectF.top;
        
        float lastX = (float) (0.5 * realWidth) + loveRectF.left;
        float lastY = (float) (realHeight * 0.8 + loveRectF.top);

        float secondX = lastX + realWidth / 14f;
        float secondY = fristY + (lastY - fristY) / 4f;

        float thirdX = lastX - realWidth / 12f;
        float thirdY = fristY + (lastY - fristY) / 2.5f;

        Path line = new Path();
        line.moveTo(fristX, fristY);

        if (mAnimatedBrokenValue > 0 && mAnimatedBrokenValue < 0.25f) {
            line.lineTo((secondX - fristX) * (mAnimatedBrokenValue / 0.25f) + fristX,
                    (secondY - fristY) * (mAnimatedBrokenValue / 0.25f) + fristY);
        }
        
        if (mAnimatedBrokenValue >= 0.25 && mAnimatedBrokenValue < 0.5f) {
            line.lineTo(secondX, secondY);
            line.lineTo((thirdX - secondX) * ((mAnimatedBrokenValue - 0.25f) / 0.25f) + secondX
                    , (thirdY - secondY) * ((mAnimatedBrokenValue - 0.25f) / 0.25f) + secondY);
        }
        
        if (mAnimatedBrokenValue >= 0.5 && mAnimatedBrokenValue <= 1f) {
            line.lineTo(secondX, secondY);
            line.lineTo(thirdX, thirdY);
            line.lineTo((lastX - thirdX) * ((mAnimatedBrokenValue - 0.5f) / 0.5f) + thirdX,
                    (lastY - thirdY) * ((mAnimatedBrokenValue - 0.5f) / 0.5f) + thirdY);
        }
        mPaintBrokenLine.setStrokeWidth(loveRectF.width() / 40);
        canvas.drawPath(line, mPaintBrokenLine);
    }

    /**
     * Then we create an override of a method `onDraw`. It's normally implemented
     * when we want to do our own drawing.
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        mPaint.setColor(edgeColor);
        mPaintLove.setColor(fillColor);
        mPaintBrokenLine.setColor(cracksColor);
        mPaintBackground.setColor(backgroundColor);
        
        rectFBg = new RectF(0, 0,getMeasuredWidth(),getMeasuredHeight());
        
        rectFloveBg.top = rectFBg.centerY() - (rectFBg.height() / 2f) * loveSize;//* 0.5f;
        rectFloveBg.bottom = rectFBg.centerY() + (rectFBg.height() / 2f) * loveSize;// * 0.5f;
        rectFloveBg.left = rectFBg.centerX() - (rectFBg.width() / 2f) * loveSize;// * 0.5f;
        rectFloveBg.right = rectFBg.centerX() + (rectFBg.width() / 2f) * loveSize;//* 0.5f;

        mPaintLove.setStrokeWidth(rectFBg.width() / 20 + dip2px(1));
        mPaint.setStrokeWidth(rectFBg.width() / 40);

        drawLove(canvas, mPaint, 1f, false);
        drawLove(canvas, mPaintLove, mAnimatedLoveValue, true);

        if (mAnimatedBrokenValue > 0 && mAnimatedBrokenValue < 0.5f) {
            float v = mAnimatedBrokenValue / 0.5f;
            drawBrokenLine(canvas, v);
        } else if (mAnimatedBrokenValue >= 0.5f && mAnimatedBrokenValue < 0.75f) {
            mAnimatedLoveValue = 0f;
            float v = (mAnimatedBrokenValue - 0.5f) / 0.25f;
            drawBrokenLove(canvas, mAnimatedBrokenValue);
        } else if (mAnimatedBrokenValue >= 0.75f && mAnimatedBrokenValue < 1f) {
            float v = (mAnimatedBrokenValue - 0.75f) / 0.25f;
            drawDrops(canvas, v);
        }
        canvas.restore();
    }

    /**
     * Let's define a method to draw the drops in our LoveView.
     * @param canvas
     * @param mAnimatedBrokenValue
     */
    private void drawDrops(Canvas canvas, float mAnimatedBrokenValue) {
        if (mAnimatedBrokenValue == 1)return;

        RectF loveRectF = new RectF();
        loveRectF.top = rectFBg.centerY() -
                (rectFBg.height() / 2f + mPaintLove.getStrokeWidth()) * loveSize;//* 0.5f;
        loveRectF.bottom = rectFBg.centerY() + (rectFBg.height() / 2f +
                mPaintLove.getStrokeWidth()) * loveSize;// * 0.5f;
        loveRectF.left = rectFBg.centerX() - (rectFBg.width() / 2f +
                mPaintLove.getStrokeWidth()) * loveSize;// * 0.5f;
        loveRectF.right = rectFBg.centerX() + (rectFBg.width() / 2f +
                mPaintLove.getStrokeWidth()) * loveSize;//* 0.5f;

        canvas.drawCircle(loveRectF.centerX() - loveRectF.width() / 4,
                loveRectF.centerY() + loveRectF.height() / 10 +
                        loveRectF.height() / 3 * mAnimatedBrokenValue,
                loveRectF.width() / 15 +
                        loveRectF.width() / 18 * (1 - mAnimatedBrokenValue),
                mPaintLove
        );

        canvas.drawCircle(loveRectF.centerX() + loveRectF.width() / 4,
                loveRectF.centerY() + loveRectF.height() / 10
                        + loveRectF.height() / 3 * mAnimatedBrokenValue,
                loveRectF.width() / 15 +
                        loveRectF.width() / 18 * (1 - mAnimatedBrokenValue),
                mPaintLove
        );
    }

    /**
     * We now define a public method to show some Love.
     */
    public void love() {
        if (mAnimatedLoveValue == 0 || mAnimatedLoveValue == MaxSize) {
            post(new Runnable() {
                @Override
                public void run() {
                    startLoveAnim(LoveType.Love);
                }
            });
        }
    }

    /**
     * Let's create a method to show some unLove
     */
    public void unLove() {
        if (mAnimatedLoveValue == MaxSize) {
            post(new Runnable() {
                @Override
                public void run() {
                    startLoveAnim(mUnLoveType);
                }
            });
        }
    }

    /**
     * Let's create a private method to start showing some Love with with animation.
     * @param loveType
     */
    private void startLoveAnim(LoveType loveType) {
        if (loveType == LoveType.unLove) {
            startViewAnim(0f, 1f, 200, loveType);
            getHeartView().setTag(mTagKey, false);

        } else if (loveType == LoveType.Love) {
            getHeartView().setTag(mTagKey, true);
            startViewAnim(0f, 1f, 200, loveType);

        } else if (loveType == LoveType.broken) {
            getHeartView().setTag(mTagKey, false);
            startViewAnim(0f, 1f, 400, loveType);
        }
        if (mOnLove != null)
            mOnLove.Love((Boolean) getHeartView().getTag(mTagKey));
    }

    /**
     * Let's create a method to stop the animation
     */
    public void stopAnim() {
        if (valueAnimator != null) {
            clearAnimation();
            valueAnimator.setRepeatCount(0);
            valueAnimator.cancel();
            valueAnimator.end();
            mAnimatedLoveValue = 0f;
            postInvalidate();
        }
    }

    /**
     * Let's create a private method to start animating a view using ValueAnimator
     * @param startF
     * @param endF
     * @param time
     * @param Love
     * @return
     */
    private ValueAnimator startViewAnim(float startF, final float endF, long time, final LoveType Love) {
        valueAnimator = ValueAnimator.ofFloat(startF, endF);
        valueAnimator.setDuration(time);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatCount(0);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                if (Love == LoveType.unLove) {
                    mAnimatedLoveValue = (float) valueAnimator.getAnimatedValue();
                    mAnimatedLoveValue = 1 - mAnimatedLoveValue;
                } else if (Love == LoveType.Love) {
                    mAnimatedLoveValue = (float) valueAnimator.getAnimatedValue();
                    mAnimatedLoveValue = mAnimatedLoveValue + (MaxSize - 1f);
                } else if (Love == LoveType.broken) {
                    mAnimatedBrokenValue = (float) valueAnimator.getAnimatedValue();
                }
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }
        });
        if (!valueAnimator.isRunning()) {
            valueAnimator.start();
        }

        return valueAnimator;
    }

    float startX = 0;
    float startY = 0;

    /**
     * Let's override the onTouchEvent() method of the View class.
     * @param event - MotionEvent. We pass it this for it to use for reporting 
     *              movement (mouse, pen, finger, trackball) events. 
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            startX = event.getX();
            startY = event.getY();
            return true;
        } else if (MotionEvent.ACTION_UP == event.getAction()) {
            if (Math.abs(event.getX() - startX) < 5 &&
                    Math.abs(event.getY() - startY) < 5) {
                if (rectFloveBg.contains(event.getX(), event.getY())) {
                    if (getHeartView().getTag(mTagKey) == null || !(Boolean) getHeartView().getTag(mTagKey)) {
                        startLoveAnim(LoveType.Love);

                    } else if ((Boolean) getHeartView().getTag(mTagKey)) {
                        if (mUnLoveType == LoveType.broken) {
                            startLoveAnim(LoveType.broken);
                        } else if (mUnLoveType == LoveType.unLove) {
                            startLoveAnim(LoveType.unLove);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Let's create an enun to represent different types of Loves.
     */
    public enum LoveType {
        broken, unLove, Love
    }

    /**
     * Let's create an interface to define us `Love` method signature.
     */
    public interface OnLove {
        void Love(boolean Love);
    }

    /**
     * Let's create a public method allow us expose our Love to implementers.
     */
    public void setLove() {
        mAnimatedLoveValue = MaxSize;
        mAnimatedBrokenValue = 0f;
        getHeartView().setTag(mTagKey, true);
        invalidate();
    }

    public void setUnLove() {
        mAnimatedLoveValue = 0f;
        mAnimatedBrokenValue = 0f;
        getHeartView().setTag(mTagKey, false);
        invalidate();
    }
}
