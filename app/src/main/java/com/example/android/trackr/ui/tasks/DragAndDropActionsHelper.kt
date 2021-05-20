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

package com.example.android.trackr.ui.tasks

import com.example.android.trackr.R

class DragAndDropActionsHelper(private val items: List<ListItem>) {

    private var headerPositions: List<Int> = mutableListOf()
    private var previousHeaderPosition = NO_POSITION
    private var nextHeaderPosition = NO_POSITION

    init {
        items.forEachIndexed { index, item ->
            if (item is ListItem.TypeHeader) {
                (headerPositions as MutableList).add(index)
            }
        }
    }

    fun execute(position: Int): List<DragAndDropActionInfo> {
        try {
            previousHeaderPosition = headerPositions.first()
            headerPositions.forEach {
                if (it < position) {
                    previousHeaderPosition = it
                }
            }

            nextHeaderPosition = headerPositions.last()
            if (nextHeaderPosition < position) { // there is no next header
                nextHeaderPosition = items.size
            } else {
                headerPositions.asReversed().forEach {
                    if (it > position) {
                        nextHeaderPosition = it
                    }
                }
            }
            return obtainDragAndDropActionInfo(position)
        } catch (e: NoSuchElementException) {
            e.printStackTrace()
            // We get here if there are no headers. In that case, there are no actions associated
            // with drag and drop.
            return emptyList()
        }
    }

    private fun obtainDragAndDropActionInfo(position: Int): List<DragAndDropActionInfo> {
        val actionParams = mutableListOf<DragAndDropActionInfo>()

        if (nextHeaderPosition != NO_POSITION
            && previousHeaderPosition != NO_POSITION
            && nextHeaderPosition - previousHeaderPosition > 2 // Only one item between two headers.
        ) {
            if (position - previousHeaderPosition > 1) {
                actionParams.add(
                    DragAndDropActionInfo(
                        position,
                        previousHeaderPosition + 1,
                        R.string.move_to_top
                    )
                )
                if (position - previousHeaderPosition > 2) {
                    actionParams.add(
                        DragAndDropActionInfo(position, position - 1, R.string.move_up_one)
                    )
                }
            }

            if (nextHeaderPosition - position > 1) {
                actionParams.add(
                    DragAndDropActionInfo(position, nextHeaderPosition - 1, R.string.move_to_bottom)
                )
                if (nextHeaderPosition - position > 2) {
                    actionParams.add(
                        DragAndDropActionInfo(position, position + 1, R.string.move_down_one)
                    )
                }
            }
        }

        return actionParams
    }

    /**
     * Contains data for building a custom accessibility action to enable the dragging and dropping
     * of items.
     */
    data class DragAndDropActionInfo(
        val fromPosition: Int,
        val toPosition: Int,
        val label: Int
    )

    companion object {
        private const val NO_POSITION = -1
    }
}