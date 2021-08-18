/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackr

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TrackrApplication : Application() {

    override fun onCreate() {
        super.onCreate()
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