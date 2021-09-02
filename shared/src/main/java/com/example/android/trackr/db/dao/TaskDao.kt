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

package com.example.android.trackr.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.Task
import com.example.android.trackr.data.TaskDetail
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.data.TaskSummary
import com.example.android.trackr.data.TaskTag
import com.example.android.trackr.data.User
import com.example.android.trackr.data.UserTask
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // TODO :consider creating UserDao and moving some of the logic in this Dao there
    @Insert
    suspend fun insertUsers(users: List<User>)

    @Insert
    suspend fun insertTags(tags: List<Tag>)

    @Insert
    suspend fun insertTasks(tasks: List<Task>)

    @Insert
    suspend fun insertTaskTags(taskTags: List<TaskTag>)

    @Insert
    suspend fun insertUserTasks(userTasks: List<UserTask>)

    @Delete
    suspend fun deleteUserTasks(userTasks: List<UserTask>)

    @Query("SELECT * FROM tasks")
    fun getTasks(): Flow<List<Task>>

    @Query("SELECT * FROM USERS WHERE id = :id")
    fun getUserById(id: Long): Flow<User?>

    @Transaction
    @Query("SELECT * FROM TaskDetail WHERE id = :id")
    fun findTaskDetailById(id: Long): Flow<TaskDetail?>

    @Transaction
    @Query("SELECT * FROM TaskDetail WHERE id = :id")
    suspend fun loadTaskDetailById(id: Long): TaskDetail?

    @Transaction
    @Query("""
        SELECT s.*,
            EXISTS(
                SELECT id
                FROM user_tasks AS t
                WHERE t.taskId = s.id AND t.userId = :userId
            ) AS starred
        FROM TaskSummary AS s WHERE s.isArchived = 0
        ORDER BY s.status, s.orderInCategory
    """)
    fun getOngoingTaskSummaries(userId: Long): Flow<List<TaskSummary>>

    @Transaction
    @Query("""
        SELECT s.*,
            EXISTS(
                SELECT id
                FROM user_tasks AS t
                WHERE t.taskId = s.id AND t.userId = :userId
            ) AS starred
        FROM TaskSummary AS s WHERE s.isArchived <> 0
        ORDER BY s.orderInCategory
    """)
    fun getArchivedTaskSummaries(userId: Long): Flow<List<TaskSummary>>

    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, status: TaskStatus)

    @Query("UPDATE tasks SET status = :status WHERE id IN (:ids)")
    suspend fun updateTaskStatus(ids: List<Long>, status: TaskStatus)

    @Query("UPDATE tasks SET orderInCategory = :orderInCategory WHERE id = :id")
    suspend fun updateOrderInCategory(id: Long, orderInCategory: Int)

    @Query("UPDATE tasks SET isArchived = :isArchived WHERE id IN (:ids)")
    suspend fun setIsArchived(ids: List<Long>, isArchived: Boolean)

    @Query("SELECT * FROM user_tasks WHERE taskId = :taskId AND userId = :userId")
    suspend fun getUserTask(taskId: Long, userId: Long): UserTask?

    @Query("SELECT * FROM users")
    suspend fun loadUsers(): List<User>

    @Query("SELECT * FROM tags")
    suspend fun loadTags(): List<Tag>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Query("SELECT tagId FROM task_tags WHERE taskId = :taskId")
    suspend fun loadTaskTagIds(taskId: Long): List<Long>

    @Query("DELETE FROM task_tags WHERE taskId = :taskId AND tagId IN(:tagIds)")
    suspend fun deleteTaskTags(taskId: Long, tagIds: List<Long>)

    @Query(
        """
        SELECT MIN(orderInCategory) FROM tasks
        WHERE
            status = :status
            AND id <> :excludeTaskId
        """
    )
    suspend fun loadMinOrderInCategory(status: TaskStatus, excludeTaskId: Long): Int?

    @Transaction
    suspend fun saveTaskDetail(detail: TaskDetail, topOrderInCategory: Boolean) {
        if (detail.title.isEmpty()) {
            throw IllegalArgumentException("Task must include non-empty title.")
        }
        val task = Task(
            id = detail.id,
            title = detail.title,
            description = detail.description,
            status = detail.status,
            creatorId = detail.creator.id,
            ownerId = detail.owner.id,
            createdAt = detail.createdAt,
            dueAt = detail.dueAt,
            isArchived = detail.isArchived,
            orderInCategory = if (topOrderInCategory) {
                val min = loadMinOrderInCategory(detail.status, detail.id)
                if (min == null) 1 else min - 1
            } else {
                detail.orderInCategory
            }
        )
        val taskId = insertTask(task)
        val updatedTagIds = detail.tags.map { tag -> tag.id }
        val currentTagIds = loadTaskTagIds(taskId)
        val removedTagIds = currentTagIds.filter { id -> id !in updatedTagIds }
        deleteTaskTags(taskId, removedTagIds)
        val newTagIds = updatedTagIds.filter { id -> id !in currentTagIds }
        insertTaskTags(newTagIds.map { id -> TaskTag(taskId = taskId, tagId = id) })
    }

    @Query(
        """
        UPDATE tasks
        SET orderInCategory = orderInCategory + :delta
        WHERE
            status = :status
            AND orderInCategory BETWEEN :minOrderInCategory AND :maxOrderInCategory
        """
    )
    suspend fun shiftTasks(
        status: TaskStatus,
        minOrderInCategory: Int,
        maxOrderInCategory: Int,
        delta: Int
    )

    @Transaction
    suspend fun reorderTasks(
        taskId: Long,
        status: TaskStatus,
        currentOrderInCategory: Int,
        targetOrderInCategory: Int
    ) {
        if (currentOrderInCategory < targetOrderInCategory) {
            shiftTasks(status, currentOrderInCategory + 1, targetOrderInCategory, -1)
        } else {
            shiftTasks(status, targetOrderInCategory, currentOrderInCategory - 1, 1)
        }
        updateOrderInCategory(taskId, targetOrderInCategory)
    }
}
