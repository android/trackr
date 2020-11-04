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

import com.example.android.trackr.data.TaskListItem
import com.example.android.trackr.data.TaskState


/**
 * Combines the results of [expandedStatesMap] and [taskListItems] and returns a list of
 * [DataItem]s.
 * @param taskListItems List of TaskListItems, which could be null
 * @param  expandedStatesMap A [TaskState] to [Boolean] map, which determines the
 * collapsed/expanded state of a category of [TaskListItem]s
 * TODO: refactor into a UseCase.
 */
class DataItemsCreator(
    private val taskListItems: List<TaskListItem>?,
    private val expandedStatesMap: MutableMap<TaskState, Boolean>?
) {
    fun execute(): List<DataItem>? {
        taskListItems?.let { items ->
            expandedStatesMap?.let { statesMap ->
                val itemsToSubmit = mutableListOf<DataItem>()
                val stateToItemsMap: Map<TaskState, List<TaskListItem>>? =
                    items.groupBy { it.state }
                for (entry in statesMap) {
                    val sublist: List<TaskListItem>? = stateToItemsMap?.get(entry.key)
                    itemsToSubmit.add(
                        DataItem.HeaderItem(
                            HeaderData(
                                count = sublist?.size ?: 0,
                                taskState = entry.key
                            ),
                        )
                    )
                    if (statesMap[entry.key] == true) {
                        sublist?.map { itemsToSubmit.add(DataItem.TaskItem(it)) }
                    }
                }
                return itemsToSubmit
            }
        }
        return null
    }
}