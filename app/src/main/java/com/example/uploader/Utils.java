package com.example.uploader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * @author KeishinYokomaku
 */
public final class Utils {
	public static String API_URL = "https://amber-torch-5086.firebaseio.com/";

	private Utils() {
		throw new AssertionError("don't use ctor");
	}

	public static void close(Cursor cursor) {
		if (cursor == null)
			return;
		cursor.close();
	}

	public static void close(SQLiteDatabase db) {
		if (db == null)
			return;
		db.close();
	}

	public static void endTransaction(SQLiteDatabase db) {
		if (db == null)
			return;
		db.endTransaction();
	}

	public static void notifyChange(Context context, Uri uri) {
		if (context == null)
			return;
		ContentResolver resolver = context.getContentResolver();
		resolver.notifyChange(uri, null);
	}
}
