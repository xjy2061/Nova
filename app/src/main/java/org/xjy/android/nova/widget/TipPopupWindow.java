package org.xjy.android.nova.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.drawable.RoundedColorDrawable;

import org.xjy.android.nova.R;
import org.xjy.android.nova.utils.DimensionUtils;

public class TipPopupWindow extends PopupWindow {
    public static final int ARROW_LEFT = 1;
    public static final int ARROW_TOP = 2;
    public static final int ARROW_RIGHT = 3;
    public static final int ARROW_BOTTOM = 4;

    private RelativeLayout mContentView;
    private RelativeLayout mTipContainer;
    private TextView mTipView;
    private TextView mConfirmView;
    private ImageView mArrowView;

    private int mWidth;
    private int mBackgroundColor;
    private int mTipTextColor;
    private int mTipTextSize;
    private String mTipText;
    private int mConfirmTextColor;
    private int mConfirmTextSize;
    private String mConfirmText;
    private int mArrowDirection;
    private int mArrowOffset;

    private int mHorizontalOffset;
    private int mVerticalOffset;

    public TipPopupWindow(Context context, int width, String tip) {
        super(context);
        mContentView = new RelativeLayout(context);
        mContentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mTipContainer = new RelativeLayout(context);
        mTipContainer.setId(R.id.container);
        mTipView = new TextView(context);
        mTipView.setId(R.id.tip);
        mConfirmView = new TextView(context);
        mArrowView = new ImageView(context);

        mWidth = width;
        mBackgroundColor = 0xcd000000;
        mTipTextColor = 0xffffffff;
        mTipTextSize = 13;
        mTipText = tip;
        mConfirmTextColor = 0xff5ab5e7;
        mConfirmTextSize = mTipTextSize;
        mConfirmText = context.getString(R.string.known);
        mArrowDirection = ARROW_BOTTOM;

        mConfirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContentView(mContentView);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
    }

    public void setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
    }

    public void setTipTextColor(int color) {
        mTipTextColor = color;
    }

    public void setTipTextSize(int size) {
        mTipTextSize = size;
    }

    public void setTipText(String text) {
        mTipText = text;
    }

    public void setConfirmTextColor(int color) {
        mConfirmTextColor = color;
    }

    public void setConfirmTextSize(int size) {
        mConfirmTextSize = size;
    }

    public void setConfirmText(String text) {
        mConfirmText = text;
    }

    public void setArrowDirection(int direction) {
        mArrowDirection = direction;
    }

    public void setArrowOffset(int offset) {
        mArrowOffset = offset;
    }

    public void setHorizontalOffset(int offset) {
        mHorizontalOffset = offset;
    }

    public void setVerticalOffset(int offset) {
        mVerticalOffset = offset;
    }

    public void show(View anchorView) {
        mTipContainer.setBackgroundDrawable(new RoundedColorDrawable(DimensionUtils.dpToIntPx(4), mBackgroundColor));
        mTipView.setTextColor(mTipTextColor);
        mTipView.setTextSize(mTipTextSize);
        mTipView.setText(mTipText);
        int dimen13dp = DimensionUtils.dpToIntPx(13);
        mTipView.setPadding(dimen13dp, dimen13dp, dimen13dp, 0);
        mTipContainer.addView(mTipView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mConfirmView.setTextColor(mConfirmTextColor);
        mConfirmView.setTextSize(mConfirmTextSize);
        mConfirmView.setText(mConfirmText);
        mConfirmView.setPadding(dimen13dp, dimen13dp, dimen13dp, DimensionUtils.dpToIntPx(17));
        RelativeLayout.LayoutParams confirmParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        confirmParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        confirmParams.addRule(RelativeLayout.BELOW, mTipView.getId());
        mTipContainer.addView(mConfirmView, confirmParams);
        mContentView.addView(mTipContainer, new RelativeLayout.LayoutParams(mWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        // TODO: Use correct drawable
        mArrowView.setImageResource(R.drawable.ic_menu_share);
        RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (mArrowDirection == ARROW_LEFT) {
            arrowParams.addRule(RelativeLayout.LEFT_OF, mTipContainer.getId());
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            Bitmap bitmap = ((BitmapDrawable) mArrowView.getDrawable()).getBitmap();
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            mArrowView.setImageDrawable(new BitmapDrawable(mArrowView.getResources(), bitmap));
        } else if (mArrowDirection == ARROW_TOP) {
            arrowParams.addRule(RelativeLayout.ABOVE, mTipContainer.getId());
        } else if (mArrowDirection == ARROW_RIGHT) {
            arrowParams.addRule(RelativeLayout.RIGHT_OF, mTipContainer.getId());
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap bitmap = ((BitmapDrawable) mArrowView.getDrawable()).getBitmap();
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            mArrowView.setImageDrawable(new BitmapDrawable(mArrowView.getResources(), bitmap));
        } else if (mArrowDirection == ARROW_BOTTOM) {
            arrowParams.addRule(RelativeLayout.BELOW, mTipContainer.getId());
            Matrix matrix = new Matrix();
            matrix.postRotate(180);
            Bitmap bitmap = ((BitmapDrawable) mArrowView.getDrawable()).getBitmap();
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            mArrowView.setImageDrawable(new BitmapDrawable(mArrowView.getResources(), bitmap));
        }
        mContentView.addView(mArrowView, arrowParams);
        mContentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int windowWidth = mContentView.getMeasuredWidth();
        int windowHeight = mContentView.getMeasuredHeight();
        int tipContainerWidth = mTipContainer.getMeasuredWidth();
        int tipContainerHeight = mTipContainer.getMeasuredHeight();
        int anchorViewWidth = anchorView.getWidth();
        int anchorViewHeight = anchorView.getHeight();
        int[] location = new int[2];
        anchorView.getLocationInWindow(location);
        if (mArrowDirection == ARROW_LEFT) {
            location[0] += anchorViewWidth + mHorizontalOffset;
            location[1] += mVerticalOffset;
            arrowParams.topMargin = mArrowOffset == 0 ? (tipContainerHeight - mArrowView.getMeasuredHeight()) >> 1 : mArrowOffset;
        } else if (mArrowDirection == ARROW_TOP) {
            location[0] += mHorizontalOffset;
            location[1] += anchorViewHeight + mVerticalOffset;
            arrowParams.leftMargin = mArrowOffset == 0 ? (tipContainerWidth - mArrowView.getMeasuredWidth()) >> 1 : mArrowOffset;
        } else if (mArrowDirection == ARROW_RIGHT) {
            location[0] -= windowWidth + mHorizontalOffset;
            location[1] += mVerticalOffset;
            arrowParams.topMargin = mArrowOffset == 0 ? (tipContainerHeight - mArrowView.getMeasuredHeight()) >> 1 : mArrowOffset;
        } else if (mArrowDirection == ARROW_BOTTOM) {
            location[0] += mHorizontalOffset;
            location[1] += -windowHeight + mVerticalOffset;
            arrowParams.leftMargin = mArrowOffset == 0 ? (tipContainerWidth - mArrowView.getMeasuredWidth()) >> 1 : mArrowOffset;
        }
        showAtLocation(anchorView, Gravity.NO_GRAVITY, location[0], location[1]);
    }
}
