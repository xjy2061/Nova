package org.xjy.android.nova.utils;

import android.widget.Toast;

import org.xjy.android.nova.NovaApplication;

public class PromptUtils {

    public static void showToast(int resId) {
        Toast.makeText(NovaApplication.getInstance(), resId, Toast.LENGTH_LONG).show();
    }

    public static void showToast(CharSequence text) {
        Toast.makeText(NovaApplication.getInstance(), text, Toast.LENGTH_LONG).show();
    }
}
