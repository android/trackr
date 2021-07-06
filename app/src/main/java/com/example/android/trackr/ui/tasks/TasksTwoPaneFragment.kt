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

package com.example.android.trackr.ui.tasks

import android.os.Bundle
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.example.android.trackr.R
import com.example.android.trackr.databinding.TasksTwoPaneFragmentBinding
import com.example.android.trackr.ui.dataBindings
import com.example.android.trackr.ui.utils.doOnApplyWindowInsets

class TasksTwoPaneFragment : Fragment(R.layout.tasks_two_pane_fragment) {

    private val binding by dataBindings(TasksTwoPaneFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.slidingPaneLayout.apply {
            lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED
            doOnLayout { // Wait for layout, otherwise isSlideable may have the wrong value.
                if (!isSlideable) {
                    doOnApplyWindowInsets { v, insets, padding, _ ->
                        // Consume horizontal insets, otherwise the children might apply padding
                        // where they shouldn't, such as the left pane padding its right side.
                        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                        v.updatePadding(
                            left = padding.left + systemBars.left,
                            right = padding.right + systemBars.right
                        )
                        WindowInsetsCompat.Builder(insets).setInsets(
                            WindowInsetsCompat.Type.systemBars(),
                            Insets.of(0, systemBars.top, 0, systemBars.bottom)
                        ).build()
                    }
                }
            }
        }
    }
}
