package com.example.uploader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * @author KeishinYokomaku
 */
public class PickedPhotoAdapter extends CursorAdapter {
	public static final String TAG = PickedPhotoAdapter.class.getSimpleName();

	public PickedPhotoAdapter(Context context) {
		super(context, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return inflater.inflate(R.layout.item_photo, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView photo = (ImageView) view.findViewById(R.id.photo);
		Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(PickedPhotoScheme.COLUMN_URI)));
		Picasso.with(context).load(uri).resizeDimen(R.dimen.image_size, R.dimen.image_size).centerInside().into(photo);
	}
}
