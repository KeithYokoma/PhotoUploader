package com.example.uploader;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PickedPhotoProvider extends ContentProvider {
	public static final String AUTHORITY = "com.example.uploader";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PickedPhotoScheme.TABLE_NAME);
	public static final int TYPE_URIS = 1;
	public static final int TYPE_URI = 2;
	public static final String CONTENT_TYPE_URIS = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.example.picked";
	public static final String CONTENT_TYPE_URI = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.example.picked";
	private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private SQLiteOpenHelper mHelper;

	static {
		MATCHER.addURI(AUTHORITY, PickedPhotoScheme.TABLE_NAME, TYPE_URIS);
		MATCHER.addURI(AUTHORITY, PickedPhotoScheme.TABLE_NAME + "/#", TYPE_URI);
	}

	/**
	 * この ContentProvider を初期化します。
	 * @return true
	 */
	@Override
	public boolean onCreate() {
		mHelper = new PickedPhotoDbHelper(getContext()); // データベースを管理するオブジェクトを初期化
		return true;
	}

	/**
	 * データベースにデータを問い合わせます。
	 * @param uri 問い合わせるデータベース、テーブル、id を示す Uri。
	 * @param projection どの行を結果として返して欲しいかを指定する
	 * @param selection 条件式
	 * @param selectionArgs 条件のプレースホルダに代入する実際の値
	 * @param sortOrder 結果のソート順
	 * @return 結果の一覧
	 */
	@Nullable
	@Override
	public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			String newSelection = buildSelection(uri, selection);
			builder.setTables(PickedPhotoScheme.TABLE_NAME);
			db = mHelper.getReadableDatabase();
			c = builder.query(db, projection, newSelection, selectionArgs, null, null, sortOrder);
			if (c != null && getContext() != null) {
				c.setNotificationUri(getContext().getContentResolver(), uri);
			}
			return c;
		} catch (RuntimeException e) {
			Utils.close(c);
			Utils.close(db);
			throw e;
		}
	}

	/**
	 * Uri で示されるデータがどのような種類のデータかを返します。
	 * @param uri 調べたい Uri
	 * @return データの種類を表す文字列。{@link PickedPhotoProvider#CONTENT_TYPE_URI} か {@link PickedPhotoProvider#CONTENT_TYPE_URIS} のいずれか。
	 * @throws IllegalArgumentException この ContentProvider では取り扱っていない種類のデータの場合。
	 */
	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		switch (MATCHER.match(uri)) {
			case TYPE_URI:
				return CONTENT_TYPE_URI;
			case TYPE_URIS:
				return CONTENT_TYPE_URIS;
			default:
				throw new IllegalArgumentException("unknown uri: " + uri);
		}
	}

	/**
	 * Uri で示されるデータベース・テーブルに対して、values のもつデータを追加します
	 * @param uri データを追加したいデータベース・テーブルの Uri
	 * @param values 追加したいデータ
	 * @return 追加されたデータを示す Uri
	 */
	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		if (MATCHER.match(uri) != TYPE_URIS) {
			throw new IllegalArgumentException("unknown uri: " + uri);
		}
		SQLiteDatabase db = null;
		try {
			db = mHelper.getWritableDatabase();
			db.beginTransaction();
			long id = db.insert(PickedPhotoScheme.TABLE_NAME, null, values);
			Uri inserted = ContentUris.withAppendedId(CONTENT_URI, id);
			Utils.notifyChange(getContext(), inserted);
			db.setTransactionSuccessful();
			return inserted;
		} finally {
			Utils.endTransaction(db);
		}
	}

	/**
	 * 条件にあうデータを Uri で示されるデータベース・テーブルから削除します
	 * @param uri 削除したいデータを持っているデータベース・テーブルの Uri
	 * @param selection 条件式
	 * @param selectionArgs 条件のプレースホルダに代入する実際の値
	 * @return 削除された件数
	 */
	@Override
	public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		if (MATCHER.match(uri) != TYPE_URI) {
			throw new IllegalArgumentException("unknown uri: " + uri);
		}
		SQLiteDatabase db = null;
		try {
			db = mHelper.getWritableDatabase();
			db.beginTransaction();
			String newSelection = buildSelection(uri, selection);
			int count = db.delete(PickedPhotoScheme.TABLE_NAME, newSelection, selectionArgs);
			Utils.notifyChange(getContext(), CONTENT_URI);
			db.setTransactionSuccessful();
			return count;
		} finally {
			Utils.endTransaction(db);
		}
	}

	/**
	 * Uri で示されるデータベース・テーブルの中にある条件にあうデータを更新します
	 * @param uri 更新したいデータを持っているデータベース・テーブルの Uri
	 * @param values 更新後のデータ
	 * @param selection 条件式
	 * @param selectionArgs 条件のプレースホルダに代入する実際の値
	 * @return 更新された件数
	 */
	@Override
	public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = null;
		try {
			db = mHelper.getWritableDatabase();
			db.beginTransaction();
			String newSelection = buildSelection(uri, selection);
			int count = db.update(PickedPhotoScheme.TABLE_NAME, values, newSelection, selectionArgs);
			Utils.notifyChange(getContext(), CONTENT_URI);
			db.setTransactionSuccessful();
			return count;
		} finally {
			Utils.endTransaction(db);
		}
	}

	private String buildSelection(Uri uri, String selection) {
		long id;
		String additionalSelection = null;

		switch (MATCHER.match(uri)) {
			case TYPE_URI:
				id = ContentUris.parseId(uri);
				additionalSelection = PickedPhotoScheme._ID + " = " + id;
				break;
			case TYPE_URIS:
				// do nothing
				break;
			default:
				throw new IllegalArgumentException("unknown uri: " + uri);
		}

		if (additionalSelection == null) {
			return selection;
		}
		if (selection == null) {
			return additionalSelection;
		}
		return additionalSelection + " AND " + selection;
	}
}
