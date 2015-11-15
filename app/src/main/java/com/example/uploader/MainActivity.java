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
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static String TAG = "MainActivity";
    private static final int REQUEST_CODE_PICK_PHOTO = 1;
    private PickedPhotoAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser user = ParseUser.getCurrentUser();
        if (user==null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else {
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
            }
        }
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

    // Uri を ContentProvider に登録するメソッド
    private void insertUri(Uri uri) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(PickedPhotoScheme.COLUMN_URI, uri.toString());
        resolver.insert(PickedPhotoProvider.CONTENT_URI, values);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Settings を選択したらlSettingActivityを表示する
            Intent intent = new android.content.Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_logout){
            ParseUser.logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
