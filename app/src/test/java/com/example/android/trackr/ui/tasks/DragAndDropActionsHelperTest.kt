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

import com.example.android.trackr.R
import com.example.android.trackr.data.Avatar
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.User
import com.google.common.truth.Truth
import org.junit.Test
import java.time.Instant

class DragAndDropActionsHelperTest {

    @Test
    fun execute_whenOnlyOneTask() {
        val items = mutableListOf<ListItem>().apply {
            add(headerItem)
            addAll(getTasks(1))
        }

        val subject = DragAndDropActionsHelper(items)
        Truth.assertThat(subject.execute(1)).isEmpty()
    }


    @Test
    fun execute_whenTwoTasks() {
        val items = mutableListOf<ListItem>().apply {
            add(headerItem)
            addAll(getTasks(2))
        }

        val subject = DragAndDropActionsHelper(items)
        val actionsForFirst = subject.execute(1)
        Truth.assertThat(actionsForFirst.size).isEqualTo(1)
        Truth.assertThat(actionsForFirst[0].fromPosition).isEqualTo(1)
        Truth.assertThat(actionsForFirst[0].toPosition).isEqualTo(2)
        Truth.assertThat(actionsForFirst[0].label).isEqualTo(R.string.move_to_bottom)

        val actionsForSecond = subject.execute(2)
        Truth.assertThat(actionsForSecond.size).isEqualTo(1)
        Truth.assertThat(actionsForSecond[0].fromPosition).isEqualTo(2)
        Truth.assertThat(actionsForSecond[0].toPosition).isEqualTo(1)
        Truth.assertThat(actionsForSecond[0].label).isEqualTo(R.string.move_to_top)
    }


    @Test
    fun execute_whenManyItems_firstItem() {
        val items = mutableListOf<ListItem>().apply {
            add(headerItem)
            addAll(getTasks(5))
        }

        val subject = DragAndDropActionsHelper(items)
        val fromPosition = 1
        val actionsForFirst = subject.execute(fromPosition)
        Truth.assertThat(actionsForFirst.size).isEqualTo(2)

        Truth.assertThat(actionsForFirst[0].fromPosition).isEqualTo(fromPosition)
        Truth.assertThat(actionsForFirst[0].toPosition).isEqualTo(5)
        Truth.assertThat(actionsForFirst[0].label).isEqualTo(R.string.move_to_bottom)

        Truth.assertThat(actionsForFirst[1].fromPosition).isEqualTo(fromPosition)
        Truth.assertThat(actionsForFirst[1].toPosition).isEqualTo(2)
        Truth.assertThat(actionsForFirst[1].label).isEqualTo(R.string.move_down_one)
    }

    @Test
    fun execute_whenManyItems_secondItem() {
        val items = mutableListOf<ListItem>().apply {
            add(headerItem)
            addAll(getTasks(5))
        }

        val subject = DragAndDropActionsHelper(items)
        val fromPosition = 2

        val actionsForSecond = subject.execute(fromPosition)
        Truth.assertThat(actionsForSecond.size).isEqualTo(3)

        Truth.assertThat(actionsForSecond[0].fromPosition).isEqualTo(fromPosition)
        Truth.assertThat(actionsForSecond[0].toPosition).isEqualTo(1)
        Truth.assertThat(actionsForSecond[0].label).isEqualTo(R.string.move_to_top)

        Truth.assertThat(actionsForSecond[1].fromPosition).isEqualTo(fromPosition)
        Truth.assertThat(actionsForSecond[1].toPosition).isEqualTo(5)
        Truth.assertThat(actionsForSecond[1].label).isEqualTo(R.string.move_to_bottom)

        Truth.assertThat(actionsForSecond[2].fromPosition).isEqualTo(fromPosition)
        Truth.assertThat(actionsForSecond[2].toPosition).isEqualTo(3)
        Truth.assertThat(actionsForSecond[2].label).isEqualTo(R.string.move_down_one)
    }

    @Test
    fun execute_whenManyItems_thirdItem() {
        val items = mutableListOf<ListItem>().apply {
            add(headerItem)
            addAll(getTasks(5))
        }

        val subject = DragAndDropActionsHelper(items)

        val fromPosition = 3
        val actionsForThird = subject.execute(fromPosition)
        Truth.assertThat(actionsForThird.size).isEqualTo(4)

        Truth.assertThat(actionsForThird[0].fromPosition).isEqualTo(fromPosition)
        Truth.assertThat(actionsForThird[0].toPosition).isEqualTo(1)
        Truth.assertThat(actionsForThird[0].label).isEqualTo(R.string.move_to_top)

        Truth.assertThat(actionsForThird[1].fromPosition).isEqualTo(fromPosition)
        Truth.assertThat(actionsForThird[1].toPosition).isEqualTo(2)
        Truth.assertThat(actionsForThird[1].label).isEqualTo(R.string.move_up_one)

        Truth.assertThat(actionsForThird[2].fromPosition).isEqualTo(fromPosition)
        Truth.assertThat(actionsForThird[2].toPosition).isEqualTo(5)
        Truth.assertThat(actionsForThird[2].label).isEqualTo(R.string.move_to_bottom)

        Truth.assertThat(actionsForThird[3].fromPosition).isEqualTo(fromPosition)
        Truth.assertThat(actionsForThird[3].toPosition).isEqualTo(4)
        Truth.assertThat(actionsForThird[3].label).isEqualTo(R.string.move_down_one)
    }

    companion object {
        private val user = User(1, "user", Avatar.DEFAULT_USER)
        private val status = TaskStatus.NOT_STARTED

        val headerItem = ListItem.TypeHeader(HeaderData(1, status, true))

        fun getTasks(count: Int) : List<ListItem.TypeTask> {
            val items = mutableListOf<ListItem.TypeTask>()
            for (i in 0 until count) {
                items.add(
                    ListItem.TypeTask(
                        TaskSummary(
                            id = i.toLong(),
                            title = i.toString(),
                            dueAt = Instant.now(),
                            owner = user,
                            status = status,
                            tags = emptyList(),
                            orderInCategory = i,
                            starred = false,
                        )
                    )
                )
            }
            return items
        }
    }
}
