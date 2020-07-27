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

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackr.R
import com.example.android.trackr.data.Issue

class IssuesAdapter(private val list: List<Issue>)
    : RecyclerView.Adapter<IssueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return IssueViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val issue: Issue = list[position]
        holder.bind(issue)
    }

    override fun getItemCount(): Int = list.size
}

class IssueViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item, parent, false)) {
    private var titleView: TextView? = null

    init {
        titleView = itemView.findViewById(R.id.title)
    }

    fun bind(issue: Issue) {
        titleView?.text = issue.title
    }
}