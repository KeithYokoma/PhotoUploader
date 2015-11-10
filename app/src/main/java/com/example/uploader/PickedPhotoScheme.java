package com.example.uploader;

import android.provider.BaseColumns;

/**
 * @author KeishinYokomaku
 */
public final class PickedPhotoScheme implements BaseColumns {
	public static final String TABLE_NAME = "phicked_uris";
	public static final String COLUMN_URI = "uri";
	public static final String STATEMENT_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
			_ID + " INTEGER NOT NULL PRIMARY KEY," +
			COLUMN_URI + " TEXT NOT NULL)";
	public static final String STATEMENT_DROP = "DROP TABLE " + TABLE_NAME;

	private PickedPhotoScheme() {
		throw new AssertionError("don't use ctor");
	}
}
