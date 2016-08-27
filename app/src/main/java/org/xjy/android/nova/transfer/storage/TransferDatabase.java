package org.xjy.android.nova.transfer.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.xjy.android.nova.NovaApplication;

public class TransferDatabase extends SQLiteOpenHelper {
    //upload_music table
    public static final String UPLOAD_MUSIC_TABLE_NAME = "upload_music";
    public static final class UploadMusicColumns implements BaseColumns {
        public static final String FILE_PATH = "file_path";
        public static final String USER_ID = "user_id";
        public static final String NAME = "name";
        public static final String ALBUM = "album";
        public static final String ARTIST = "artist";
        public static final String BITRATE = "bitrate";
        public static final String MD5 = "md5";
        public static final String CHECK_ID = "check_id";
        public static final String FILE_ID = "file_id";
        public static final String SONG_ID = "song_id";
        public static final String FILE_LENGTH = "file_length";
        public static final String TIME = "time";
        public static final String STATE = "state";
        public static final String FAIL_REASON = "fail_reason";
    }
    public static final String CREATE_UPLOAD_MUSIC_TABLE = "CREATE TABLE IF NOT EXISTS " + UPLOAD_MUSIC_TABLE_NAME + " (" + UploadMusicColumns.FILE_PATH + " VARCHAR , " + UploadMusicColumns.USER_ID + " INTEGER, "
            + UploadMusicColumns.NAME + " VARCHAR, " + UploadMusicColumns.ALBUM + " VARCHAR, " + UploadMusicColumns.ARTIST + " VARCHAR, " + UploadMusicColumns.BITRATE + " INTEGER, " + UploadMusicColumns.MD5 + " VARCHAR, "
            + UploadMusicColumns.CHECK_ID + " VARCHAR, " + UploadMusicColumns.FILE_ID + " INTEGER, " + UploadMusicColumns.SONG_ID + " INTEGER, " + UploadMusicColumns.FILE_LENGTH + " INTEGER, "
            + UploadMusicColumns.TIME + " INTEGER, " + UploadMusicColumns.STATE + " INTEGER, " + UploadMusicColumns.FAIL_REASON + " INTEGER, PRIMARY KEY(" + UploadMusicColumns.FILE_PATH + ", " + UploadMusicColumns.USER_ID + "))";

    //upload table
    public static final String UPLOAD_TABLE_NAME = "upload";
    public static final class UploadColumns implements BaseColumns {
        public static final String FILE_PATH = "file_path";
        public static final String BUCKET_NAME = "bucket_name";
        public static final String OBJECT_NAME = "object_name";
        public static final String TOKEN = "token";
        public static final String FILE_ID = "file_id";
        public static final String MD5 = "md5";
        public static final String CONTEXT = "context";
    }
    public static final String CREATE_UPLOAD_TABLE = "CREATE TABLE IF NOT EXISTS " + UPLOAD_TABLE_NAME + " (" + UploadColumns.FILE_PATH + " VARCHAR PRIMARY KEY, "
            + UploadColumns.BUCKET_NAME + " VARCHAR, " + UploadColumns.OBJECT_NAME + " VARCHAR, " + UploadColumns.TOKEN + " VARCHAR, " + UploadColumns.FILE_ID + " INTEGER, "
            + UploadColumns.MD5 + " VARCHAR, " + UploadColumns.CONTEXT + " VARCHAR)";

    //post_status table
    public static final String POST_STATUS_TABLE_NAME = "post_status";
    public static final class PostStatusColumns implements BaseColumns {
        public static final String UID = "uid";
        public static final String STATUS = "status";
        public static final String RESOURCE = "resource";
        public static final String PIC_PATH= "pic_path";
        public static final String TAG_ID = "tag_id";
        public static final String TAG_NAME = "tag_name";
        public static final String SYNC_TARGETS = "sync_targets";
        public static final String PIC_INFO = "pic_info";
        public static final String TIME = "time";
    }
    public static final String CREATE_POST_STATUS_TABLE = "CREATE TABLE IF NOT EXISTS " + POST_STATUS_TABLE_NAME + " (" + PostStatusColumns._ID + " VARCHAR PRIMARY KEY, "
            + PostStatusColumns.UID + " INTEGER, " + PostStatusColumns.STATUS + " VARCHAR, " + PostStatusColumns.RESOURCE + " VARCHAR, " + PostStatusColumns.PIC_PATH + " VARCHAR, "
            + PostStatusColumns.TAG_ID + " INTEGER, " + PostStatusColumns.TAG_NAME + " VARCHAR, " + PostStatusColumns.SYNC_TARGETS + " VARCHAR, " + PostStatusColumns.PIC_INFO + " VARCHAR, "
            + PostStatusColumns.TIME + " INTEGER)";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static final TransferDatabase sTransferDatabase = new TransferDatabase(NovaApplication.getInstance());

    private SQLiteDatabase mDatabase;

    private TransferDatabase(Context context) {
        super(context, "transfer.db", null, 2);
        mDatabase = getWritableDatabase();
    }

    public static TransferDatabase getInstance() {
        return sTransferDatabase;
    }

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_UPLOAD_MUSIC_TABLE);
        db.execSQL(CREATE_UPLOAD_TABLE);
        db.execSQL(CREATE_POST_STATUS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        while (oldVersion++ < newVersion) {
            switch (oldVersion) {
                case 2:
                    db.execSQL(CREATE_POST_STATUS_TABLE);
                    break;
            }
        }
    }
}
