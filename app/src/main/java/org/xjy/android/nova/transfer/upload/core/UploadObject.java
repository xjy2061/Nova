package org.xjy.android.nova.transfer.upload.core;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class UploadObject {
    private String bucketName;
    private String objectName;
    private String token;
    private long fileId;
    private String md5;
    private String contentType;
    private String context;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String toJSONString() {
        try {
            JSONObject json = new JSONObject();
            json.put("bucketName", bucketName);
            json.put("objectName", objectName);
            json.put("token", token);
            json.put("fileId", fileId);
            json.put("md5", md5);
            json.put("contentType", contentType);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static UploadObject create(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(jsonString);
            UploadObject uploadObject = new UploadObject();
            uploadObject.setBucketName(json.getString("bucketName"));
            uploadObject.setObjectName(json.getString("objectName"));
            uploadObject.setToken(json.getString("token"));
            uploadObject.setFileId(json.getLong("fileId"));
            uploadObject.setMd5(json.getString("md5"));
            uploadObject.setContentType(json.optString("contentType"));
            return uploadObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
