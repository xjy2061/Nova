package org.xjy.android.nova.utils;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import org.xjy.android.nova.NovaApplication;

import java.util.ArrayList;

public class StateListFactory {

    public static StateListDrawable createStateListDrawable(int pressed, int selected, int focused, int other) {
        StateListDrawable drawable = new StateListDrawable();
        Resources resources = NovaApplication.getInstance().getResources();
        if (pressed > 0) {
            drawable.addState(new int[]{android.R.attr.state_pressed}, resources.getDrawable(pressed));
        }
        if (selected > 0) {
            drawable.addState(new int[]{android.R.attr.state_selected}, resources.getDrawable(selected));
        }
        if (focused > 0) {
            drawable.addState(new int[]{android.R.attr.state_focused}, resources.getDrawable(focused));
        }
        drawable.addState(new int[]{}, resources.getDrawable(other));
        return drawable;
    }

    public static StateListDrawable createStateListDrawable(Drawable pressed, Drawable selected, Drawable focused, Drawable other) {
        StateListDrawable drawable = new StateListDrawable();
        if (pressed != null) {
            drawable.addState(new int[]{android.R.attr.state_pressed}, pressed);
        }
        if (selected != null) {
            drawable.addState(new int[]{android.R.attr.state_selected}, selected);
        }
        if (focused != null) {
            drawable.addState(new int[]{android.R.attr.state_focused}, focused);
        }
        drawable.addState(new int[]{}, other);
        return drawable;
    }

    public static StateListDrawable createStateListDrawable(int normal, int normalPressed, int selected, int selectedPressed, int disable) {
        StateListDrawable drawable = new StateListDrawable();
        Resources resources = NovaApplication.getInstance().getResources();
        if (selectedPressed > 0) {
            drawable.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_selected, android.R.attr.state_pressed}, resources.getDrawable(selectedPressed));
        }
        if (selected > 0) {
            drawable.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_selected}, resources.getDrawable(selected));
        }
        if (normalPressed > 0) {
            drawable.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed}, resources.getDrawable(normalPressed));
        }
        if (disable > 0) {
            drawable.addState(new int[]{-android.R.attr.state_enabled}, resources.getDrawable(disable));
        }
        drawable.addState(new int[]{}, resources.getDrawable(normal));
        return drawable;
    }

    public static ColorStateList createColorStateList(int pressed, int selected, int other) {
        ArrayList<int[]> stateList = new ArrayList<>();
        ArrayList<Integer> colorList = new ArrayList<>();
        if (pressed != 0) {
            stateList.add(new int[]{android.R.attr.state_pressed});
            colorList.add(pressed);
        }
        if (selected != 0) {
            stateList.add(new int[]{android.R.attr.state_selected});
            colorList.add(selected);
        }
        stateList.add(new int[]{});
        colorList.add(other);
        int size = stateList.size();
        int[][] states = new int[size][];
        int[] colors = new int[size];
        for (int i = 0; i < size; i++) {
            states[i] = stateList.get(i);
            colors[i] = colorList.get(i);
        }
        return new ColorStateList(states, colors);
    }

    public static ColorStateList createColorStateList(int disable, int other) {
        int[][] states = new int[2][];
        states[0] = new int[]{-android.R.attr.state_enabled};
        states[1] = new int[]{};
        int[] colors = new int[]{disable, other};
        return new ColorStateList(states, colors);
    }
}
