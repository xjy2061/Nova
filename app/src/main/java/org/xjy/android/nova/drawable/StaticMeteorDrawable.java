package org.xjy.android.nova.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class StaticMeteorDrawable extends Drawable {
    private Paint mPaint;
    private LinearGradient mLeftShader;
    private LinearGradient mTopShader;
    private LinearGradient mRightShader;
    private LinearGradient mBottomShader;

    public StaticMeteorDrawable(float strokeWidth) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(strokeWidth);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        int width = bounds.width();
        int height = bounds.height();
        float horizontalLength = width * 0.6f;
        float verticalLength = height * 0.6f;
        float leftY = bounds.top + verticalLength;
        float topX = bounds.left + horizontalLength;
        float rightY = bounds.bottom - verticalLength;
        float bottomX = bounds.right - horizontalLength;

        //left
        if (mLeftShader == null) {
            mLeftShader = new LinearGradient(bounds.left, bounds.top, bounds.left, leftY, 0x00ffffff, 0xffffffff, Shader.TileMode.CLAMP);
        }
        mPaint.setShader(mLeftShader);
        canvas.drawLine(bounds.left, bounds.top, bounds.left, leftY, mPaint);

        //top
        if (mTopShader == null) {
            mTopShader = new LinearGradient(bounds.left, bounds.top, topX, bounds.top, 0x00ffffff, 0xffffffff, Shader.TileMode.CLAMP);
        }
        mPaint.setShader(mTopShader);
        canvas.drawLine(bounds.left, bounds.top, topX, bounds.top, mPaint);

        //right
        if (mRightShader == null) {
            mRightShader = new LinearGradient(bounds.right, bounds.bottom, bounds.right, rightY, 0x00ffffff, 0xffffffff, Shader.TileMode.CLAMP);
        }
        mPaint.setShader(mRightShader);
        canvas.drawLine(bounds.right, bounds.bottom, bounds.right, rightY, mPaint);

        //bottom
        if (mBottomShader == null) {
            mBottomShader = new LinearGradient(bounds.right, bounds.bottom, bottomX, bounds.bottom, 0x00ffffff, 0xffffffff, Shader.TileMode.CLAMP);
        }
        mPaint.setShader(mBottomShader);
        canvas.drawLine(bounds.right, bounds.bottom, bottomX, bounds.bottom, mPaint);
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }
}
