package com.example.uploader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

import com.parse.ParseUser;


public class SettingsFragment extends PreferenceFragment {
    private ParseUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference);
        EditTextPreference editText = (EditTextPreference) findPreference(getString(R.string.display_name_key));

        // usernameを表示させておく
        user = ParseUser.getCurrentUser();
        if (user!=null) {
            editText.setSummary(user.getUsername());
        }
    }

    // ユーザーが設定を変更した時に実行される処理を定義できる
    private SharedPreferences.OnSharedPreferenceChangeListener onPreferenceChangeListenter = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.display_name_key))) {
                EditTextPreference pref = (EditTextPreference) findPreference(key);
                pref.setSummary(pref.getText());

                if (user!=null) {
                    // 表示を変更するついでにparseのデータも更新する
                    user.setUsername(pref.getText());
                    user.saveInBackground();
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(onPreferenceChangeListenter);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onPreferenceChangeListenter);
    }
}
