package org.xjy.android.nova.drawable;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;

import java.util.ArrayList;

public class MeteorDrawable extends Drawable implements Animatable {
    private Paint mPaint;
    private AnimatorSet mLeftAnimator;
    private AnimatorSet mTopAnimator;
    private AnimatorSet mRightAnimator;
    private AnimatorSet mBottomAnimator;

    public MeteorDrawable(int strokeWidth) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(strokeWidth);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (mLeftAnimator == null) {
            mLeftAnimator = createAnimator(bounds.top, bounds.bottom, 570, 230);
            mTopAnimator = createRoundAnimator(bounds.left, bounds.right, 570, 830, 230);
            mRightAnimator = createAnimator(bounds.bottom, bounds.top, 570, 230);
            mBottomAnimator = createRoundAnimator(bounds.right, bounds.left, 570, 830, 230);
        }

        drawLine(mLeftAnimator, bounds.left, false, canvas);
        drawHorizontalLine(mTopAnimator, bounds.top, canvas);
        drawHorizontalLine(mBottomAnimator, bounds.bottom, canvas);
        drawLine(mRightAnimator, bounds.right, false, canvas);

        if (isRunning()) {
            invalidateSelf();
        }
    }

    private AnimatorSet createAnimator(int start, int end, long duration, long interval) {
        AnimatorSet set = new AnimatorSet();
        HoldIntValueAnimator first = createTranslateAnimator(start, end, duration);
        HoldIntValueAnimator second = createTranslateAnimator(start, end, duration);
        second.setStartDelay(interval);
        AnimatorSet translateAnimator = new AnimatorSet();
        translateAnimator.play(first).with(second);
        set.play(translateAnimator).with(createAlphaAnimator(duration + interval));
        return set;
    }

    private AnimatorSet createRoundAnimator(int start, int end, long duration, long backDuration, long interval) {
        AnimatorSet set = new AnimatorSet();
        set.play(createAnimator(end, start, backDuration, interval)).after(createAnimator(start, end, duration, interval));
        return set;
    }

    private HoldIntValueAnimator createTranslateAnimator(int start, int end, long duration) {
        HoldIntValueAnimator animator = HoldIntValueAnimator.ofInt(start, end);
        animator.setDuration(duration);
        animator.setInterpolator(PathInterpolatorCompat.create(0.6f, 0, 0.4f, 1));
        return animator;
    }

    private ValueAnimator createAlphaAnimator(long duration) {
        ValueAnimator animator = ValueAnimator.ofInt(0, 128, 0);
        animator.setDuration(duration);
        return animator;
    }

    private void drawHorizontalLine(AnimatorSet animatorSet, int y, Canvas canvas) {
        if (animatorSet.isStarted()) {
            ArrayList<Animator> animators = animatorSet.getChildAnimations();
            for (int i = 0, size = animators.size(); i < size; i++) {
                drawLine((AnimatorSet) animators.get(i), y, true, canvas);
            }
        }
    }

    private void drawLine(AnimatorSet animatorSet, int coordinate, boolean horizontal, Canvas canvas) {
        if (animatorSet.isStarted()) {
            ArrayList<Animator> animators = animatorSet.getChildAnimations();
            for (int i = 0, size = animators.size(); i < size; i++) {
                Animator animator = animators.get(i);
                int start = 0;
                int end = 0;
                if (animator instanceof AnimatorSet) {
                    ArrayList<Animator> animations = ((AnimatorSet) animator).getChildAnimations();
                    start = (int) ((ValueAnimator) animations.get(0)).getAnimatedValue();
                    HoldIntValueAnimator holdIntValueAnimator = (HoldIntValueAnimator) animations.get(1);
                    if (holdIntValueAnimator.isStarted() && !holdIntValueAnimator.isRunning()) {
                        end = holdIntValueAnimator.mValues[0];
                    } else {
                        end = (int) holdIntValueAnimator.getAnimatedValue();
                    }
                } else {
                    mPaint.setAlpha((Integer) ((ValueAnimator) animator).getAnimatedValue());
                }
                if (horizontal) {
                    canvas.drawLine(start, coordinate, end, coordinate, mPaint);
                } else {
                    canvas.drawLine(coordinate, start, coordinate, end, mPaint);
                }
            }
        }
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    public void start() {
        if (mLeftAnimator != null && !isRunning()) {
            mLeftAnimator.start();
            mTopAnimator.start();
            mRightAnimator.start();
            mBottomAnimator.start();
            invalidateSelf();
        }
    }

    @Override
    public void stop() {
        if (mLeftAnimator != null) {
            mLeftAnimator.end();
            mTopAnimator.end();
            mRightAnimator.end();
            mBottomAnimator.end();
        }
    }

    @Override
    public boolean isRunning() {
        return mLeftAnimator.isRunning() || mTopAnimator.isRunning() || mRightAnimator.isRunning() || mBottomAnimator.isRunning();
    }

    private static class HoldIntValueAnimator extends ValueAnimator {
        private int[] mValues;

        @Override
        public void setIntValues(int... values) {
            super.setIntValues(values);
            mValues = values;
        }

        public static HoldIntValueAnimator ofInt(int... values) {
            HoldIntValueAnimator anim = new HoldIntValueAnimator();
            anim.setIntValues(values);
            return anim;
        }
    }
}
