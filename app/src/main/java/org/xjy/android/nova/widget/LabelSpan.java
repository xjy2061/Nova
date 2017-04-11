package org.xjy.android.nova.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.text.style.ReplacementSpan;

public class LabelSpan extends ReplacementSpan {
    private int mStrokeColor;
    private int mTextSize;
    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;
    private CharSequence mText;
    private int mSpanWidth;
    private int mSpanHeight;
    private int mSpanTop;

    public LabelSpan(int strokeColor, int textSize) {
        mStrokeColor = strokeColor;
        mTextSize = textSize;
    }

    public void setPadding(int left, int top, int right, int bottom) {
        mPaddingLeft = left;
        mPaddingTop = top;
        mPaddingRight = right;
        mPaddingBottom = bottom;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        if (mText == null) {
            mText = text.subSequence(start, end);
        }
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        mSpanTop = fontMetrics.descent + fontMetrics.ascent;
        paint.setTextSize(mTextSize);
        mSpanWidth = (int) paint.measureText(text, start, end) + mPaddingLeft + mPaddingRight;
        fontMetrics = paint.getFontMetricsInt();
        mSpanHeight = fontMetrics.bottom - fontMetrics.top + mPaddingTop + mPaddingBottom;
        mSpanTop = (mSpanTop - mSpanHeight) / 2;
        return mSpanWidth;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        paint.setColor(mStrokeColor);
        paint.setStyle(Paint.Style.STROKE);
        mSpanTop = mSpanTop + y;
        canvas.drawRect(x, mSpanTop, x + mSpanWidth, mSpanTop + mSpanHeight, paint);
        canvas.drawText(mText != null ? mText : text, start, end, x + mPaddingLeft, mSpanTop + mPaddingTop - paint.getFontMetrics().top, paint);
    }
}
