package org.xjy.android.nova.transfer.upload.core;

import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Pair;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.xjy.android.nova.api.NovaApi;
import org.xjy.android.nova.common.io.IoUtils;
import org.xjy.android.nova.common.net.HttpClientFactory;
import org.xjy.android.nova.transfer.common.ProgressListener;
import org.xjy.android.nova.transfer.common.QuitGuard;
import org.xjy.android.nova.transfer.common.TransferException;
import org.xjy.android.nova.transfer.storage.UploadStorage;
import org.xjy.android.nova.transfer.upload.utils.ProgressNotifyInputStream;
import org.xjy.android.nova.transfer.upload.utils.RepeatableFileInputStream;
import org.xjy.android.nova.transfer.upload.utils.RepeatableInputStreamEntity;
import org.xjy.android.nova.transfer.upload.utils.SubInputStream;
import org.xjy.android.nova.transfer.upload.utils.UploadUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Uploader {
    private static final String LBS_SERVER = "";
    private static final String LBS_VERSION = "1.0";
    private static final String UPLOAD_SERVER = "";
    private static final String UPLOAD_VERSION = "1.0";
    private static final int RETRY_COUNT = 2;
    private static final String HEADER_TOKEN = "";

    public static final int ERROR_FILE_NOT_EXIST = -1;
    public static final int ERROR_QUIT = -2;
    public static final int ERROR_NETWORK = -3;
    public static final int ERROR_DATA_RESTRICT = -4;
    public static final int ERROR_API_SERVER = -5;
    public static final int ERROR_UPLOAD_SERVER = -6;
    public static final int ERROR_UNKNOWN = -50;
    private static final int ERROR_CACHE_EXPIRED = -100;
    private static final int ERROR_TOKEN_EXPIRED = -101;
    private static final int ERROR_CHANGE_SERVER = -102;

    public static long upload(File file, String type, String contentType, boolean needBreakPointResume) {
        return upload(file, type, contentType, null, needBreakPointResume, null, null);
    }

    public static long upload(File file, String type, String contentType, String md5, boolean needBreakPointResume, ProgressListener progressListener, QuitGuard quitGuard) {
        if (file.length() == 0) {
            return ERROR_FILE_NOT_EXIST;
        }
        try {
            UploadObject uploadObject = createUploadObject(file, type, contentType, md5, needBreakPointResume, quitGuard);
            if (quitGuard != null && quitGuard.isQuit()) {
                return ERROR_QUIT;
            }
            NetworkType networkType = NetworkType.getNetworkType();
            String bucketName = uploadObject.getBucketName();
            Pair<Integer, ArrayList<String>> queryResult = queryLbs(bucketName, networkType.getType());
            if (quitGuard != null && quitGuard.isQuit()) {
                return ERROR_QUIT;
            }
            if (queryResult.first < 0 && queryResult.second.size() == 0) {
                return queryResult.first;
            }
            return uploadFile(file, uploadObject, networkType.getChunkSize(), queryResult.second, progressListener, quitGuard);
        } catch (Throwable t) {
            if (t instanceof TransferException) {
                if (((TransferException) t).getType() == TransferException.TYPE_QUIT) {
                    return ERROR_QUIT;
                }
            }
//            else if (t instanceof WifiOnlyException) {
//                return ERROR_DATA_RESTRICT;
//            } else if (t instanceof ApiException) {
//                int code = ((ApiException) t).getCode();
//                if (code == ApiException.CODE.IO_ERR) {
//                    return ERROR_NETWORK;
//                } else if (code == ApiException.CODE.SERVER_ERR) {
//                    return ERROR_API_SERVER;
//                }
//            }
        }
        return ERROR_UNKNOWN;
    }

    private static UploadObject createUploadObject(File file, String type, String contentType, String md5, boolean needBreakPointResume, QuitGuard quitGuard) throws Throwable {
        UploadDao uploadDao = UploadDao.getInstance();
        String path = file.getAbsolutePath();
        UploadObject uploadObject = null;
        if (needBreakPointResume) {
            uploadObject = uploadDao.getUploadObject(path);
        }
        if (uploadObject == null) {
            if (TextUtils.isEmpty(md5)) {
                md5 = UploadUtils.calcMD5(file, quitGuard);
            }
            String fileName = file.getName();
            int suffixIndex = fileName.lastIndexOf(".");
            uploadObject = NovaApi.getUploadObject(fileName, type, suffixIndex > 0 ? fileName.substring(suffixIndex + 1) : "");
            uploadObject.setMd5(md5);
            if (needBreakPointResume) {
                uploadDao.insert(path, uploadObject);
            }
        }
        uploadObject.setContentType(contentType);
        return uploadObject;
    }

    private static Pair<Integer, ArrayList<String>> queryLbs(String bucketName, int networkType) {
        ArrayList<String> uploadServers;
//        if ((networkType == NetworkType.TYPE_MOBILE_SLOW || networkType == NetworkType.TYPE_MOBILE_FAST) && OperatorFreeUtils.isOperatorFree()) {
//            uploadServers = new ArrayList<>();
//            uploadServers.add(UPLOAD_SERVER);
//            return new Pair<>(1, uploadServers);
//        }
        int oldNetworkType = UploadStorage.getNetworkType(bucketName);
        if (oldNetworkType != networkType) {
            UploadStorage.saveLbsTime(bucketName, 0);
            UploadStorage.saveNetworkType(bucketName, networkType);
        }
        uploadServers = UploadStorage.getUploadServer(bucketName);
        if (uploadServers.size() > 0 && UploadStorage.getLbsTime(bucketName) + DateUtils.HOUR_IN_MILLIS > System.currentTimeMillis()) {
            return new Pair<>(1, uploadServers);
        }
        String lbsServer = UploadStorage.getLbsServer(bucketName);
        if (TextUtils.isEmpty(lbsServer)) {
            lbsServer = LBS_SERVER;
        } else {
            lbsServer += ";" + LBS_SERVER;
        }
        Throwable throwable = null;
        HttpClient httpClient = HttpClientFactory.createSingletonHttpClient();
        String[] servers = lbsServer.split(";");
        for (String server : servers) {
            throwable = null;
            HttpGet request = null;
            try {
                request = new HttpGet(Uri.parse(server).buildUpon().appendQueryParameter("version", LBS_VERSION).appendQueryParameter("bucketname", bucketName).toString());
                JSONObject jsonResult = new JSONObject(EntityUtils.toString(httpClient.execute(request).getEntity()));
                UploadStorage.saveLbsServer(bucketName, jsonResult.getString("lbs"));
                UploadStorage.saveUploadServer(bucketName, jsonResult.getJSONArray("upload").toString());
                UploadStorage.saveLbsTime(bucketName, System.currentTimeMillis());
                return new Pair<>(1, UploadStorage.getUploadServer(bucketName));
            } catch (Throwable t) {
                throwable = t;
                t.printStackTrace();
            } finally {
                if (request != null) {
                    request.abort();
                }
            }
        }
        return new Pair<>(throwable instanceof IOException ? ERROR_NETWORK : ERROR_UPLOAD_SERVER, uploadServers);
    }

    private static long uploadFile(File file, UploadObject uploadObject, long chunkSize, ArrayList<String> uploadServers, ProgressListener progressListener, QuitGuard quitGuard) {
        String bucketName = uploadObject.getBucketName();
        String objectName = uploadObject.getObjectName();
        String token = uploadObject.getToken();
        String context = uploadObject.getContext();
        String path = file.getAbsolutePath();
        long offset = 0;
        if (!TextUtils.isEmpty(context)) {
            offset = getOffset(bucketName, objectName, token, context, uploadServers, quitGuard);
            if (offset == ERROR_TOKEN_EXPIRED) {
                token = NovaApi.refreshUploadToken(bucketName, objectName);
                if (token == null) {
                    return ERROR_API_SERVER;
                } else if ("".equals(token)) {
                    return ERROR_NETWORK;
                }
                UploadDao.getInstance().updateToken(path, token);
                offset = getOffset(bucketName, objectName, token, context, uploadServers, quitGuard);
            }
            if (offset < 0) {
                if (offset == ERROR_TOKEN_EXPIRED) {
                    return ERROR_UPLOAD_SERVER;
                } else if (offset == ERROR_CACHE_EXPIRED) {
                    context = null;
                    UploadDao.getInstance().updateContext(path, null);
                } else {
                    return offset;
                }
            }
        }
        if (offset > file.length()) {
            UploadDao.getInstance().delete(path);
            return ERROR_UPLOAD_SERVER;
        }
        if (quitGuard != null && quitGuard.isQuit()) {
            return ERROR_QUIT;
        }
        int result = uploadData(file, bucketName, objectName, token, uploadObject.getMd5(), uploadObject.getContentType(), context, offset, chunkSize, uploadServers, progressListener, quitGuard);
        if (result > 0) {
            UploadDao.getInstance().delete(path);
            return uploadObject.getFileId();
        }
        return result;
    }

    private static long getOffset(String bucketName, String objectName, String token, String context, ArrayList<String> uploadServers, QuitGuard quitGuard) {
        HttpClient httpClient = HttpClientFactory.createSingletonHttpClient();
        Throwable throwable = null;
        String query = "?uploadContext&version=" + UPLOAD_VERSION + "&context=" + context;
        for (int i = 0, size = uploadServers.size(); i < size; i++) {
            String server = uploadServers.get(i);
            String url = Uri.parse(server).buildUpon().appendPath(bucketName).appendPath(objectName).toString() + query;
            for (int retryCount = 0; retryCount < RETRY_COUNT; retryCount++) {
                if (quitGuard != null && quitGuard.isQuit()) {
                    return ERROR_QUIT;
                }
                throwable = null;
                HttpGet request = null;
                try {
                    request = new HttpGet(url);
                    request.addHeader(HEADER_TOKEN, token);
                    HttpResponse response = httpClient.execute(request);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        return new JSONObject(EntityUtils.toString(response.getEntity())).getLong("offset");
                    } else if (statusCode == 403) {
                        return ERROR_TOKEN_EXPIRED;
                    } else if (statusCode == 404) {
                        return ERROR_CACHE_EXPIRED;
                    }
                } catch (Throwable t) {
                    throwable = t;
                    t.printStackTrace();
                } finally {
                    if (request != null) {
                        request.abort();
                    }
                }
            }
        }
        if (throwable instanceof IOException) {
            return ERROR_NETWORK;
        } else {
            return ERROR_UPLOAD_SERVER;
        }
    }

    private static int uploadData(File file, String bucketName, String objectName, String token, String md5, String contentType, String context, long offset, long chunkSize, ArrayList<String> uploadServers, ProgressListener progressListener, QuitGuard quitGuard) {
        for (final long fileLength = file.length(); offset < fileLength;) {
            long realChunkSize = Math.min(chunkSize, fileLength - offset);
            boolean complete = realChunkSize + offset >= fileLength;
            for (int i = 0, size = uploadServers.size(); i < size; i++) {
                String server = uploadServers.get(i);
                Uri.Builder builder = Uri.parse(server).buildUpon().appendPath(bucketName).appendPath(objectName).appendQueryParameter("version", UPLOAD_VERSION).appendQueryParameter("offset", offset + "")
                        .appendQueryParameter("complete", complete + "");
                if (!TextUtils.isEmpty(context)) {
                    builder.appendQueryParameter("context", context);
                }
                String url = builder.toString();
                Object[] result = uploadChunk(file, url, token, md5, contentType, context, offset, realChunkSize, progressListener, quitGuard);
                int code = (int) result[0];
                if (code == ERROR_TOKEN_EXPIRED) {
                    token = NovaApi.refreshUploadToken(bucketName, objectName);
                    if (token == null) {
                        return ERROR_API_SERVER;
                    } else if ("".equals(token)) {
                        return ERROR_NETWORK;
                    }
                    UploadDao.getInstance().updateToken(file.getAbsolutePath(), token);
                    result = uploadChunk(file, url, token, md5, contentType, context, offset, realChunkSize, progressListener, quitGuard);
                }
                code = (int) result[0];
                if (code == 200) {
                    offset = (long) result[1];
                    context = (String) result[2];
                    break;
                } else if (code == ERROR_QUIT) {
                    return code;
                } else if (code == ERROR_TOKEN_EXPIRED) {
                    return ERROR_UPLOAD_SERVER;
                } else if (i == size - 1) {
                    return ERROR_UPLOAD_SERVER;
                }
            }
        }
        return 1;
    }

    private static Object[] uploadChunk(File file, String url, String token, String md5, String contentType, String context, long offset, long chunkSize, ProgressListener progressListener, QuitGuard quitGuard) {
        Object[] result = new Object[3];
        HttpClient httpClient = HttpClientFactory.createSingletonHttpClient();
        for (int retryCount = 0; retryCount < RETRY_COUNT; retryCount++) {
            if (quitGuard != null && quitGuard.isQuit()) {
                result[0] = ERROR_QUIT;
                return result;
            }
            HttpPost request = null;
            ProgressNotifyInputStream inputStream = null;
            try {
                request = new HttpPost(url);
                request.addHeader(HEADER_TOKEN, token);
                request.addHeader("Content-MD5", md5);
                if (!TextUtils.isEmpty(contentType)) {
                    request.addHeader("Content-Type", contentType);
                }
                inputStream = new ProgressNotifyInputStream(new SubInputStream(new RepeatableFileInputStream(file), offset, chunkSize, true), new FrontProgressListener(offset, file.length(), progressListener));
                request.setEntity(new RepeatableInputStreamEntity(inputStream, chunkSize));
                HttpResponse response = httpClient.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    JSONObject jsonResult = new JSONObject(EntityUtils.toString(response.getEntity()));
                    offset = jsonResult.getLong("offset");
                    String newContext = jsonResult.getString("context");
                    if (!newContext.equals(context)) {
                        context = newContext;
                        UploadDao.getInstance().updateContext(file.getAbsolutePath(), context);
                    }
                    result[0] = 200;
                    result[1] = offset;
                    result[2] = context;
                    return result;
                } else if (statusCode == 403) {
                    result[0] = ERROR_TOKEN_EXPIRED;
                    return result;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                if (request != null) {
                    request.abort();
                }
                IoUtils.closeSilently(inputStream);
            }
        }
        result[0] = ERROR_CHANGE_SERVER;
        return result;
    }

    public static class FrontProgressListener implements ProgressListener {
        private long mProgress;
        private long mMax;
        private ProgressListener mProgressListener;

        public FrontProgressListener(long progress, long max, ProgressListener progressListener) {
            mProgress = progress;
            mMax = max;
            mProgressListener = progressListener;
        }

        @Override
        public void onProgressChanged(long progress, long max) {
            mProgress += progress;
            if (mProgressListener != null) {
                mProgressListener.onProgressChanged(mProgress, mMax);
            }
        }
    }
}
