package com.example.uploader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private String TAG = "SearchActivity";

    // 現在表示している写真を入れるリスト
    private List<ParseObject> mPhotoList = new ArrayList<>();
    private SearchPhotoAdapter mPhotoAdapter;

    // 現在表示しているユーザーを入れるリスト
    private List<ParseObject> mUserList = new ArrayList<>();
    private UserAdapter mUserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // ToolbarにSpinnerを追加します
        View customView = getLayoutInflater().inflate(R.layout.toolbar_spinner, null, false);
        toolbar.addView(customView);
        // SpinnerにAdapterやOnItemSelectedListenerを設定します
        Spinner spinner = (Spinner) customView.findViewById(R.id.spinner);
        mUserAdapter = new UserAdapter(this, mUserList);
        spinner.setAdapter(mUserAdapter);
        spinner.setOnItemSelectedListener(new SpinnerItemSelectedListener());

        // GridViewにAdapterやOnItemClickListenerを設定します
        GridView gridView = (GridView) findViewById(R.id.grid_view);
        mPhotoAdapter = new SearchPhotoAdapter(this, mPhotoList);
        gridView.setAdapter(mPhotoAdapter);
        gridView.setOnItemClickListener(new PhotoItemClickListener());

        // TODO 1-3 ユーザー一覧を取得し、Spinnerに変更を反映しましょう
        ParseQuery.getQuery("_User").findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects == null) return;
                mUserList.addAll(objects);
                mUserAdapter.notifyDataSetChanged();
            }
        });
    }

    // ユーザーを選択した時の動作を定義するクラス
    private class SpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            // TODO 2-1 選択されたユーザーの写真一覧を取得し、GridViewを更新します。

            ParseObject object = (ParseObject) parent.getItemAtPosition(position);
            String userId = object.getObjectId();

            ParseQuery.getQuery("photo")
            .whereEqualTo("uploadUserId", userId)
            .findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects == null) {
                        return;
                    }
                    mPhotoList.clear();
                    mPhotoList.addAll(objects);
                    mPhotoAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    // 写真をタップした時の動作を定義するクラス
    private class PhotoItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final ParseObject photo = (ParseObject) parent.getItemAtPosition(position);

            // TODO 3-1 タップした写真に対してアクションを選択するAlertDialogを表示します

            AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
            builder.setItems(new String[]{"Like", "Show users who liked", "delete"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        putLike(photo);
                    }
                    if (which == 1) {
                        showLikedUsers(photo);
                    }
                    if (which == 2) {
                        deletePhoto(photo);
                    }
                }
            });
            builder.show();
        }
    }

    private void putLike(ParseObject photo) {
        // photo に Likeします
        ParseObject object = new ParseObject("Like");
        object.put("who", ParseUser.getCurrentUser());
        object.put("target_photo", photo);
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "Liked!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showLikedUsers(ParseObject photo) {
        // photo に Likeした人の一覧を表示します
        ParseQuery.getQuery("Like")
                .whereEqualTo("target_photo", photo)
                .include("who")
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            List<String> names = new ArrayList<>();
                            for (ParseObject obj : objects) {
                                names.add(obj.getParseObject("who").getString("username"));
                            }
                            Toast.makeText(getApplicationContext(), TextUtils.join(", ", names), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "error while reading like data", e);
                        }
                    }
                });
    }

    private void deletePhoto(ParseObject photo) {
        // photo を 消します
        photo.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "deleted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "error while deleting", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // GridViewの写真アイテムのAdapter
    private static class SearchPhotoAdapter extends ArrayAdapter<ParseObject> {
        public SearchPhotoAdapter(Context context, List<ParseObject> objects) {
            super(context, R.layout.item_photo, R.id.label, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            ParseObject object = getItem(position);
            ParseFile file = object.getParseFile("file");
            file.getUrl();

            ImageView imageView = (ImageView) view.findViewById(R.id.photo);

            Picasso.with(getContext())
                    .load(file.getUrl())
                    .resizeDimen(R.dimen.image_size, R.dimen.image_size)
                    .centerInside()
                    .into(imageView);

            return view;
        }
    }

    // ユーザー一覧のSpinnerのAdapter
    private static class UserAdapter extends ArrayAdapter<ParseObject> {
        public UserAdapter(Context context, List<ParseObject> objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView text = (TextView) view.findViewById(android.R.id.text1);

            // TODO 1-1 text に ユーザー名(username)をsetTextしましょう
            ParseObject object = getItem(position);
            text.setText(object.getString("username"));
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            // TODO 1-2 getViewと同じ処理を行いましょう
            return getView(position, convertView, parent);
        }
    }

}
