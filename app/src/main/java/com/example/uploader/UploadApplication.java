package com.example.uploader;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by takafumi.nanao on 11/13/15.
 */
public class UploadApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, getString(R.string.parse_application_id), getString(R.string.parse_client_key));
    }
}
