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

package com.example.android.trackr.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import com.example.android.trackr.R
import com.example.android.trackr.databinding.SettingsFragmentBinding
import com.example.android.trackr.ui.dataBindings

class SettingsFragment : Fragment(R.layout.settings_fragment) {

    private val binding by dataBindings(SettingsFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // We need to access binding once in order for Data Binding to bind the views.
        binding.root
        if (childFragmentManager.findFragmentById(R.id.preference) == null) {
            childFragmentManager.commitNow {
                replace(R.id.preference, SettingsPreferenceFragment())
            }
        }
    }
}
