package com.example.uploader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference);
        EditTextPreference editText = (EditTextPreference) findPreference(getString(R.string.display_name_key));

        // TODO 8-1 usernameを表示しよう
    }

    // ユーザーが設定を変更した時に実行される処理を定義できる
    private SharedPreferences.OnSharedPreferenceChangeListener onPreferenceChangeListenter = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.display_name_key))) {
                EditTextPreference pref = (EditTextPreference) findPreference(key);
                pref.setSummary(pref.getText());

                // TODO 8-2 usernameの変更を保存しよう
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
