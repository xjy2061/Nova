package org.xjy.android.nova.transfer.common;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.os.Handler;
import android.os.Looper;

import org.xjy.android.nova.R;
import org.xjy.android.nova.utils.PromptUtils;

public abstract class AbstractDao {
    protected SQLiteDatabase mDatabase;
    private Handler mHandler;

    public AbstractDao() {
        mDatabase = getDatabase();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public abstract SQLiteDatabase getDatabase();

    protected void closeCursorSilently(Cursor cursor) {
        try {
            cursor.close();
        } catch (Throwable t) { }
    }

    protected void handleException(final Throwable t) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (t instanceof SQLiteFullException) {
                    PromptUtils.showToast(R.string.internal_storage_full);
                } else {
                    PromptUtils.showToast(R.string.database_operation_failed);
                }
            }
        });
    }
}
