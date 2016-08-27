package org.xjy.android.nova.transfer.upload.music;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Pair;

import org.xjy.android.nova.transfer.common.AbstractDao;
import org.xjy.android.nova.transfer.storage.TransferDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class UploadMusicDao extends AbstractDao {
	private static final UploadMusicDao sUploadMusicDao = new UploadMusicDao();

	private UploadMusicDao() {}
	
	public static UploadMusicDao getInstance() {
		return sUploadMusicDao;
	}

	@Override
	public SQLiteDatabase getDatabase() {
		return TransferDatabase.getInstance().getDatabase();
	}

	HashMap<String, UploadMusicObject> getUploadInfo(ArrayList<String> paths, long userId) {
		HashMap<String, UploadMusicObject> uploadInfo = new HashMap<>();
		Cursor cursor = null;
		try {
			String[] projection = new String[]{TransferDatabase.UploadMusicColumns.FILE_PATH, TransferDatabase.UploadMusicColumns.MD5, TransferDatabase.UploadMusicColumns.CHECK_ID, TransferDatabase.UploadMusicColumns.FILE_ID,
					TransferDatabase.UploadMusicColumns.SONG_ID, TransferDatabase.UploadMusicColumns.STATE};
			String sql = "SELECT " + TextUtils.join(",", projection) + " FROM " + TransferDatabase.UPLOAD_MUSIC_TABLE_NAME + " WHERE " + TransferDatabase.UploadMusicColumns.USER_ID + "=? AND "
					+ TransferDatabase.UploadMusicColumns.FILE_PATH + " IN (\"" + TextUtils.join("\",\"", paths) + "\")";
			cursor = mDatabase.rawQuery(sql, new String[]{userId + ""});
			while (cursor.moveToNext()) {
				UploadMusicObject uploadMusicObject = new UploadMusicObject();
				String path = cursor.getString(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.FILE_PATH));
				uploadMusicObject.setPath(path);
				uploadMusicObject.setMd5(cursor.getString(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.MD5)));
				uploadMusicObject.setCheckId(cursor.getString(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.CHECK_ID)));
				uploadMusicObject.setFileId(cursor.getLong(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.FILE_ID)));
				uploadMusicObject.setSongId(cursor.getLong(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.SONG_ID)));
				uploadMusicObject.setState(cursor.getInt(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.STATE)));
				uploadInfo.put(path, uploadMusicObject);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			closeCursorSilently(cursor);
		}
		return uploadInfo;
	}

	Pair<HashSet<Long>, HashSet<Long>> getUploadSongIds(long userId) {
		HashSet<Long> uploadedIds = new HashSet<>();
		HashSet<Long> transcodedIds = new HashSet<>();
		Pair<HashSet<Long>, HashSet<Long>> uploadSongIds = new Pair<>(uploadedIds, transcodedIds);
		Cursor cursor = null;
		try {
			String[] projection = new String[]{TransferDatabase.UploadMusicColumns.SONG_ID, TransferDatabase.UploadMusicColumns.STATE};
			String sql = "SELECT " + TextUtils.join(",", projection) + " FROM " + TransferDatabase.UPLOAD_MUSIC_TABLE_NAME + " WHERE " + TransferDatabase.UploadMusicColumns.USER_ID + "=? AND "
					+ TransferDatabase.UploadMusicColumns.STATE + " IN (?, ?)";
			cursor = mDatabase.rawQuery(sql, new String[]{userId + "", UploadMusicAgent.STATE_COMPLETED + "", UploadMusicAgent.STATE_TRANSCODE_COMPLETED + ""});
			while (cursor.moveToNext()) {
				if (cursor.getInt(1) == UploadMusicAgent.STATE_COMPLETED) {
					uploadedIds.add(cursor.getLong(0));
				} else {
					transcodedIds.add(cursor.getLong(0));
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			closeCursorSilently(cursor);
		}
		return uploadSongIds;
	}

	HashSet<String> getPathsBySongId(long songId, long userId) {
		HashSet<String> paths = new HashSet<>();
		Cursor cursor = null;
		try {
			String sql = "SELECT " + TransferDatabase.UploadMusicColumns.FILE_PATH + " FROM " + TransferDatabase.UPLOAD_MUSIC_TABLE_NAME + " WHERE " + TransferDatabase.UploadMusicColumns.SONG_ID + "=? AND "
					+ TransferDatabase.UploadMusicColumns.USER_ID + "=?";
			cursor = mDatabase.rawQuery(sql, new String[]{songId + "", userId + ""});
			while (cursor.moveToNext()) {
				paths.add(cursor.getString(0));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			closeCursorSilently(cursor);
		}
		return paths;
	}

	Pair<Integer, Integer> getUploadProgress(long userId) {
		int progress = 0;
		int max = 0;
		Cursor cursor = null;
		mDatabase.beginTransaction();
		try {
			String sql = "SELECT COUNT(*) FROM " + TransferDatabase.UPLOAD_MUSIC_TABLE_NAME + " WHERE " + TransferDatabase.UploadMusicColumns.USER_ID + "=? AND " + TransferDatabase.UploadMusicColumns.STATE;
			cursor = mDatabase.rawQuery(sql + "=?", new String[]{userId + "", UploadMusicAgent.STATE_COMPLETED + ""});
			while (cursor.moveToNext()) {
				progress = cursor.getInt(0);
			}
			cursor.close();
			cursor = mDatabase.rawQuery(sql + "<=?", new String[]{userId + "", UploadMusicAgent.STATE_COMPLETED + ""});
			while (cursor.moveToNext()) {
				max = cursor.getInt(0);
			}
			mDatabase.setTransactionSuccessful();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			closeCursorSilently(cursor);
			mDatabase.endTransaction();
		}
		return new Pair<>(progress, max);
	}

	int getCount(long userId) {
		int count = 0;
		Cursor cursor = null;
		try {
			cursor = mDatabase.rawQuery("SELECT COUNT(*) FROM " + TransferDatabase.UPLOAD_MUSIC_TABLE_NAME + " WHERE " + TransferDatabase.UploadMusicColumns.USER_ID + "=?", new String[]{userId + ""});
			while (cursor.moveToNext()) {
				count = cursor.getInt(0);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			closeCursorSilently(cursor);
		}
		return count;
	}

//	Pair<ArrayList<UploadMusicActivity.UploadMusicEntry>, HashMap<String, UploadMusicActivity.UploadMusicEntry>> getUploadMusicEntry(long userId) {
//		ArrayList<UploadMusicActivity.UploadMusicEntry> list = new ArrayList<>();
//		HashMap<String, UploadMusicActivity.UploadMusicEntry> map = new HashMap<>();
//		Pair<ArrayList<UploadMusicActivity.UploadMusicEntry>, HashMap<String, UploadMusicActivity.UploadMusicEntry>> pair = new Pair<>(list, map);
//		Cursor cursor = null;
//		try {
//			String[] projection = new String[]{TransferDatabase.UploadMusicColumns.FILE_PATH, TransferDatabase.UploadMusicColumns.NAME, TransferDatabase.UploadMusicColumns.ALBUM, TransferDatabase.UploadMusicColumns.ARTIST,
//					TransferDatabase.UploadMusicColumns.BITRATE, TransferDatabase.UploadMusicColumns.FILE_LENGTH, TransferDatabase.UploadMusicColumns.TIME,
//					TransferDatabase.UploadMusicColumns.STATE, TransferDatabase.UploadMusicColumns.FAIL_REASON};
//			String sql = "SELECT " + TextUtils.join(",", projection) + " FROM " + TransferDatabase.UPLOAD_MUSIC_TABLE_NAME + " WHERE " + TransferDatabase.UploadMusicColumns.USER_ID + "=?";
//			cursor = mDatabase.rawQuery(sql, new String[]{userId + ""});
//			UploadMusicAgent uploadMusicAgent = UploadMusicAgent.getInstance();
//			while (cursor.moveToNext()) {
//				UploadMusicObject uploadMusicObject = new UploadMusicObject();
//				String path = cursor.getString(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.FILE_PATH));
//				uploadMusicObject.setPath(path);
//				uploadMusicObject.setName(cursor.getString(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.NAME)));
//				uploadMusicObject.setAlbumName(cursor.getString(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.ALBUM)));
//				uploadMusicObject.setArtistName(cursor.getString(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.ARTIST)));
//				uploadMusicObject.setBitrate(cursor.getInt(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.BITRATE)));
//				uploadMusicObject.setFileLength(cursor.getLong(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.FILE_LENGTH)));
//				uploadMusicObject.setTime(cursor.getLong(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.TIME)));
//				uploadMusicObject.setState(uploadMusicAgent.getUploadMusicState(path, cursor.getInt(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.STATE)), userId));
//				uploadMusicObject.setFailReason(cursor.getInt(cursor.getColumnIndex(TransferDatabase.UploadMusicColumns.FAIL_REASON)));
//				UploadMusicActivity.UploadMusicEntry entry = new UploadMusicActivity.UploadMusicEntry(uploadMusicObject);
//				list.add(entry);
//				map.put(path, entry);
//			}
//		} catch (Throwable t) {
//			t.printStackTrace();
//		} finally {
//			closeCursorSilently(cursor);
//		}
//		return pair;
//	}

	int getUploadState(String path, long userId) {
		int state = UploadMusicAgent.STATE_ABSENT;
		Cursor cursor = null;
		try {
			String sql = "SELECT " + TransferDatabase.UploadMusicColumns.STATE + " FROM " + TransferDatabase.UPLOAD_MUSIC_TABLE_NAME + " WHERE " + TransferDatabase.UploadMusicColumns.FILE_PATH + "=? AND "
					+ TransferDatabase.UploadMusicColumns.USER_ID + "=?";
			cursor = mDatabase.rawQuery(sql, new String[]{path, userId + ""});
			while (cursor.moveToNext()) {
				state = cursor.getInt(0);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			closeCursorSilently(cursor);
		}
		return state;
	}

	int insertMusics(ArrayList<UploadMusicJob> jobs, long userId) {
		try {
			mDatabase.beginTransaction();
			try {
				for (int i = 0, size = jobs.size(); i < size; i++) {
					UploadMusicJob job = jobs.get(i);
					String path = job.getId();
					UploadMusicObject uploadMusicObject = job.getUploadMusicObject();
					ContentValues contentValues = new ContentValues();
					contentValues.put(TransferDatabase.UploadMusicColumns.FILE_PATH, path);
					contentValues.put(TransferDatabase.UploadMusicColumns.USER_ID, userId);
					contentValues.put(TransferDatabase.UploadMusicColumns.NAME, uploadMusicObject.getName());
					contentValues.put(TransferDatabase.UploadMusicColumns.ALBUM, uploadMusicObject.getAlbumName());
					contentValues.put(TransferDatabase.UploadMusicColumns.ARTIST, uploadMusicObject.getArtistName());
					contentValues.put(TransferDatabase.UploadMusicColumns.BITRATE, uploadMusicObject.getBitrate());
					contentValues.put(TransferDatabase.UploadMusicColumns.MD5, uploadMusicObject.getMd5());
					contentValues.put(TransferDatabase.UploadMusicColumns.CHECK_ID, uploadMusicObject.getCheckId());
					contentValues.put(TransferDatabase.UploadMusicColumns.FILE_ID, uploadMusicObject.getFileId());
					contentValues.put(TransferDatabase.UploadMusicColumns.SONG_ID, uploadMusicObject.getSongId());
					contentValues.put(TransferDatabase.UploadMusicColumns.FILE_LENGTH, new File(path).length());
					contentValues.put(TransferDatabase.UploadMusicColumns.TIME, System.currentTimeMillis());
					contentValues.put(TransferDatabase.UploadMusicColumns.STATE, UploadMusicAgent.STATE_WAIT);
					mDatabase.insertWithOnConflict(TransferDatabase.UPLOAD_MUSIC_TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
				}
				mDatabase.setTransactionSuccessful();
			} finally {
				mDatabase.endTransaction();
			}
			return 1;
		} catch (Throwable t) {
			handleException(t);
			t.printStackTrace();
		}
		return -1;
	}

	int deleteMusics(HashSet<String> paths, long userId) {
		try {
			return mDatabase.delete(TransferDatabase.UPLOAD_MUSIC_TABLE_NAME, TransferDatabase.UploadMusicColumns.FILE_PATH + " IN (\"" + TextUtils.join("\",\"", paths) + "\") AND "
					+ TransferDatabase.UploadMusicColumns.USER_ID + "=?", new String[]{userId + ""});
		} catch (Throwable t) {
			handleException(t);
			t.printStackTrace();
		}
		return -1;
	}

	int deleteBySongId(long songId, long userId) {
		try {
			mDatabase.delete(TransferDatabase.UPLOAD_MUSIC_TABLE_NAME, TransferDatabase.UploadMusicColumns.SONG_ID + "=? AND " + TransferDatabase.UploadMusicColumns.USER_ID + "=?", new String[]{songId + "", userId + ""});
			return 1;
		} catch (Throwable t) {
			handleException(t);
			t.printStackTrace();
		}
		return 0;
	}

	int updateState(HashSet<String> paths, int state, int failReason, long userId) {
		try {
			ContentValues cv = new ContentValues();
			cv.put(TransferDatabase.UploadMusicColumns.STATE, state);
			if (failReason >= 0) {
				cv.put(TransferDatabase.UploadMusicColumns.FAIL_REASON, failReason);
			}
			return mDatabase.update(TransferDatabase.UPLOAD_MUSIC_TABLE_NAME, cv, TransferDatabase.UploadMusicColumns.FILE_PATH + " IN (\"" + TextUtils.join("\",\"", paths) + "\") AND "
					+ TransferDatabase.UploadMusicColumns.USER_ID + "=?", new String[]{userId + ""});
		} catch (Throwable t) {
			handleException(t);
			t.printStackTrace();
		}
		return -1;
	}

	int updateMD5(String path, String md5, long userId) {
		try {
			ContentValues cv = new ContentValues();
			cv.put(TransferDatabase.UploadMusicColumns.MD5, md5);
			return mDatabase.update(TransferDatabase.UPLOAD_MUSIC_TABLE_NAME, cv, TransferDatabase.UploadMusicColumns.FILE_PATH + "=? AND " + TransferDatabase.UploadMusicColumns.USER_ID + "=?", new String[]{path, userId + ""});
		} catch (Throwable t) {
			handleException(t);
			t.printStackTrace();
		}
		return -1;
	}

	int updateCheckIdAndFileId(String path, String checkId, long fileId, long userId) {
		try {
			ContentValues cv = new ContentValues();
			cv.put(TransferDatabase.UploadMusicColumns.CHECK_ID, checkId);
			cv.put(TransferDatabase.UploadMusicColumns.FILE_ID, fileId);
			return mDatabase.update(TransferDatabase.UPLOAD_MUSIC_TABLE_NAME, cv, TransferDatabase.UploadMusicColumns.FILE_PATH + "=? AND " + TransferDatabase.UploadMusicColumns.USER_ID + "=?", new String[]{path, userId + ""});
		} catch (Throwable t) {
			handleException(t);
			t.printStackTrace();
		}
		return -1;
	}

	int updateState(String path, int state, int failReason, long userId) {
		try {
			ContentValues cv = new ContentValues();
			cv.put(TransferDatabase.UploadMusicColumns.STATE, state);
			if (failReason != 0) {
				cv.put(TransferDatabase.UploadMusicColumns.FAIL_REASON, failReason);
			}
			if (failReason == UploadMusicAgent.FAIL_FILE_MODIFIED) {
				cv.put(TransferDatabase.UploadMusicColumns.MD5, (String) null);
				cv.put(TransferDatabase.UploadMusicColumns.CHECK_ID, (String) null);
			}
			return mDatabase.update(TransferDatabase.UPLOAD_MUSIC_TABLE_NAME, cv, TransferDatabase.UploadMusicColumns.FILE_PATH + "=? AND " + TransferDatabase.UploadMusicColumns.USER_ID + "=?", new String[]{path, userId + ""});
		} catch (Throwable t) {
			handleException(t);
			t.printStackTrace();
		}
		return -1;
	}

	int updateFileId(String path, long fileId, long userId) {
		try {
			ContentValues cv = new ContentValues();
			cv.put(TransferDatabase.UploadMusicColumns.FILE_ID, fileId);
			return mDatabase.update(TransferDatabase.UPLOAD_MUSIC_TABLE_NAME, cv, TransferDatabase.UploadMusicColumns.FILE_PATH + "=? AND " + TransferDatabase.UploadMusicColumns.USER_ID + "=?", new String[]{path, userId + ""});
		} catch (Throwable t) {
			handleException(t);
			t.printStackTrace();
		}
		return -1;
	}

	int updateUploadSongIdAndState(String path, long uploadSongId, int state, long userId) {
		try {
			ContentValues cv = new ContentValues();
			cv.put(TransferDatabase.UploadMusicColumns.SONG_ID, uploadSongId);
			cv.put(TransferDatabase.UploadMusicColumns.STATE, state);
			return mDatabase.update(TransferDatabase.UPLOAD_MUSIC_TABLE_NAME, cv, TransferDatabase.UploadMusicColumns.FILE_PATH + "=? AND " + TransferDatabase.UploadMusicColumns.USER_ID + "=?", new String[]{path, userId + ""});
		} catch (Throwable t) {
			handleException(t);
			t.printStackTrace();
		}
		return -1;
	}

	Pair<HashSet<String>, HashSet<String>> updateTranscodeState(HashSet<Long> successIds, HashSet<Long> failedIds, long userId) {
		HashSet<String> successPaths = new HashSet<>();
		HashSet<String> failedPaths = new HashSet<>();
		Pair<HashSet<String>, HashSet<String>> paths = new Pair<>(successPaths, failedPaths);
		try {
			Cursor cursor = null;
			mDatabase.beginTransaction();
			try {
				String whereClause = TransferDatabase.UploadMusicColumns.SONG_ID + " IN (" + TextUtils.join(",", successIds) + ") AND " + TransferDatabase.UploadMusicColumns.USER_ID + "=?";
				String sql = "SELECT " + TransferDatabase.UploadMusicColumns.FILE_PATH + " FROM " + TransferDatabase.UPLOAD_MUSIC_TABLE_NAME + " WHERE ";
				cursor = mDatabase.rawQuery(sql + whereClause, new String[]{userId + ""});
				while (cursor.moveToNext()) {
					successPaths.add(cursor.getString(0));
				}
				cursor.close();
				ContentValues cv = new ContentValues();
				cv.put(TransferDatabase.UploadMusicColumns.STATE, UploadMusicAgent.STATE_TRANSCODE_COMPLETED);
				mDatabase.update(TransferDatabase.UPLOAD_MUSIC_TABLE_NAME, cv, whereClause, new String[]{userId + ""});
				whereClause = TransferDatabase.UploadMusicColumns.SONG_ID + " IN (" + TextUtils.join(",", failedIds) + ") AND " + TransferDatabase.UploadMusicColumns.USER_ID + "=?";
				cursor = mDatabase.rawQuery(sql + whereClause, new String[]{userId + ""});
				while (cursor.moveToNext()) {
					failedPaths.add(cursor.getString(0));
				}
				cursor.close();
				cv = new ContentValues();
				cv.put(TransferDatabase.UploadMusicColumns.STATE, UploadMusicAgent.STATE_TRANSCODE_FAILED);
				mDatabase.update(TransferDatabase.UPLOAD_MUSIC_TABLE_NAME, cv, whereClause, new String[]{userId + ""});
				mDatabase.setTransactionSuccessful();
			} finally {
				closeCursorSilently(cursor);
				mDatabase.endTransaction();
			}
		} catch (Throwable t) {
			handleException(t);
			t.printStackTrace();
		}
		return paths;
	}
}
