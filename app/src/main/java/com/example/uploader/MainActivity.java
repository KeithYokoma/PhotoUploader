package com.example.uploader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int REQUEST_CODE_PICK_PHOTO = 1;
	private PickedPhotoAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		GridView grid = (GridView) findViewById(R.id.photo_grid);
		mAdapter = new PickedPhotoAdapter(this);
		grid.setAdapter(mAdapter);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) { // floating action button をおした時、画像の選択画面を起動する
				Intent intent = new Intent(Intent.ACTION_PICK); // TODO (実習1) 選択画面を開く為の Intent の Action を追加しよう
				intent.setType("image/*"); // 画像のみを選択できるようにします
				// TODO (実習1) 上記の Intent を使って、選択した画像を取得するための新しい画面を呼びだそう
				startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
			}
		});
		getSupportLoaderManager().initLoader(0, null, this); // 保存した画像の読込みを始める
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); // 必ず呼ぶようにします

		if (requestCode == REQUEST_CODE_PICK_PHOTO) {
			if (resultCode == RESULT_OK) { // TODO (実習1) この if 文の条件を、resultCode が RESULT_OK かどうかチェックするように書き換えよう
				Uri uri = data.getData(); // TODO (実習1) uri を、data から取り出すように書き換えよう
				Toast.makeText(getApplicationContext(), uri.toString(), Toast.LENGTH_SHORT).show();
				// TODO (実習2) 取得した Uri を ContentProvider に登録しよう
				insertUri(uri);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, PickedPhotoProvider.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO (実習2) PickedPhotoArrayAdapter を PickedPhotoCursorAdapter に変更し終わったら、data を mAdapter にセットしよう
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	// Uri を ContentProvider に登録するメソッド
	private void insertUri(Uri uri) {
		ContentResolver resolver = getContentResolver();
		ContentValues values = new ContentValues();
		values.put(PickedPhotoScheme.COLUMN_URI, uri.toString());
		// TODO (実習2) ContentResolver を使って、PickedPhotoProvider.CONTENT_URI に values を追加しよう
		resolver.insert(PickedPhotoProvider.CONTENT_URI, values);
	}
}
