package org.xjy.android.nova.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;

public class RangeEllipsisTextView extends AppCompatTextView {
    private static final String ELLIPSIS_STRING = "\u2026";

    private int mEllipsisStart;
    private int mEllipsisEnd;
    private CharSequence mEllipsisText;
    private int mTextWidth;

    public RangeEllipsisTextView(Context context) {
        super(context);
    }

    public RangeEllipsisTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RangeEllipsisTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setEllipsisRange(int start, int end) {
        mEllipsisStart = start;
        mEllipsisEnd = end;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        CharSequence originText = getText();
        int length = originText != null ? originText.length() : 0;
        if (length > 0 && mEllipsisEnd > mEllipsisStart && mEllipsisEnd - mEllipsisStart < length) {
            int drawableWidth = 0;
            Drawable[] drawables = getCompoundDrawables();
            if (drawables[0] != null) {
                drawableWidth = drawables[0].getBounds().width();
            }
            if (drawables[2] != null) {
                drawableWidth += drawables[2].getBounds().width();
            }
            int textWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - getCompoundDrawablePadding() - drawableWidth;
            if (mTextWidth != textWidth || !originText.equals(mEllipsisText)) {
                TextPaint paint = getPaint();
                String originString = originText.toString();
                if (paint.measureText(originString) > textWidth) {
                    String head = originString.substring(0, mEllipsisStart);
                    String tail = originString.substring(mEllipsisEnd, length);
                    float avail = textWidth - paint.measureText(ELLIPSIS_STRING) - paint.measureText(head) - paint.measureText(tail);
                    int i = mEllipsisStart;
                    for (; i < mEllipsisEnd; i++) {
                        avail -= paint.measureText(originString, i, i + 1);
                        if (avail < 0) {
                            break;
                        }
                    }
                    mEllipsisText = head + originString.substring(mEllipsisStart, i) + ELLIPSIS_STRING + tail;
                    setText(mEllipsisText);
                } else {
                    mEllipsisText = originText;
                }
            }
            mTextWidth = textWidth;
        }
        super.onDraw(canvas);
    }
}
