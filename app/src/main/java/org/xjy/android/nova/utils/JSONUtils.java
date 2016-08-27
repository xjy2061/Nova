package org.xjy.android.nova.utils;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class JSONUtils {

    public static ArrayList<String> jsonArrayStringToStringList(String jsonArrayString) {
        try {
            if (!TextUtils.isEmpty(jsonArrayString)) {
                return jsonArrayToStringList(new JSONArray(jsonArrayString));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static ArrayList<String> jsonArrayToStringList(JSONArray jsonArray) {
        ArrayList<String> list = new ArrayList<>();
        try {
            int length;
            if (jsonArray != null && (length = jsonArray.length()) > 0) {
                for (int i = 0; i < length; i++) {
                    list.add(jsonArray.getString(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
