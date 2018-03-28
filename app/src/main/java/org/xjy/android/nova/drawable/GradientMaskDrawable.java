package org.xjy.android.nova.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class GradientMaskDrawable extends Drawable {
    private float mTopMaskHeight;
    private float mBottomMaskHeight;
    private int mTopColorFrom;
    private int mTopColorTo;
    private int mBottomColorFrom;
    private int mBottomColorTo;
    private float mRadius;
    private RectF mRect = new RectF();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private LinearGradient mTopShader;
    private LinearGradient mBottomShader;

    public GradientMaskDrawable(float topMaskHeight, float bottomMaskHeight, float radius, int colorFrom, int colorTo) {
        this(topMaskHeight, bottomMaskHeight, radius, colorFrom, colorTo, colorFrom, colorTo);
    }

    public GradientMaskDrawable(float topMaskHeight, float bottomMaskHeight, float radius, int topColorFrom, int topColorTo, int bottomColorFrom, int bottomColorTo) {
        mTopMaskHeight = topMaskHeight;
        mBottomMaskHeight = bottomMaskHeight;
        mRadius = radius;
        mTopColorFrom = topColorFrom;
        mTopColorTo = topColorTo;
        mBottomColorFrom = bottomColorFrom;
        mBottomColorTo = bottomColorTo;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int width = canvas.getWidth();
        if (mTopMaskHeight > 0) {
            if (mTopShader == null) {
                mTopShader = new LinearGradient(0, 0, 0, mTopMaskHeight, mTopColorFrom, mTopColorTo, Shader.TileMode.CLAMP);
            }
            mPaint.setShader(mTopShader);
            mRect.set(0, 0, width, mTopMaskHeight);
            if (mRadius > 0) {
                canvas.drawRoundRect(mRect, mRadius, mRadius, mPaint);
            } else {
                canvas.drawRect(mRect, mPaint);
            }
        }
        if (mBottomMaskHeight > 0) {
            int height = canvas.getHeight();
            if (mBottomShader == null) {
                mBottomShader = new LinearGradient(0, height - mBottomMaskHeight, 0, height, mBottomColorTo, mBottomColorFrom, Shader.TileMode.CLAMP);
            }
            mPaint.setShader(mBottomShader);
            mRect.set(0, height - mBottomMaskHeight, width, height);
            if (mRadius > 0) {
                canvas.drawRoundRect(mRect, mRadius, mRadius, mPaint);
            } else {
                canvas.drawRect(mRect, mPaint);
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }
}
