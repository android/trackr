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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackr.R
import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.data.User
import com.example.android.trackr.databinding.FragmentTasksBinding
import com.example.android.trackr.ui.detail.TaskDetailFragmentArgs

import com.example.android.trackr.ui.utils.configureEdgeToEdge
import com.example.android.trackr.ui.profile.ProfileDialogFragmentArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.Clock
import java.util.Collections
import javax.inject.Inject

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.ItemListener {

    private val viewModel: TasksViewModel by viewModels()
    private lateinit var binding: FragmentTasksBinding
    private lateinit var tasksAdapter: TasksAdapter

    @Inject
    lateinit var currentUser: User

    @Inject
    lateinit var clock: Clock

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tasksAdapter = TasksAdapter(this, currentUser, clock)

        binding = FragmentTasksBinding.bind(view)
        binding.listener = this
        configureEdgeToEdge(
            root = view,
            scrollingContent = binding.tasksList,
            topBar = binding.stickyHeader.headerContent,
            bottomBar = binding.bottomAppBar
        )

        binding.tasksList.apply {
            val itemTouchHelper = ItemTouchHelper(SwipeAndDragCallback())
            itemTouchHelper.attachToRecyclerView(this)
            val linearLayoutManager = LinearLayoutManager(activity)
            layoutManager = linearLayoutManager
            adapter = tasksAdapter
            doOnScroll { _, _ ->
                binding.headerData = tasksAdapter
                    .findHeaderItem(linearLayoutManager.findFirstVisibleItemPosition())
                    .headerData
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.dataItems.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }

        // Logic for presenting user with the option to unarchive a previously archived task.
        viewModel.archivedItem.observe(viewLifecycleOwner) { item ->
            if (item != null) {
                view?.let {
                    Snackbar.make(
                        it,
                        getString(R.string.task_archived),
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(getString(R.string.undo)) {
                            viewModel.unarchiveTask()
                        }
                        .show()
                }
            }
        }
    }

    override fun onStarClicked(taskListItem: TaskListItem) {
        viewModel.toggleTaskStarState(taskListItem, currentUser)
    }

    override fun onHeaderClicked(headerData: HeaderData) {
        viewModel.toggleExpandedState(headerData)
    }

    override fun onTaskClicked(taskListItem: TaskListItem) {
        findNavController()
            .navigate(R.id.nav_task_detail, TaskDetailFragmentArgs(taskListItem.id).toBundle())
    }

    override fun onTaskArchived(taskListItem: TaskListItem) {
        viewModel.archiveTask(taskListItem)
    }

    override fun onTaskDragged(fromPosition: Int, toPosition: Int) {
        tasksAdapter.changeTaskPosition(fromPosition, toPosition)
    }

    override fun onDragStarted() {
        viewModel.cacheCurrentList(listToTaskListItems(tasksAdapter.currentList))
    }

    override fun onDragCompleted(fromPosition: Int, toPosition: Int, customAction: Boolean) {
        val draggedItem = tasksAdapter.currentList[fromPosition]

        val list = tasksAdapter.currentList.toMutableList()

        // If using a custom action for drag and drop, the current list tasksAdapter.currentList
        // will return the original list. In that case, swap the items in the returned list.
        if (customAction) {
            Collections.swap(list, fromPosition, toPosition)
        }

        viewModel.persistUpdatedList(
            (draggedItem as DataItem.TaskItem).taskListItem.status,
            listToTaskListItems(list)
        )

        Snackbar.make (
            binding.root,
            R.string.task_position_changed,
            Snackbar.LENGTH_LONG
        )
            .setAction(getString(R.string.undo)) {
                viewModel.restoreListFromCache()
            }
            .show()
    }

    override fun onAvatarClicked(taskListItem: TaskListItem) {
        findNavController().navigate(
            R.id.nav_profile,
            ProfileDialogFragmentArgs(taskListItem.owner.id).toBundle()
        )
    }

    private fun listToTaskListItems(list: List<DataItem>) : List<TaskListItem> {
        return list.filterIsInstance<DataItem.TaskItem>().map { it.taskListItem }
    }
}

private inline fun RecyclerView.doOnScroll(crossinline action: (dx: Int, dy: Int) -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            action(dx, dy)
        }
    })
}
