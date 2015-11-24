package com.example.uploader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author KeishinYokomaku
 */
public class PickedPhotoDbHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "picked_photo.db";
	public static final int VERSION = 2;

	public PickedPhotoDbHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		create(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		drop(db);
		create(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		drop(db);
		create(db);
	}

	private void create(SQLiteDatabase db) {
		db.execSQL(PickedPhotoScheme.STATEMENT_CREATE);
	}

	public void drop(SQLiteDatabase db) {
		db.execSQL(PickedPhotoScheme.STATEMENT_DROP);
	}
}
