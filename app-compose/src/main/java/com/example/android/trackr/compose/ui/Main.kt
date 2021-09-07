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

package com.example.android.trackr.compose.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.android.trackr.compose.ui.detail.TaskDetail
import com.example.android.trackr.compose.ui.detail.TaskDetailViewModel
import com.example.android.trackr.compose.ui.tasks.Tasks
import com.google.accompanist.insets.ProvideWindowInsets

@Composable
fun Main() {
    TrackrTheme {
        ProvideWindowInsets {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "tasks",
            ) {
                composable("tasks") {
                    Tasks(
                        hiltViewModel(),
                        onTaskClick = { taskId ->
                            navController.navigate("detail/$taskId")
                        },
                        onAddTaskClick = {
                            // TODO
                        },
                        onArchiveClick = {
                            // TODO
                        },
                        onSettingsClick = {
                            // TODO
                        }
                    )
                }
                composable(
                    route = "detail/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.LongType })
                ) { backStackEntry ->
                    TaskDetail(
                        hiltViewModel<TaskDetailViewModel>().apply {
                            taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
                        }
                    )
                }
            }
        }
    }
}
