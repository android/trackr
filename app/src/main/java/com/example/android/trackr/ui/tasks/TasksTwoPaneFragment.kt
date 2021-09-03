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
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.example.android.trackr.R
import com.example.android.trackr.TaskDetailGraphDirections
import com.example.android.trackr.databinding.TasksTwoPaneFragmentBinding
import com.example.android.trackr.ui.BaseTwoPaneFragment
import com.example.android.trackr.ui.dataBindings
import com.example.android.trackr.ui.utils.repeatWithViewLifecycle
import com.example.android.trackr.ui.utils.requireFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksTwoPaneFragment : BaseTwoPaneFragment(R.layout.tasks_two_pane_fragment) {

    private val binding by dataBindings(TasksTwoPaneFragmentBinding::bind)
    private val tasksViewModel: TasksViewModel by hiltNavGraphViewModels(R.id.nav_tasks)

    override fun getSlidingPaneLayout(): SlidingPaneLayout = binding.slidingPaneLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val detailNavController = requireFragment(R.id.detail_pane).findNavController()
        repeatWithViewLifecycle {
            launch {
                tasksViewModel.showTaskDetailEvents.collect {
                    if (it.isNewSelection) {
                        // Change the detail pane contents.
                        detailNavController.navigate(TaskDetailGraphDirections.toTaskDetail(it.taskId))
                    }
                    if (it.isUserSelection) {
                        // Slide the detail pane into view. If both panes are visible, this has no
                        // visible effect.
                        binding.slidingPaneLayout.openPane()
                    }
                }
            }
        }
    }
}
