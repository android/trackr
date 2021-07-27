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

package com.example.android.trackr.ui.tasks

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackr.R
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.User
import com.example.android.trackr.databinding.TasksFragmentBinding
import com.example.android.trackr.ui.dataBindings
import com.example.android.trackr.ui.detail.TaskDetailFragmentArgs
import com.example.android.trackr.ui.utils.doOnApplyWindowInsets
import com.example.android.trackr.ui.utils.repeatWithViewLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Clock
import java.util.Collections
import javax.inject.Inject

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.tasks_fragment), TasksAdapter.ItemListener {

    private val viewModel: TasksViewModel by viewModels()
    private val binding by dataBindings(TasksFragmentBinding::bind)
    private lateinit var tasksAdapter: TasksAdapter

    @Inject
    lateinit var currentUser: User

    @Inject
    lateinit var clock: Clock

    @SuppressLint("ShowToast")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tasksAdapter = TasksAdapter(this, currentUser, clock)

        binding.listener = this

        binding.tasksList.apply {
            val itemTouchHelper = ItemTouchHelper(SwipeAndDragCallback())
            itemTouchHelper.attachToRecyclerView(this)
            val linearLayoutManager = LinearLayoutManager(activity)
            layoutManager = linearLayoutManager
            adapter = tasksAdapter
            doOnScroll { _, _ ->
                updateStickyHeader()
            }

            doOnApplyWindowInsets { v, insets, padding, _ ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                // BottomAppBar has its own logic to adapt to window insets, but its height isn't
                // updated until measurement, so wait for its next layout.
                binding.bottomAppBar.doOnNextLayout { bottomBar ->
                    v.updatePadding(
                        left = padding.left + systemBars.left,
                        right = padding.right + systemBars.right,
                        bottom = bottomBar.height
                    )
                }
            }
        }
        binding.add.setOnClickListener {
            findNavController().navigate(R.id.nav_task_edit_graph)
        }
        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.archive -> {
                    findNavController().navigate(R.id.nav_archives)
                    true
                }
                R.id.settings -> {
                    findNavController().navigate(R.id.nav_settings)
                    true
                }
                else -> false
            }
        }

        repeatWithViewLifecycle {
            launch {
                viewModel.listItems.collect {
                    tasksAdapter.submitList(it) {
                        updateStickyHeader()
                    }
                }
            }
            launch {
                // Logic for presenting user the option to unarchive a previously archived task.
                viewModel.archivedItem.collect { item ->
                    Snackbar
                        .make(
                            binding.coordinator,
                            getString(R.string.task_archived),
                            Snackbar.LENGTH_LONG
                        )
                        .setAction(getString(R.string.undo)) {
                            viewModel.unarchiveTask(item)
                        }
                        .setAnchorView(binding.add)
                        .show()
                }
            }
            launch {
                viewModel.undoReorderTasks.collect { undo ->
                    Snackbar
                        .make(
                            binding.coordinator,
                            R.string.task_position_changed,
                            Snackbar.LENGTH_LONG
                        )
                        .setAction(getString(R.string.undo)) {
                            viewModel.undoReorderTasks(undo)
                        }
                        .setAnchorView(binding.add)
                        .show()
                }
            }
        }
    }

    private fun updateStickyHeader() {
        val linearLayoutManager = binding.tasksList.layoutManager as? LinearLayoutManager ?: return
        tasksAdapter.findHeaderItem(linearLayoutManager.findFirstVisibleItemPosition())?.let {
            binding.headerData = it.headerData
        }
    }

    override fun onStarClicked(taskSummary: TaskSummary) {
        viewModel.toggleTaskStarState(taskSummary)
    }

    override fun onHeaderClicked(headerData: HeaderData) {
        viewModel.toggleExpandedState(headerData)
    }

    override fun onTaskClicked(taskSummary: TaskSummary) {
        findNavController()
            .navigate(R.id.nav_task_detail, TaskDetailFragmentArgs(taskSummary.id).toBundle())
    }

    override fun onTaskArchived(taskSummary: TaskSummary) {
        viewModel.archiveTask(taskSummary)
    }

    override fun onTaskDragged(fromPosition: Int, toPosition: Int) {
        tasksAdapter.changeTaskPosition(fromPosition, toPosition)
    }

    override fun onDragStarted() {
        // Do nothing
    }

    override fun onDragCompleted(
        fromPosition: Int,
        toPosition: Int,
        usingDragAndDropCustomActions: Boolean
    ) {
        // If using a custom action for drag and drop, the current list tasksAdapter.currentList
        // will return the original list. In that case, swap the items in the returned list.
        val list = tasksAdapter.currentList.toMutableList()
        if (usingDragAndDropCustomActions) {
            Collections.swap(list, fromPosition, toPosition)
        }

        // The item dragged and moved.
        val draggedItem = list[toPosition] as? ListItem.TypeTask ?: return

        // The item on the other end of the range shifted by the movement.
        val targetItem = if (fromPosition < toPosition) {
            list[toPosition - 1]
        } else {
            list[toPosition + 1]
        } as? ListItem.TypeTask ?: return

        viewModel.reorderTasks(draggedItem.taskSummary, targetItem.taskSummary)
    }
}

private inline fun RecyclerView.doOnScroll(crossinline action: (dx: Int, dy: Int) -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            action(dx, dy)
        }
    })
}
