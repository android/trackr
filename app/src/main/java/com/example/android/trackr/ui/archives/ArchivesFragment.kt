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

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.example.android.trackr.R
import com.example.android.trackr.databinding.ArchiveFragmentBinding
import com.example.android.trackr.ui.dataBindings
import com.example.android.trackr.ui.utils.doOnApplyWindowInsets
import com.example.android.trackr.ui.utils.repeatWithViewLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

@AndroidEntryPoint
class ArchivesFragment : Fragment(R.layout.archive_fragment) {

    private val viewModel: ArchiveViewModel by hiltNavGraphViewModels(R.id.nav_archives)
    private val binding by dataBindings(ArchiveFragmentBinding::bind)
    private val backPressCallback = BackPressCallback()

    @Inject
    lateinit var clock: Clock

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel

        val adapter = ArchiveAdapter(
            clock = clock,
            onItemClick = { task ->
                if (viewModel.selectedCount.value > 0) {
                    viewModel.toggleTaskSelection(task.id)
                } else {
                    viewModel.selectTask(task)
                }
            },
            onItemLongClick = { task -> viewModel.toggleTaskSelection(task.id) },
            onItemStarClicked = { task -> viewModel.toggleTaskStarState(task.id) }
        )
        binding.archivedTasks.adapter = adapter

        repeatWithViewLifecycle {
            launch {
                viewModel.archivedTasks.collect {
                    adapter.submitList(it)
                }
            }
            // Undo unarchiving tasks
            launch {
                viewModel.unarchiveActions.collect { action ->
                    val count = action.taskIds.size
                    Snackbar.make(
                        binding.coordinator,
                        resources.getQuantityString(
                            R.plurals.tasks_unarchived,
                            count,
                            count
                        ),
                        5_000 // 5 seconds
                    )
                        .setAction(R.string.undo) { viewModel.undoUnarchiving(action) }
                        .show()
                }
            }
            launch {
                viewModel.selectedCount.collect {
                    backPressCallback.isEnabled = it > 0
                }
            }
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

        binding.archivedTasks.doOnApplyWindowInsets { v, insets, padding, _ ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // BottomAppBar has its own logic to adapt to window insets, but its height isn't
            // updated until measurement, so wait for its next layout.
            binding.bottomBar.doOnNextLayout { bottomBar ->
                v.updatePadding(
                    left = padding.left + systemBars.left,
                    right = padding.right + systemBars.right,
                    bottom = padding.bottom + bottomBar.height
                )
            }
            insets
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressCallback)
    }

    inner class BackPressCallback : OnBackPressedCallback(false) {

        override fun handleOnBackPressed() {
            viewModel.clearSelection()
        }
    }
}
