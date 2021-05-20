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

package com.example.android.trackr.ui.archives

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.android.trackr.R
import com.example.android.trackr.data.User
import com.example.android.trackr.databinding.ArchiveFragmentBinding
import com.example.android.trackr.ui.dataBindings
import com.example.android.trackr.ui.detail.TaskDetailFragmentArgs
import com.example.android.trackr.ui.utils.configureEdgeToEdge
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.Clock
import javax.inject.Inject

@AndroidEntryPoint
class ArchivesFragment : Fragment(R.layout.archive_fragment) {

    private val viewModel: ArchiveViewModel by viewModels()
    private val binding by dataBindings(ArchiveFragmentBinding::bind)

    @Inject
    lateinit var currentUser: User

    @Inject
    lateinit var clock: Clock

    @SuppressLint("ShowToast")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        configureEdgeToEdge(
            root = view,
            scrollingContent = binding.archivedTasks,
            topBar = binding.toolbar,
            bottomBar = binding.bottomBar
        )

        val adapter = ArchiveAdapter(
            currentUser = currentUser,
            clock = clock,
            onItemClick = { task ->
                if (viewModel.selectedCount.value ?: 0 > 0) {
                    viewModel.toggleTaskSelection(task.id)
                } else {
                    navigateToDetail(task.id)
                }
            },
            onItemLongClick = { task -> viewModel.toggleTaskSelection(task.id) },
            onItemStarClicked = { task -> viewModel.toggleTaskStarState(task.id) }
        )
        binding.archivedTasks.adapter = adapter

        viewModel.archivedTasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
        }

        binding.bottomBar.setNavigationOnClickListener { viewModel.clearSelection() }
        binding.bottomBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_unarchive -> {
                    viewModel.unarchiveSelectedTasks()
                    true
                }
                else -> false
            }
        }

        // Undo unarchiving tasks
        var snackbar: Snackbar? = null
        viewModel.undoableCount.observe(viewLifecycleOwner) { undoableCount ->
            snackbar = if (undoableCount > 0) {
                println("Showing ${System.currentTimeMillis()}")
                Snackbar
                    .make(
                        binding.coordinator,
                        resources.getQuantityString(
                            R.plurals.tasks_unarchived,
                            undoableCount,
                            undoableCount
                        ),
                        Snackbar.LENGTH_INDEFINITE
                    )
                    .setAction(R.string.undo) { viewModel.undoUnarchiving() }.also {
                        it.show()
                    }
            } else {
                println("Dismissing ${System.currentTimeMillis()}")
                snackbar?.dismiss()
                null
            }
        }
    }

    private fun navigateToDetail(taskId: Long) {
        findNavController().navigate(
            R.id.nav_task_detail,
            TaskDetailFragmentArgs(taskId).toBundle()
        )
    }
}
