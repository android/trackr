package com.example.android.trackr

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TrackrApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        setupDarkModePreference()
    }

    private fun setupDarkModePreference() {
        val defaultValue = resources.getString(R.string.system_default_value)
        val disabledValue = resources.getString(R.string.disabled_value)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val darkModeSetting =
            sharedPreferences.getString(resources.getString(R.string.dark_mode_key), defaultValue)

        if (!darkModeSetting.equals(defaultValue)) {
            if (darkModeSetting.equals(disabledValue)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }
}