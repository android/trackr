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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TagColor
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagGroupTest {

    @get:Rule
    val rule = createComposeRule()

    @Suppress("SameParameterValue")
    private fun createTags(n: Int): List<Tag> {
        return (1..n).map { i ->
            Tag(
                id = i.toLong(),
                label = "tag $i",
                color = TagColor.YELLOW
            )
        }
    }

    @Test
    fun all() {
        rule.setContent {
            TrackrTheme {
                TagGroup(
                    tags = createTags(10)
                )
            }
        }
        for (i in 1..10) {
            rule.onNodeWithText("tag $i").assertIsDisplayed()
        }
    }

    @Test
    fun limit() {
        rule.setContent {
            TrackrTheme {
                TagGroup(
                    tags = createTags(10),
                    max = 6
                )
            }
        }
        for (i in 1..6) {
            rule.onNodeWithText("tag $i").assertIsDisplayed()
        }
        for (i in 7..10) {
            rule.onNodeWithText("tag $i").assertDoesNotExist()
        }
        rule.onNodeWithText("+4").assertIsDisplayed()
    }
}
