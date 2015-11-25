package com.example.uploader;

import android.content.Context;
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

    private List<ParseObject> mPhotoList = new ArrayList<>();
    private SearchPhotoAdapter mPhotoAdapter;

    private List<ParseObject> mUserList = new ArrayList<>();
    private UserAdapter mUserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        mUserAdapter = new UserAdapter(this, mUserList);
        View customView = getLayoutInflater().inflate(R.layout.toolbar_spinner, null, false);
        toolbar.addView(customView);
        Spinner spinner = (Spinner) customView.findViewById(R.id.spinner);
        spinner.setAdapter(mUserAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

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
        });
        ParseQuery.getQuery("_User").findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects == null) return;
                mUserList.clear();
                mUserList.addAll(objects);
                mUserAdapter.notifyDataSetChanged();
            }
        });

        GridView gridView = (GridView) findViewById(R.id.grid_view);
        mPhotoAdapter = new SearchPhotoAdapter(this, mPhotoList);
        gridView.setAdapter(mPhotoAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ParseObject photo = (ParseObject) parent.getItemAtPosition(position);
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
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject photo = (ParseObject) parent.getItemAtPosition(position);
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
                return false;
            }
        });
    }

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
            ParseObject object = getItem(position);
            text.setText(object.getString("username"));
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

}
