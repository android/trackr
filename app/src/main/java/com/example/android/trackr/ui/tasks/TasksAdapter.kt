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
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackr.R
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.databinding.HeaderItemBinding
import com.example.android.trackr.databinding.TaskSummaryBinding
import com.example.android.trackr.ui.utils.AccessibilityUtils
import java.time.Clock
import java.util.Collections


class TasksAdapter(
    private val itemListener: ItemListener,
    private val clock: Clock
) :
    ListAdapter<ListItem, RecyclerView.ViewHolder>(
        ListItemDiffCallback()
    ) {

    private lateinit var dragAndDropActionsHelper: DragAndDropActionsHelper

    override fun onCurrentListChanged(
        previousList: MutableList<ListItem>,
        currentList: MutableList<ListItem>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        dragAndDropActionsHelper = DragAndDropActionsHelper(currentList)
    }

    interface ItemListener {
        fun onHeaderClicked(headerData: HeaderData)
        fun onStarClicked(taskSummary: TaskSummary)
        fun onTaskClicked(taskSummary: TaskSummary)
        fun onTaskArchived(taskSummary: TaskSummary)
        fun onTaskDragged(fromPosition: Int, toPosition: Int)
        fun onDragStarted()
        fun onDragCompleted(
            fromPosition: Int,
            toPosition: Int,
            usingDragAndDropCustomActions: Boolean = false
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent, itemListener)
            ITEM_VIEW_TYPE_TASK -> TaskViewHolder.from(parent, itemListener, clock)
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.TypeHeader -> ITEM_VIEW_TYPE_HEADER
            is ListItem.TypeTask -> ITEM_VIEW_TYPE_TASK
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TaskViewHolder -> {
                holder.bind(
                    (getItem(position) as ListItem.TypeTask).taskSummary, dragAndDropActionsHelper)
            }

            is HeaderViewHolder -> {
                holder.bind((getItem(position) as ListItem.TypeHeader).headerData)
            }
        }
    }

    /**
     * Find a header item at the specified [position] or before.
     */
    fun findHeaderItem(position: Int): ListItem.TypeHeader? {
        var p = position
        while (p in 0 until itemCount) {
            val item = getItem(p)
            if (item is ListItem.TypeHeader) return item
            p--
        }
        return null
    }

    fun changeTaskPosition(fromPosition: Int, toPosition: Int) {
        // TODO: persist new order in the db instead of calling submitList()
        if (currentList[fromPosition] is ListItem.TypeTask && currentList[toPosition] is ListItem.TypeTask) {
            val fromItem = (currentList[fromPosition] as ListItem.TypeTask).taskSummary
            val toItem = (currentList[toPosition] as ListItem.TypeTask).taskSummary
            if (fromItem.status != toItem.status) {
                return
            }
            val newList = currentList.toMutableList()
            Collections.swap(newList, fromPosition, toPosition)
            submitList(newList)
        }
    }

    class HeaderViewHolder private constructor(
        val binding: HeaderItemBinding,
        private val itemListener: ItemListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(headerData: HeaderData) {
            binding.headerData = headerData
            binding.listener = itemListener
            ViewCompat.setStateDescription(
                binding.root,
                headerData.stateDescription(binding.root.context)
            )
            ViewCompat.setAccessibilityHeading(binding.root, true)
        }

        companion object {
            fun from(parent: ViewGroup, itemListener: ItemListener): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return HeaderViewHolder(
                    HeaderItemBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ), itemListener
                )
            }
        }
    }

    class TaskViewHolder private constructor(
        val binding: TaskSummaryBinding,
        private val itemListener: ItemListener,
        private val clock: Clock
    ) :
        RecyclerView.ViewHolder(binding.root), SwipeAndDragCallback.ItemTouchListener {

        val accessibilityActionIds = arrayListOf<Int>()

        fun bind(taskSummary: TaskSummary, dragAndDraopActionsHelper: DragAndDropActionsHelper)  {
            val resources = binding.root.resources
            binding.taskSummary = taskSummary
            binding.card.setOnClickListener { itemListener.onTaskClicked(taskSummary) }
            binding.star.setOnClickListener { itemListener.onStarClicked(taskSummary) }
            binding.clock = clock
            binding.root.contentDescription =
                AccessibilityUtils.taskSummaryLabel(binding.root.context, taskSummary, clock)

            ViewCompat.setStateDescription(
                binding.root,
                resources.getString(taskSummary.status.stringResId) + ", " +
                        resources.getString(
                            if (taskSummary.starred) R.string.starred else R.string.unstarred
                        )
            )

            // TODO(b/176934848): include chip/tag information in contentDescription of each task in list.
            binding.chipGroup.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS

            // TODO(b/178437838): write UIAutomation test to confirm the custom action label.

            // Clear previously added actions. If this is skipped, the actions may be duplicated
            // when a view is rebound.
            removeAccessibilityActions(binding.root)

            addArchiveAccessibilityAction(taskSummary)
            addStarAccessibilityAction(taskSummary)

            val actionParams = dragAndDraopActionsHelper.execute(adapterPosition)
            for (actionParam in actionParams) {
                addDragAndDropAction(actionParam)
            }

            binding.executePendingBindings()
        }

        override fun onItemSwiped() {
            binding.taskSummary?.let {
                itemListener.onTaskArchived(it)
            }
        }

        override fun onItemMoved(fromPosition: Int, toPosition: Int) {
            itemListener.onTaskDragged(fromPosition, toPosition)
        }

        override fun onItemMoveStarted() {
            itemListener.onDragStarted()
        }

        override fun onItemMoveCompleted(fromPosition: Int, toPosition: Int) {
            itemListener.onDragCompleted(fromPosition, toPosition)
        }

        /**
         * Creates a custom accessibility action for archiving tasks.
         *
         * This provides the swipe-to-archive functionality to users of accessibility services who
         * may not be able to perform the swipe gesture.
         */
        private fun addArchiveAccessibilityAction(taskSummary: TaskSummary) {
            accessibilityActionIds.add(
                ViewCompat.addAccessibilityAction(
                    binding.root,
                    // The label surfaced to end user by an accessibility service.
                    binding.root.context.getString(R.string.archive)
                ) { _, _ ->
                    // The functionality associated with the label.
                    itemListener.onTaskArchived(taskSummary)
                    true
                })
        }

        /**
         * Adds a custom accessibility action representing the dragging and dropping of an item.
         */
        private fun addDragAndDropAction(
            actionParamAnd: DragAndDropActionsHelper.DragAndDropActionInfo
        ) {
            accessibilityActionIds.add(
                ViewCompat.addAccessibilityAction(
                    binding.root,
                    binding.root.context.resources.getString(actionParamAnd.label)
                ) { _, _ ->
                    doDrag(actionParamAnd.fromPosition, actionParamAnd.toPosition)
                    true
                })
        }

        private fun doDrag(fromPosition: Int, toPosition: Int) {
            itemListener.onDragStarted()
            itemListener.onTaskDragged(fromPosition, toPosition)
            itemListener.onDragCompleted(
                fromPosition,
                toPosition,
                usingDragAndDropCustomActions = true
            )
        }

        private fun removeAccessibilityActions(view: View) {
            accessibilityActionIds.forEach { ViewCompat.removeAccessibilityAction(view, it) }
            accessibilityActionIds.clear()
        }

        /**
         * Creates a custom accessibility action for starring / unstarring tasks.
         */
        private fun addStarAccessibilityAction(taskSummary: TaskSummary) {
            accessibilityActionIds.add(
                ViewCompat.addAccessibilityAction(
                    binding.root,
                    // The label surfaced to end user by an accessibility service.
                    if (taskSummary.starred) {
                        binding.root.context.getString(R.string.unstar)
                    } else {
                        binding.root.context.getString(R.string.star)
                    }
                ) { _, _ ->
                    // The functionality associated with the label.
                    itemListener.onStarClicked(taskSummary)
                    true
                })
        }

        companion object {
            fun from(
                parent: ViewGroup,
                itemListener: ItemListener,
                clock: Clock
            ):
                    TaskViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return TaskViewHolder(
                    TaskSummaryBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ), itemListener, clock
                )
            }
        }
    }

    companion object {
        const val ITEM_VIEW_TYPE_HEADER = 0
        const val ITEM_VIEW_TYPE_TASK = 1
    }
}

class ListItemDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem == newItem
    }
}

sealed class ListItem {
    abstract val id: Long

    data class TypeTask(val taskSummary: TaskSummary) : ListItem() {
        override val id = taskSummary.id
    }

    data class TypeHeader(val headerData: HeaderData) : ListItem() {
        override val id = Long.MIN_VALUE
    }
}

data class HeaderData(
    val count: Int,
    val taskStatus: TaskStatus,
    var expanded: Boolean
) {
    fun stateDescription(context: Context): String {
        return context.getString(if (expanded) R.string.expanded else R.string.collapsed)
    }

    fun label(context: Context): String {
        return context.getString(
            R.string.header_label_with_count,
            context.getString(taskStatus.stringResId),
            count
        )
    }
}
