package com.example.uploader;

import android.app.Notification;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static String TAG = "MainActivity";
    private static final int REQUEST_CODE_PICK_PHOTO = 1;
    private static final int UPLOAD_NOTIFICATION_ID = 1;
    private PickedPhotoAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Welcome! " + user.getUsername(), Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onOptionsItemSelected(menuItem);
            }
        });

        GridView grid = (GridView) findViewById(R.id.photo_grid);
        mAdapter = new PickedPhotoAdapter(this);
        grid.setAdapter(mAdapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Grid のアイテムをタップした時のイベントハンドラ
                Cursor cursor = (Cursor) parent.getItemAtPosition(position); // Adapter から選択された場所の Cursor を取得する
                String photoObjectId = cursor.getString(cursor.getColumnIndex(PickedPhotoScheme.COLUMN_PHOTO_OBJECT_ID)); // 写真のオブジェクトを見つけるために保存した objectId を取り出す
                // TODO (実習) photo から、photoObjectId に一致する ParseObject を取得する
                ParseQuery.getQuery("photo")
                        .findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e == null && !objects.isEmpty()) {
                                    ParseObject photo = objects.get(0);
                                    ParseObject object = new ParseObject("Like");
                                    // TODO (実習) Like するユーザのオブジェクトと、Like する写真のオブジェクトを object に追加しよう
                                    object.put("who", null); // TODO ユーザ
                                    object.put("target_photo", null); // TODO 写真
                                    object.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Toast.makeText(getApplicationContext(), "Liked!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Log.e(TAG, "error while putting like.", e);
                                }
                            }
                        });
            }
        });
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position); // Adapter から選択された場所の Cursor を取得する
                String photoObjectId = cursor.getString(cursor.getColumnIndex(PickedPhotoScheme.COLUMN_PHOTO_OBJECT_ID)); // 写真のオブジェクトを見つけるために保存した objectId を取り出す
                ParseQuery.getQuery("photo")
                        .whereEqualTo("objectId", photoObjectId)
                        .findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e == null && !objects.isEmpty()) {
                                    ParseObject photo = objects.get(0);
                                    // TODO who カラムを含むよう ParseQuery を作る
                                    ParseQuery.getQuery("Like")
                                            .whereEqualTo("target_photo", photo)
                                            .findInBackground(new FindCallback<ParseObject>() {
                                                @Override
                                                public void done(List<ParseObject> objects, ParseException e) {
                                                    if (e == null) {

                                                    } else {
                                                        Log.e(TAG, "error while reading like data", e);
                                                    }
                                                }
                                            });
                                } else {
                                    Log.e(TAG, "error while reading data", e);
                                }
                            }
                        });
                return false;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // floating action button をおした時、画像の選択画面を起動する
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*"); // 画像のみを選択できるようにします
                startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
            }
        });

        getSupportLoaderManager().initLoader(0, null, this); // 保存した画像の読込みを始める
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // 必ず呼ぶようにします

        if (requestCode == REQUEST_CODE_PICK_PHOTO) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Toast.makeText(getApplicationContext(), uri.toString(), Toast.LENGTH_SHORT).show();
                insertUri(uri);
                uploadToParse(uri);
            }
        }
    }

    private void uploadToParse(final Uri uri) {
        final ParseFile parseFile = createParseFile(this, uri);
        if (parseFile == null) return;
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "upload fail!!!!!", e);
                    return;
                }
                Log.v(TAG, "upload done!!!!!");
                final ParseObject obj = new ParseObject("photo");
                // Userを取得
                obj.put("uploadUserId", ParseUser.getCurrentUser().getObjectId());
                obj.put("file", parseFile);
                obj.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        addObjectId(uri, obj);
                    }
                });
                NotificationManagerCompat nm = NotificationManagerCompat.from(MainActivity.this);
                Notification n = new NotificationCompat.Builder(MainActivity.this)
                        .setTicker("Uploading photo...")
                        .setContentTitle("Uploaded!")
                        .setContentText("Photo has been uploaded.")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true)
                        .build();
                nm.notify(UPLOAD_NOTIFICATION_ID, n);
            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer percentDone) {
                NotificationManagerCompat nm = NotificationManagerCompat.from(MainActivity.this);
                Notification n = new NotificationCompat.Builder(MainActivity.this)
                        .setTicker("Uploading photo...")
                        .setContentTitle("Uploading photo in progress")
                        .setProgress(100, percentDone, false)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setOngoing(true)
                        .build();
                nm.notify(UPLOAD_NOTIFICATION_ID, n);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, PickedPhotoProvider.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void addObjectId(Uri uri, ParseObject object) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(PickedPhotoScheme.COLUMN_PHOTO_OBJECT_ID, object.getObjectId());
        int count = resolver.update(PickedPhotoProvider.CONTENT_URI, values, PickedPhotoScheme.COLUMN_URI + " = ?", new String[] {uri.toString()});
        if (count > 0) {
            Toast.makeText(getApplicationContext(), "updated", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "no update", Toast.LENGTH_LONG).show();
        }
    }

    // Uri を ContentProvider に登録するメソッド
    private void insertUri(Uri uri) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(PickedPhotoScheme.COLUMN_URI, uri.toString());
        resolver.insert(PickedPhotoProvider.CONTENT_URI, values);
    }

    private ParseFile createParseFile(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        String[] columns = {MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        FileInputStream fis = null;
        try {
            cursor = resolver.query(uri, columns, null, null, null);
            if (cursor == null) {
                return null;
            }
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            File file = new File(path);

            byte[] data = new byte[(int) file.length()];
            fis = new FileInputStream(file);
            int readLength = fis.read(data);
            if (readLength != data.length) {
                Log.e(TAG, "read fail");
                return null;
            }
            return new ParseFile(file.getName(), data);
        } catch (IOException e) {
            Log.e(TAG, "fail create parse file", e);
        } finally {
            if (cursor != null) cursor.close();
            try {
                if (fis != null) fis.close();
            } catch (IOException ignored) {
            }
        }

        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Settings を選択したらlSettingActivityを表示する
            Intent intent = new android.content.Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            ParseUser.logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
