package org.xjy.android.nova.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.xjy.android.nova.utils.DimensionUtils;

public class ColorPicker extends View {
    private static final int[] COLORS = new int[] {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};

    private float mThumbRadius;
    private int mHeight;
    private float mBarWidth;
    private float mBarHeight;
    private Paint mHueBarPaint;
    private Paint mValueBarPaint;
    private Paint mThumbPaint;
    private RectF mHueBarRectF;
    private RectF mValueBarRectF;
    private float mHueThumbCenterX;
    private float mValueThumbCenterX;
    private Shader mValueBarShader;

    private float[] mHue = new float[]{0f, 1f, 1f};
    private float[] mHSVColor = new float[3];

    private OnColorChangedListener mOnColorChangedListener;

    public ColorPicker(Context context) {
        super(context);
        init(context);
    }

    public ColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mThumbRadius = DimensionUtils.dpToPx(12);
        mHeight = (int) (mThumbRadius * 4 + DimensionUtils.dpToPx(17f) + 0.5);
        mBarHeight = DimensionUtils.dpToPx(4f);

        mHueBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHueBarPaint.setStyle(Paint.Style.STROKE);
        mHueBarPaint.setStrokeWidth(mBarHeight);
        mValueBarPaint = new Paint(mHueBarPaint);
        mThumbPaint = new Paint();
        mThumbPaint.setAntiAlias(true);

        setColor(COLORS[0]);
    }

    public void setColor(int color) {
        mThumbPaint.setColor(color);
        Color.colorToHSV(color, mHSVColor);
        mHSVColor[1] = 1f;
        mHue[0] = mHSVColor[0];
        if (mBarWidth > 0) {
            mHueThumbCenterX = mBarWidth * (mHSVColor[0] / 360) + mThumbRadius;
            mValueThumbCenterX = mBarWidth * mHSVColor[2] + mThumbRadius;
            mValueBarShader = new LinearGradient(mValueBarRectF.left, mValueBarRectF.top, mValueBarRectF.right, mValueBarRectF.bottom, new int[]{Color.BLACK, Color.HSVToColor(mHue)}, null, Shader.TileMode.CLAMP);
            mValueBarPaint.setShader(mValueBarShader);
            invalidate();
        }
    }

    public void setOnColorChangedListener(OnColorChangedListener onColorChangedListener) {
        mOnColorChangedListener = onColorChangedListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = widthMeasureSpec;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else if (widthMode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        setMeasuredDimension(width, mHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float halfBarHeight = mBarHeight / 2;
        mHueBarRectF = new RectF(mThumbRadius, mThumbRadius - halfBarHeight, w - mThumbRadius, mThumbRadius + halfBarHeight);
        mValueBarRectF = new RectF(mThumbRadius, h - mThumbRadius - halfBarHeight, w - mThumbRadius, h - mThumbRadius + halfBarHeight);
        mBarWidth = (int) (mHueBarRectF.right - mHueBarRectF.left);
        mHueThumbCenterX = mBarWidth * (mHSVColor[0] / 360) + mThumbRadius;
        mValueThumbCenterX = mBarWidth * mHSVColor[2] + mThumbRadius;
        Shader hueBarShader = new LinearGradient(mHueBarRectF.left, mHueBarRectF.top, mHueBarRectF.right, mHueBarRectF.bottom, COLORS, null, Shader.TileMode.CLAMP);
        mValueBarShader = new LinearGradient(mValueBarRectF.left, mValueBarRectF.top, mValueBarRectF.right, mValueBarRectF.bottom, new int[]{Color.BLACK, Color.HSVToColor(mHue)}, null, Shader.TileMode.CLAMP);
        mHueBarPaint.setShader(hueBarShader);
        mValueBarPaint.setShader(mValueBarShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //hue bar
        canvas.drawRoundRect(mHueBarRectF, mBarHeight / 2, mBarHeight / 2, mHueBarPaint);

        //value bar
        canvas.drawRoundRect(mValueBarRectF, mBarHeight / 2, mBarHeight / 2, mValueBarPaint);

        //hue bar thumb
        canvas.drawCircle(mHueThumbCenterX, mThumbRadius, mThumbRadius, mThumbPaint);

        //value bar thumb
        canvas.drawCircle(mValueThumbCenterX, mHeight - mThumbRadius, mThumbRadius, mThumbPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (x >= mHueBarRectF.left && x <= mHueBarRectF.right && y <= mThumbRadius * 2) {
                    mHueThumbCenterX = x;
                    mHue[0] = (x - mThumbRadius) / mBarWidth * 360;
                    mValueBarShader = new LinearGradient(mValueBarRectF.left, mValueBarRectF.top, mValueBarRectF.right, mValueBarRectF.bottom, new int[]{Color.BLACK, Color.HSVToColor(mHue)}, null, Shader.TileMode.CLAMP);
                    mValueBarPaint.setShader(mValueBarShader);
                    mHSVColor[0] = mHue[0];
                } else if (x >= mValueBarRectF.left && x < mValueBarRectF.right && y >= mHeight - mThumbRadius * 2) {
                    mValueThumbCenterX = x;
                    mHSVColor[2] = (x - mThumbRadius) / mBarWidth;
                }
                int color = Color.HSVToColor(mHSVColor);
                mThumbPaint.setColor(color);
                if (mOnColorChangedListener != null) {
                    mOnColorChangedListener.onColorChanged(color);
                }
                invalidate();
                break;
        }
        return true;
    }

    public interface OnColorChangedListener {
        void onColorChanged(int color);
    }
}
