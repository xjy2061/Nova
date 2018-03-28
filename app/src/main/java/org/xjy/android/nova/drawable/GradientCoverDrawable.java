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

public class GradientCoverDrawable extends Drawable {
    private float mTopCoverHeight;
    private float mBottomCoverHeight;
    private int mTopColorFrom;
    private int mTopColorTo;
    private int mBottomColorFrom;
    private int mBottomColorTo;
    private float mRadius;
    private RectF mRect = new RectF();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private LinearGradient mTopShader;
    private LinearGradient mBottomShader;

    public GradientCoverDrawable(float topCoverHeight, float bottomCoverHeight, float radius, int colorFrom, int colorTo) {
        this(topCoverHeight, bottomCoverHeight, radius, colorFrom, colorTo, colorFrom, colorTo);
    }

    public GradientCoverDrawable(float topCoverHeight, float bottomCoverHeight, float radius, int topColorFrom, int topColorTo, int bottomColorFrom, int bottomColorTo) {
        mTopCoverHeight = topCoverHeight;
        mBottomCoverHeight = bottomCoverHeight;
        mRadius = radius;
        mTopColorFrom = topColorFrom;
        mTopColorTo = topColorTo;
        mBottomColorFrom = bottomColorFrom;
        mBottomColorTo = bottomColorTo;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int width = canvas.getWidth();
        if (mTopCoverHeight > 0) {
            if (mTopShader == null) {
                mTopShader = new LinearGradient(0, 0, 0, mTopCoverHeight, mTopColorFrom, mTopColorTo, Shader.TileMode.CLAMP);
            }
            mPaint.setShader(mTopShader);
            mRect.set(0, 0, width, mTopCoverHeight);
            if (mRadius > 0) {
                canvas.drawRoundRect(mRect, mRadius, mRadius, mPaint);
            } else {
                canvas.drawRect(mRect, mPaint);
            }
        }
        if (mBottomCoverHeight > 0) {
            int height = canvas.getHeight();
            if (mBottomShader == null) {
                mBottomShader = new LinearGradient(0, height - mBottomCoverHeight, 0, height, mBottomColorTo, mBottomColorFrom, Shader.TileMode.CLAMP);
            }
            mPaint.setShader(mBottomShader);
            mRect.set(0, height - mBottomCoverHeight, width, height);
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
