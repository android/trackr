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

package com.example.android.trackr.ui.issues

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackr.R
import com.example.android.trackr.data.HeaderData
import com.example.android.trackr.data.Issue
import com.example.android.trackr.data.IssueState
import com.example.android.trackr.databinding.ListHeaderBinding
import com.example.android.trackr.databinding.ListIssueBinding

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ISSUE = 1

class IssuesAdapter(
    private val issueItemListener: IssueItemListener
) :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(
    DataItemDiffCallback()
) {

    interface IssueItemListener {
        fun onItemClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ISSUE -> IssueViewHolder.from(parent, issueItemListener)
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.HeaderItem -> ITEM_VIEW_TYPE_HEADER
            is DataItem.IssueItem -> ITEM_VIEW_TYPE_ISSUE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is IssueViewHolder -> {
                holder.bind((getItem(position) as DataItem.IssueItem).issue)
            }

            is HeaderViewHolder -> {
                holder.bind((getItem(position) as DataItem.HeaderItem).headerData)
            }
        }
    }

    // TODO: refactor into use case, using a coroutine (and add tests).
    fun addHeadersAndSubmitList(context: Context, issues: List<Issue>?) {
        val items = mutableListOf<DataItem>()

        val map = issues?.groupBy { it.state }

        val issueStates = listOf(
            IssueState.IN_PROGRESS,
            IssueState.NOT_STARTED,
            IssueState.COMPLETED,
            IssueState.ARCHIVED
        )

        issueStates.forEach { state ->
            val sublist = map?.get(state)
            // Add header even if the category does not have any issues.
            items.add(
                DataItem.HeaderItem(
                    HeaderData(
                        headerLabel(context, state),
                        sublist?.size ?: 0
                    )
                )
            )
            sublist?.map { items.add(DataItem.IssueItem(it)) }
        }

        submitList(items)
    }

    private fun headerLabel(context: Context, issueState: IssueState) : String {
        return context.getString(
            when(issueState) {
                IssueState.IN_PROGRESS -> R.string.in_progress
                IssueState.NOT_STARTED -> R.string.not_started
                IssueState.COMPLETED -> R.string.completed
                IssueState.ARCHIVED -> R.string.archived
            })
    }

    class HeaderViewHolder private constructor(private val binding: ListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(headerData: HeaderData) {
            binding.headerData = headerData
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return HeaderViewHolder(ListHeaderBinding.inflate(layoutInflater, parent, false))
            }
        }
    }

    class IssueViewHolder private constructor(
        private val binding: ListIssueBinding,
        private val issueItemListener: IssueItemListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(issue: Issue) {
            binding.issue = issue
            binding.listener = issueItemListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup, issueItemListener: IssueItemListener): IssueViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                return IssueViewHolder(
                    ListIssueBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ), issueItemListener)
            }
        }
    }
}

class DataItemDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

sealed class DataItem {
    abstract val id: Long

    data class IssueItem(val issue: Issue) : DataItem() {
        override val id = issue.id
    }

    data class HeaderItem(val headerData: HeaderData) : DataItem() {
        override val id = Long.MIN_VALUE
    }
}