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
import com.example.android.trackr.R
import com.example.android.trackr.databinding.FragmentTaskDetailBinding
import com.example.android.trackr.ui.edit.TaskEditFragmentArgs
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.Clock
import javax.inject.Inject

@AndroidEntryPoint
class TaskDetailFragment : Fragment(R.layout.fragment_task_detail) {

    private val viewModel: TaskDetailViewModel by viewModels()
    private val args: TaskDetailFragmentArgs by navArgs()

    private lateinit var binding: FragmentTaskDetailBinding

    @Inject
    lateinit var clock: Clock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.taskId = args.taskId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskDetailBinding.bind(view)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.clock = clock
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.edit.setOnClickListener {
            findNavController().navigate(
                R.id.nav_task_edit,
                TaskEditFragmentArgs(
                    taskId = args.taskId
                ).toBundle()
            )
        }
    }
}
