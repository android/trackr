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

package com.example.android.trackr.ui

import android.app.Application
import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.android.trackr.TestApplication
import com.example.android.trackr.data.Tag
import com.example.android.trackr.data.TagColor
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class BindingAdaptersTagsTest {

    private lateinit var resources: Resources
    private lateinit var chipGroup: ChipGroup

    @Before
    fun setup() {
        val application: Application = ApplicationProvider.getApplicationContext()
        resources = application.resources
        chipGroup = ChipGroup(application)
    }

    private fun createChip(): Chip {
        val application: Application = ApplicationProvider.getApplicationContext()
        return Chip(application)
    }

    private fun createLabel(): TextView {
        return TextView(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun tags_oneTag() {
        val chips = (0 until 3).map { createChip().also { chipGroup.addView(it) } }
        val label = createLabel().also { chipGroup.addView(it) }
        val tags = listOf(Tag(1L, "a", TagColor.RED))
        chipGroup.tags(tags, false)
        assertThat(chips[0].text).isEqualTo("a")
        assertThat(chips[0].visibility).isEqualTo(View.VISIBLE)
        assertThat(chips[1].visibility).isEqualTo(View.GONE)
        assertThat(chips[2].visibility).isEqualTo(View.GONE)
        assertThat(label.visibility).isEqualTo(View.GONE)
    }

    @Test
    fun tags_fourTags_truncate() {
        val chips = (0 until 3).map { createChip().also { chipGroup.addView(it) } }
        val label = createLabel().also { chipGroup.addView(it) }
        val tags = listOf(
            Tag(1L, "a", TagColor.RED),
            Tag(2L, "b", TagColor.RED),
            Tag(3L, "c", TagColor.RED),
            Tag(4L, "d", TagColor.RED),
        )
        chipGroup.tags(tags, false)
        assertThat(chips[0].text).isEqualTo("a")
        assertThat(chips[0].visibility).isEqualTo(View.VISIBLE)
        assertThat(chips[1].text).isEqualTo("b")
        assertThat(chips[1].visibility).isEqualTo(View.VISIBLE)
        assertThat(chips[2].text).isEqualTo("c")
        assertThat(chips[2].visibility).isEqualTo(View.VISIBLE)
        assertThat(label.text).isEqualTo("+1")
        assertThat(label.visibility).isEqualTo(View.VISIBLE)
    }

    @Test
    fun tags_fourTags_append() {
        val chips = (0 until 3).map { createChip().also { chipGroup.addView(it) } }
        val tags = listOf(
            Tag(1L, "a", TagColor.RED),
            Tag(2L, "b", TagColor.RED),
            Tag(3L, "c", TagColor.RED),
            Tag(4L, "d", TagColor.RED),
        )
        chipGroup.tags(tags, true)
        assertThat(chips[0].text).isEqualTo("a")
        assertThat(chips[0].visibility).isEqualTo(View.VISIBLE)
        assertThat(chips[1].text).isEqualTo("b")
        assertThat(chips[1].visibility).isEqualTo(View.VISIBLE)
        assertThat(chips[2].text).isEqualTo("c")
        assertThat(chips[2].visibility).isEqualTo(View.VISIBLE)
        val chip = chipGroup.getChildAt(3) as Chip
        assertThat(chip.text).isEqualTo("d")
        assertThat(chip.visibility).isEqualTo(View.VISIBLE)
    }
}
