/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.example.android.trackr.ui.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.trackr.NavTaskEditGraphArgs
import com.example.android.trackr.R
import com.example.android.trackr.databinding.TaskDetailFragmentBinding
import com.example.android.trackr.ui.dataBindings
import com.example.android.trackr.ui.utils.DateTimeUtils
import com.example.android.trackr.ui.utils.configureEdgeToEdge
import com.example.android.trackr.ui.utils.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import org.threeten.bp.Clock
import javax.inject.Inject

@AndroidEntryPoint
class TaskDetailFragment : Fragment(R.layout.task_detail_fragment) {

    private val viewModel: TaskDetailViewModel by viewModels()
    private val args: TaskDetailFragmentArgs by navArgs()
    private val binding by dataBindings(TaskDetailFragmentBinding::bind)

    @Inject
    lateinit var clock: Clock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.taskId.value = args.taskId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.clock = clock

        configureEdgeToEdge(
            root = view,
            scrollingContent = binding.scrollingContent,
            topBar = binding.toolbar,
            fab = binding.edit
        )

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        repeatWithViewLifecycle {
            viewModel.detail.collect { value ->
                value?.let {
                    binding.dueAt.contentDescription = resources.getString(
                        R.string.due_date_with_value,
                        DateTimeUtils.formattedDate(resources, it.dueAt, clock)
                    )

                    binding.createdAt.contentDescription = resources.getString(
                        R.string.creation_date_with_value,
                        DateTimeUtils.formattedDate(resources, it.createdAt, clock)
                    )

                    binding.owner.contentDescription =
                        resources.getString(R.string.owner_with_value, value.owner.username)
                    binding.creator.contentDescription =
                        resources.getString(R.string.creator_with_value, value.creator.username)
                }
            }
        }

        binding.edit.setOnClickListener {
            findNavController().navigate(
                R.id.nav_task_edit_graph,
                NavTaskEditGraphArgs(
                    taskId = args.taskId
                ).toBundle()
            )
        }

        binding.star.setOnClickListener {
            viewModel.toggleTaskStarState()
        }
    }
}
