package org.xjy.android.nova.transfer.upload.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.xjy.android.nova.transfer.common.AbstractDao;
import org.xjy.android.nova.transfer.storage.TransferDatabase;

public class UploadDao extends AbstractDao {
    private static UploadDao sUploadDao;

    private UploadDao() {}

    public static UploadDao getInstance() {
        if (sUploadDao == null) {
            sUploadDao = new UploadDao();
        }
        return sUploadDao;
    }

    @Override
    public SQLiteDatabase getDatabase() {
        return TransferDatabase.getInstance().getDatabase();
    }

    public int insert(String path, UploadObject uploadObject) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(TransferDatabase.UploadColumns.FILE_PATH, path);
            contentValues.put(TransferDatabase.UploadColumns.BUCKET_NAME, uploadObject.getBucketName());
            contentValues.put(TransferDatabase.UploadColumns.OBJECT_NAME, uploadObject.getObjectName());
            contentValues.put(TransferDatabase.UploadColumns.TOKEN, uploadObject.getToken());
            contentValues.put(TransferDatabase.UploadColumns.FILE_ID, uploadObject.getFileId());
            contentValues.put(TransferDatabase.UploadColumns.MD5, uploadObject.getMd5());
            contentValues.put(TransferDatabase.UploadColumns.CONTEXT, uploadObject.getContext());
            mDatabase.insertWithOnConflict(TransferDatabase.UPLOAD_TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            return 1;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return -1;
    }

    public int updateToken(String path, String token) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(TransferDatabase.UploadColumns.TOKEN, token);
            mDatabase.update(TransferDatabase.UPLOAD_TABLE_NAME, cv, TransferDatabase.UploadColumns.FILE_PATH + "=?", new String[]{path});
            return 1;
        } catch (Throwable t) {
            handleException(t);
            t.printStackTrace();
        }
        return -1;
    }

    public int updateContext(String path, String context) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(TransferDatabase.UploadColumns.CONTEXT, context);
            mDatabase.update(TransferDatabase.UPLOAD_TABLE_NAME, cv, TransferDatabase.UploadColumns.FILE_PATH + "=?", new String[]{path});
            return 1;
        } catch (Throwable t) {
            handleException(t);
            t.printStackTrace();
        }
        return -1;
    }

    public int delete(String path) {
        try {
            mDatabase.delete(TransferDatabase.UPLOAD_TABLE_NAME, TransferDatabase.UploadColumns.FILE_PATH + "=?", new String[]{path});
            return 1;
        } catch (Throwable t) {
            handleException(t);
            t.printStackTrace();
        }
        return -1;
    }

    public UploadObject getUploadObject(String path) {
        Cursor cursor = null;
        try {
            String sql = "SELECT * FROM " + TransferDatabase.UPLOAD_TABLE_NAME + " WHERE " + TransferDatabase.UploadColumns.FILE_PATH + "=?";
            cursor = mDatabase.rawQuery(sql, new String[]{path});
            while (cursor.moveToNext()) {
                UploadObject uploadObject = new UploadObject();
                uploadObject.setBucketName(cursor.getString(cursor.getColumnIndex(TransferDatabase.UploadColumns.BUCKET_NAME)));
                uploadObject.setObjectName(cursor.getString(cursor.getColumnIndex(TransferDatabase.UploadColumns.OBJECT_NAME)));
                uploadObject.setToken(cursor.getString(cursor.getColumnIndex(TransferDatabase.UploadColumns.TOKEN)));
                uploadObject.setFileId(cursor.getLong(cursor.getColumnIndex(TransferDatabase.UploadColumns.FILE_ID)));
                uploadObject.setMd5(cursor.getString(cursor.getColumnIndex(TransferDatabase.UploadColumns.MD5)));
                uploadObject.setContext(cursor.getString(cursor.getColumnIndex(TransferDatabase.UploadColumns.CONTEXT)));
                return uploadObject;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return null;
    }
}
